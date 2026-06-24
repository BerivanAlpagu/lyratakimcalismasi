package com.turkcell.lyraapp.ui.playlist_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.library.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Çalma listesi detay ekranının Hilt destekli MVI ViewModel sınıfı.
 *
 * [SavedStateHandle] üzerinden gelen `playlistId` değerini çözer.
 */
@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"]) {
        "playlistId parametresi PlaylistDetail ekranı için zorunludur."
    }

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PlaylistDetailEffect>(Channel.BUFFERED)
    val effect: Flow<PlaylistDetailEffect> = _effect.receiveAsFlow()

    init {
        onIntent(PlaylistDetailIntent.LoadDetail(playlistId))
    }

    fun onIntent(intent: PlaylistDetailIntent) {
        when (intent) {
            is PlaylistDetailIntent.LoadDetail -> loadDetail(intent.id)
            is PlaylistDetailIntent.RetryClicked -> loadDetail(playlistId)
            is PlaylistDetailIntent.BackClicked -> navigateBack()
            is PlaylistDetailIntent.SongClicked -> playSong(intent.songId, intent.title, intent.artist)
        }
    }

    private fun loadDetail(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            libraryRepository.getPlaylistDetail(id)
                .onSuccess { detail ->
                    _uiState.update { it.copy(isLoading = false, playlist = detail) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                    _effect.send(
                        PlaylistDetailEffect.ShowError(
                            error.message ?: "Çalma listesi detayları yüklenemedi."
                        )
                    )
                }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(PlaylistDetailEffect.NavigateBack)
        }
    }

    private fun playSong(songId: String, title: String, artist: String) {
        viewModelScope.launch {
            _effect.send(PlaylistDetailEffect.NavigateToPlayer(songId, title, artist))
        }
    }
}
