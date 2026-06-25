package com.turkcell.lyraapp.ui.player

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.local.FavoriteSong
import com.turkcell.lyraapp.data.local.FavoritesStore
import com.turkcell.lyraapp.data.player.GlobalPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val globalPlayerManager: GlobalPlayerManager,
    private val favoritesStore: FavoritesStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle[ARG_SONG_ID]) {
        "PlayerViewModel için '$ARG_SONG_ID' argümanı zorunludur."
    }
    private val title: String = savedStateHandle.get<String>(ARG_TITLE).orEmpty()
    private val artist: String = savedStateHandle.get<String>(ARG_ARTIST).orEmpty()
    private val coverUrl: String = savedStateHandle.get<String>(ARG_COVER_URL).orEmpty()
    private val playlistName: String = savedStateHandle.get<String>(ARG_PLAYLIST_NAME).orEmpty()
    private val isFavoriteArg: Boolean = savedStateHandle.get<Boolean>(ARG_IS_FAVORITE) ?: false

    /** Palette API'den UI katmanı tarafından yazılan baskın renk. */
    private val _dominantColor = MutableStateFlow<Color?>(null)

    val uiState: StateFlow<PlayerUiState> = combine(
        globalPlayerManager.playerState,
        favoritesStore.favoriteSongIds,
        _dominantColor,
    ) { state, favoriteIds, dominantColor ->
        PlayerUiState(
            songId = songId,
            title = state.title.ifEmpty { title },
            artist = state.artist.ifEmpty { artist },
            coverUrl = coverUrl,
            playlistName = playlistName,
            isPlaying = state.isPlaying,
            isLoading = state.isLoading,
            isBuffering = state.isBuffering,
            hasEnded = state.hasEnded,
            positionMs = state.positionMs,
            durationMs = state.durationMs,
            bufferedMs = state.bufferedMs,
            errorMessage = state.errorMessage,
            isFavorite = favoriteIds.contains(songId),
            dominantColor = dominantColor,
            canSkipNext = state.canSkipNext,
            canSkipPrevious = state.canSkipPrevious,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState(
            songId = songId,
            title = title,
            artist = artist,
            coverUrl = coverUrl,
            playlistName = playlistName,
            isFavorite = isFavoriteArg,
            isLoading = true
        ),
    )

    init {
        // Manager şu an BU şarkıyı çalmıyorsa, çalmasını söylüyoruz.
        if (globalPlayerManager.playerState.value.songId != songId) {
            globalPlayerManager.playSong(songId, title, artist)
        }
    }

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            PlayerIntent.TogglePlayPause -> globalPlayerManager.togglePlayPause()
            PlayerIntent.Restart -> globalPlayerManager.restart()
            PlayerIntent.SeekForward -> globalPlayerManager.seekBy(SEEK_STEP_MS)
            PlayerIntent.SeekBackward -> globalPlayerManager.seekBy(-SEEK_STEP_MS)
            is PlayerIntent.SeekTo -> globalPlayerManager.seekTo(intent.positionMs)
            PlayerIntent.Retry -> globalPlayerManager.playSong(songId, title, artist)
            PlayerIntent.SkipNext -> globalPlayerManager.playNext()
            PlayerIntent.SkipPrevious -> globalPlayerManager.playPrevious()
            PlayerIntent.ToggleFavorite -> {
                viewModelScope.launch {
                    val currentState = globalPlayerManager.playerState.value
                    favoritesStore.toggleFavorite(
                        FavoriteSong(
                            id = songId,
                            title = currentState.title.ifEmpty { title },
                            artist = currentState.artist.ifEmpty { artist },
                            duration = formatSongDuration(currentState.durationMs),
                            durationMs = currentState.durationMs,
                            artworkStartColor = currentState.artworkStartColor,
                            artworkEndColor = currentState.artworkEndColor
                        )
                    )
                }
            }
            PlayerIntent.ToggleRepeat -> {
                // TODO: Döngüsel tekrar modu lojiği bağlandığında tetiklenecek
            }
            PlayerIntent.ToggleShuffle -> {
                // TODO: Karışık çalma lojiği bağlandığında tetiklenecek
            }
            is PlayerIntent.UpdateDominantColor -> {
                _dominantColor.value = intent.color?.let { Color(it) }
            }
        }
    }

    private fun formatSongDuration(ms: Long): String {
        val totalSeconds = (ms.coerceAtLeast(0L)) / 1000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return "%d:%02d".format(minutes, seconds)
    }

    companion object {
        const val ARG_SONG_ID = "songId"
        const val ARG_TITLE = "title"
        const val ARG_ARTIST = "artist"
        const val ARG_COVER_URL = "coverUrl"
        const val ARG_PLAYLIST_NAME = "playlistName"
        const val ARG_IS_FAVORITE = "isFavorite"

        private const val SEEK_STEP_MS = 10_000L
    }
}
