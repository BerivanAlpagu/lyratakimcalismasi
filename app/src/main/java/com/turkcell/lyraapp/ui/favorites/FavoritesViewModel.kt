package com.turkcell.lyraapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.local.FavoritesStore
import com.turkcell.lyraapp.data.local.OfflineManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesStore: FavoritesStore,
    private val offlineManager: OfflineManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<FavoritesEffect>()
    val effect: SharedFlow<FavoritesEffect> = _effect.asSharedFlow()

    init {
        handleIntent(FavoritesIntent.LoadFavorites)

        viewModelScope.launch {
            offlineManager.downloadedSongIds.collect { downloadedIds ->
                _uiState.update { it.copy(downloadedSongIds = downloadedIds) }
            }
        }
        viewModelScope.launch {
            offlineManager.downloadingSongIds.collect { downloadingIds ->
                _uiState.update { it.copy(downloadingSongIds = downloadingIds) }
            }
        }
    }

    fun handleIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.LoadFavorites -> loadFavorites()
            FavoritesIntent.DownloadFavoritesClicked -> downloadFavorites()
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            favoritesStore.favoriteSongs.collect { favoriteSongs ->
                val uiModels = favoriteSongs.map { song ->
                    FavoriteSongUiModel(
                        id = song.id,
                        title = song.title,
                        artist = song.artist,
                        duration = song.duration,
                        durationMs = song.durationMs
                    )
                }
                _uiState.update { it.copy(favorites = uiModels, isLoading = false) }
            }
        }
    }

    private fun downloadFavorites() {
        viewModelScope.launch {
            val songs = _uiState.value.favorites
            if (songs.isEmpty()) return@launch

            val downloadedIds = _uiState.value.downloadedSongIds
            val allDownloaded = songs.all { downloadedIds.contains(it.id) }

            if (allDownloaded) {
                songs.forEach { song ->
                    offlineManager.deleteSong(song.id)
                }
            } else {
                songs.forEach { song ->
                    if (!downloadedIds.contains(song.id)) {
                        val result = offlineManager.downloadSong(
                            songId = song.id,
                            title = song.title,
                            artist = song.artist,
                            durationMs = song.durationMs
                        )
                        if (result.isFailure) {
                            val errorMsg = result.exceptionOrNull()?.message ?: "İndirme başarısız oldu."
                            _effect.emit(FavoritesEffect.ShowError(errorMsg))
                        }
                    }
                }
            }
        }
    }
}
