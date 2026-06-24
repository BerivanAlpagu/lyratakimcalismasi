package com.turkcell.lyraapp.ui.player

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.player.GlobalPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val globalPlayerManager: GlobalPlayerManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle[ARG_SONG_ID]) {
        "PlayerViewModel için '$ARG_SONG_ID' argümanı zorunludur."
    }
    private val title: String = savedStateHandle.get<String>(ARG_TITLE).orEmpty()
    private val artist: String = savedStateHandle.get<String>(ARG_ARTIST).orEmpty()

    val uiState: StateFlow<PlayerUiState> = globalPlayerManager.playerState.map { state ->
        PlayerUiState(
            title = state.title.ifEmpty { title },
            artist = state.artist.ifEmpty { artist },
            isPlaying = state.isPlaying,
            isLoading = state.isLoading,
            isBuffering = state.isBuffering,
            hasEnded = state.hasEnded,
            positionMs = state.positionMs,
            durationMs = state.durationMs,
            bufferedMs = state.bufferedMs,
            errorMessage = state.errorMessage
            // İlerleyen fazlarda ihtiyaç halinde buraya globalPlayerManager üzerinden
            // isFavorite, repeatMode, isShuffling gibi durumlar da map'lenebilir.
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState(title = title, artist = artist, isLoading = true)
    )

    init {
        // Eğer manager şu an BU şarkıyı çalmıyorsa, çalmasını söylüyoruz.
        if (globalPlayerManager.playerState.value.songId != songId) {
            globalPlayerManager.playSong(songId, title, artist)
        }
    }

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            // Mevcut Lojikleriniz (Aynen Korundu)
            PlayerIntent.TogglePlayPause -> globalPlayerManager.togglePlayPause()
            PlayerIntent.Restart -> globalPlayerManager.restart()
            PlayerIntent.SeekForward -> globalPlayerManager.seekBy(SEEK_STEP_MS)
            PlayerIntent.SeekBackward -> globalPlayerManager.seekBy(-SEEK_STEP_MS)
            is PlayerIntent.SeekTo -> globalPlayerManager.seekTo(intent.positionMs)
            PlayerIntent.Retry -> globalPlayerManager.playSong(songId, title, artist)

            // ─── YENİ KONTRATA GÖRE IDE HATASINI ÇÖZEN EKSİK DALLAR ───
            PlayerIntent.SkipNext -> {
                // TODO: İlerleyen fazda playlist kuyruğu bağlandığında manager tetiklenecek
            }
            PlayerIntent.SkipPrevious -> {
                // TODO: İlerleyen fazda playlist kuyruğu bağlandığında manager tetiklenecek
            }
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
                // TODO: Palette API'den gelen renk durumunu UI state'e yazma lojiği
            }
        }
    }

    companion object {
        const val ARG_SONG_ID = "songId"
        const val ARG_TITLE = "title"
        const val ARG_ARTIST = "artist"

        private const val SEEK_STEP_MS = 10_000L
    }
}