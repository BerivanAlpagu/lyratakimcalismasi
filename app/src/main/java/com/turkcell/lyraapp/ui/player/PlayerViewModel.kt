package com.turkcell.lyraapp.ui.player

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.data.player.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Oynatıcı ekranının ViewModel'i; ExoPlayer örneğinin sahibidir (bkz. mvi-viewmodel-rules.md).
 *
 * [SavedStateHandle]'dan şarkı kimliğini ve başlık/sanatçı bilgisini okur, oynatmadan hemen
 * önce imzalı stream URL'sini ([PlayerRepository]) alıp ExoPlayer'a verir. Oynatıcı durumu
 * (oynatılıyor mu, tampon, süre, konum, hata) [Player.Listener] geri çağrıları ve yarım
 * saniyelik konum yoklamasıyla [PlayerUiState]'e yansıtılır. Player, [onCleared]'da serbest
 * bırakılır.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val playerRepository: PlayerRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle[ARG_SONG_ID]) {
        "PlayerViewModel için '$ARG_SONG_ID' argümanı zorunludur."
    }
    private val title: String = savedStateHandle.get<String>(ARG_TITLE).orEmpty()
    private val artist: String = savedStateHandle.get<String>(ARG_ARTIST).orEmpty()

    private val _uiState = MutableStateFlow(
        PlayerUiState(title = title, artist = artist, isLoading = true),
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _uiState.update {
                it.copy(
                    isLoading = it.isLoading && playbackState == Player.STATE_BUFFERING && it.durationMs == 0L,
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                    hasEnded = playbackState == Player.STATE_ENDED,
                )
            }
            if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _uiState.update {
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
        loadAndPlay()
        observePosition()
    }

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            PlayerIntent.TogglePlayPause -> {
                if (player.isPlaying) player.pause() else player.play()
            }

            PlayerIntent.Restart -> {
                player.seekTo(0L)
                player.play()
            }

            PlayerIntent.SeekForward -> seekBy(SEEK_STEP_MS)
            PlayerIntent.SeekBackward -> seekBy(-SEEK_STEP_MS)
            is PlayerIntent.SeekTo -> {
                val target = intent.positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L))
                player.seekTo(target)
            }

            PlayerIntent.Retry -> loadAndPlay()
        }
    }

    private fun seekBy(deltaMs: Long) {
        val duration = player.duration.coerceAtLeast(0L)
        val target = (player.currentPosition + deltaMs).coerceIn(0L, duration)
        player.seekTo(target)
    }

    private fun loadAndPlay() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            playerRepository.getStreamUrl(songId)
                .onSuccess { url ->
                    player.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
                    player.prepare()
                    player.playWhenReady = true
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Stream adresi alınamadı.",
                        )
                    }
                }
        }
    }

    /** Oynatıcının anlık konum/süre/tampon değerlerini periyodik olarak duruma yansıtır. */
    private fun observePosition() {
        viewModelScope.launch {
            while (isActive) {
                _uiState.update {
                    it.copy(
                        positionMs = player.currentPosition.coerceAtLeast(0L),
                        durationMs = player.duration.coerceAtLeast(0L),
                        bufferedMs = player.bufferedPosition.coerceAtLeast(0L),
                    )
                }
                delay(POSITION_POLL_MS)
            }
        }
    }

    override fun onCleared() {
        player.removeListener(listener)
        player.release()
    }

    companion object {
        const val ARG_SONG_ID = "songId"
        const val ARG_TITLE = "title"
        const val ARG_ARTIST = "artist"

        private const val SEEK_STEP_MS = 10_000L
        private const val POSITION_POLL_MS = 500L
    }
}
