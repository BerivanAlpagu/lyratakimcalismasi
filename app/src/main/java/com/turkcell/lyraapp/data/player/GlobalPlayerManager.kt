package com.turkcell.lyraapp.data.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.data.songs.SongDto
import com.turkcell.lyraapp.ui.player.PlayerUiState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.turkcell.lyraapp.LyraMediaService
import androidx.media3.common.MediaMetadata
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playerRepository: PlayerRepository,
) {
    /** LyraMediaService tarafından MediaSession'a bağlanmak için erişilebilir. */
    val player: ExoPlayer = ExoPlayer.Builder(context).build()
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var pollJob: Job? = null
    private var playlistQueue: List<SongDto> = emptyList()
    private var currentSongIndex: Int = -1
    private var songAfterAd: PlaybackDecision.Song? = null
    private var currentAdImpressionId: String? = null

    // PlayerUiState includes title, artist, isPlaying, duration, etc.
    // To also carry the songId and artwork colors for the Home screen NowPlayingBar,
    // we can either add them to PlayerUiState or keep them parallel.
    // For simplicity, we add songId, artworkStartColor, artworkEndColor to PlayerUiState.
    // But since we don't want to modify PlayerUiState heavily if it's already used, we can just expose a wrapper.

    private val _playerState = MutableStateFlow(GlobalPlayerState())
    val playerState: StateFlow<GlobalPlayerState> = _playerState.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _playerState.update {
                it.copy(
                    isLoading = it.isLoading && playbackState == Player.STATE_BUFFERING && it.durationMs == 0L,
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                    hasEnded = playbackState == Player.STATE_ENDED,
                )
            }
            if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
                _playerState.update { it.copy(isLoading = false) }
            }
            if (playbackState == Player.STATE_ENDED) {
                val pendingSong = songAfterAd
                val impressionId = currentAdImpressionId
                if (pendingSong != null && impressionId != null) {
                    songAfterAd = null
                    currentAdImpressionId = null
                    scope.launch {
                        playerRepository.completeAd(impressionId)
                        playResolvedSong(pendingSong)
                    }
                } else {
                    playNext()
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _playerState.update {
                it.copy(
                    isLoading = false,
                    isBuffering = false,
                    errorMessage = error.localizedMessage ?: "Şarkı oynatılamadı.",
                )
            }
        }
    }

    init {
        player.addListener(listener)
    }

    fun playSongWithQueue(songs: List<SongDto>, startIndex: Int) {
        playlistQueue = songs
        currentSongIndex = startIndex
        updateSkipState()
        if (startIndex in songs.indices) {
            val song = songs[startIndex]
            playSong(song.id, song.title, song.artist)
        }
    }

    fun playNext() {
        if (playlistQueue.isNotEmpty() && currentSongIndex < playlistQueue.lastIndex) {
            currentSongIndex++
            val nextSong = playlistQueue[currentSongIndex]
            playSong(nextSong.id, nextSong.title, nextSong.artist)
        }
        updateSkipState()
    }

    fun playPrevious() {
        if (playlistQueue.isNotEmpty() && currentSongIndex > 0) {
            currentSongIndex--
            val prevSong = playlistQueue[currentSongIndex]
            playSong(prevSong.id, prevSong.title, prevSong.artist)
        } else {
            // Listedeki ilk şarkıysa başa sar.
            restart()
        }
        updateSkipState()
    }

    private fun updateSkipState() {
        _playerState.update {
            it.copy(
                canSkipNext = playlistQueue.isNotEmpty() && currentSongIndex < playlistQueue.lastIndex,
                canSkipPrevious = playlistQueue.isNotEmpty() && currentSongIndex > 0,
            )
        }
    }

    fun playSong(songId: String, title: String, artist: String, artworkStartColor: Long = 0L, artworkEndColor: Long = 0L) {
        if (_playerState.value.songId == songId && player.playbackState != Player.STATE_IDLE && player.playbackState != Player.STATE_ENDED) {
            // Already playing or loaded this song
            if (!player.isPlaying) player.play()
            return
        }

        _playerState.update {
            it.copy(
                songId = songId,
                title = title,
                artist = artist,
                artworkStartColor = artworkStartColor,
                artworkEndColor = artworkEndColor,
                isLoading = true,
                errorMessage = null,
                positionMs = 0L,
                durationMs = 0L,
                bufferedMs = 0L,
                hasEnded = false
            )
        }

        val serviceIntent = Intent(context, LyraMediaService::class.java)
        try {
            context.startService(serviceIntent)
        } catch (e: Exception) {
            ContextCompat.startForegroundService(context, serviceIntent)
        }

        scope.launch {
            playerRepository.resolveNextPlayback(songId)
                .onSuccess { decision ->
                    when (decision) {
                        is PlaybackDecision.Song -> playResolvedSong(decision)
                        is PlaybackDecision.AdThenSong -> playAdThenSong(decision)
                    }
                }
                .onFailure { error ->
                    _playerState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Stream adresi alınamadı.",
                        )
                    }
                }
        }
    }

    private fun playAdThenSong(decision: PlaybackDecision.AdThenSong) {
        songAfterAd = PlaybackDecision.Song(
            songId = decision.songId,
            title = decision.title,
            artist = decision.artist,
            streamUrl = decision.streamUrl,
        )
        currentAdImpressionId = decision.impressionId
        _playerState.update {
            it.copy(
                songId = decision.adId,
                title = decision.adTitle,
                artist = decision.advertiser,
                isLoading = true,
                errorMessage = null,
                positionMs = 0L,
                durationMs = 0L,
                bufferedMs = 0L,
                hasEnded = false,
            )
        }
        playMedia(
            mediaId = decision.adId,
            title = decision.adTitle,
            artist = decision.advertiser,
            url = decision.adStreamUrl,
        )
    }

    private fun playResolvedSong(decision: PlaybackDecision.Song) {
        _playerState.update {
            it.copy(
                songId = decision.songId,
                title = decision.title,
                artist = decision.artist,
                isLoading = true,
                errorMessage = null,
                positionMs = 0L,
                durationMs = 0L,
                bufferedMs = 0L,
                hasEnded = false,
            )
        }
        playMedia(
            mediaId = decision.songId,
            title = decision.title,
            artist = decision.artist,
            url = decision.streamUrl,
        )
    }

    private fun playMedia(
        mediaId: String,
        title: String,
        artist: String,
        url: String,
    ) {
        val dummyCoverUrl = "https://picsum.photos/seed/$mediaId/300/300"
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setDisplayTitle(title)
            .setArtworkUri(Uri.parse(dummyCoverUrl))
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(mediaId)
            .setUri(Uri.parse(url))
            .setMediaMetadata(mediaMetadata)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
        startPolling()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun skipToNext() {
        playNext()
    }

    fun skipToPrevious() {
        if (player.currentPosition > 3000L) {
            player.seekTo(0L)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else {
            player.seekTo(0L)
        }
    }

    fun toggleFavorite() {
        _playerState.update { it.copy(isFavorite = !it.isFavorite) }
    }

    fun seekTo(positionMs: Long) {
        val target = positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L))
        player.seekTo(target)
    }

    fun seekBy(deltaMs: Long) {
        val duration = player.duration.coerceAtLeast(0L)
        val target = (player.currentPosition + deltaMs).coerceIn(0L, duration)
        player.seekTo(target)
    }

    fun restart() {
        player.seekTo(0L)
        player.play()
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = scope.launch {
            while (isActive) {
                _playerState.update {
                    it.copy(
                        positionMs = player.currentPosition.coerceAtLeast(0L),
                        durationMs = player.duration.coerceAtLeast(0L),
                        bufferedMs = player.bufferedPosition.coerceAtLeast(0L),
                    )
                }
                delay(500L)
            }
        }
    }
}

data class GlobalPlayerState(
    val songId: String? = null,
    val title: String = "",
    val artist: String = "",
    val artworkStartColor: Long = 0L,
    val artworkEndColor: Long = 0L,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isBuffering: Boolean = false,
    val hasEnded: Boolean = false,
    val isFavorite: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val errorMessage: String? = null,
    /** Playlist kuyruğunda bir sonraki öğe var mı. */
    val canSkipNext: Boolean = false,
    /** Playlist kuyruğunda bir önceki öğe var mı. */
    val canSkipPrevious: Boolean = false,
)
