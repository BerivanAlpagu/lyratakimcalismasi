package com.turkcell.lyraapp.ui.player

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.player.GlobalPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val globalPlayerManager: GlobalPlayerManager,
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
        _dominantColor,
    ) { state, dominantColor ->
        PlayerUiState(
            songId = songId,
            title = state.title.ifEmpty { title },
            artist = state.artist.ifEmpty { artist },
            coverUrl = coverUrl,
            playlistName = playlistName,
            isFavorite = isFavoriteArg,
            isPlaying = state.isPlaying,
            isLoading = state.isLoading,
            isBuffering = state.isBuffering,
            hasEnded = state.hasEnded,
            positionMs = state.positionMs,
            durationMs = state.durationMs,
            bufferedMs = state.bufferedMs,
            errorMessage = state.errorMessage,
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
                // TODO: Favori repo/manager lojiği bağlandığında tetiklenecek
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