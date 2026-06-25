package com.turkcell.lyraapp.ui.library

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
 * Library (Kütüphane) ekranının MVI ViewModel'i.
 *
 * Tek giriş noktası [onIntent]'tir. Durum [uiState] üzerinden gözlemlenir; tek seferlik
 * olaylar [effect] kanalından akar.
 *
 * ViewModel içinde Android/Compose/Context bağımlılığı yoktur —
 * bkz. docs/architecture/mvi-viewmodel-rules.md §3.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LibraryEffect>(Channel.BUFFERED)
    val effect: Flow<LibraryEffect> = _effect.receiveAsFlow()

    init {
        onIntent(LibraryIntent.LoadPlaylists)
    }

    fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.LoadPlaylists -> loadPlaylists()
            is LibraryIntent.RetryClicked -> loadPlaylists()
            is LibraryIntent.PlaylistClicked -> navigateToDetail(intent.playlistId)
            is LibraryIntent.TabSelected -> {
                _uiState.update { it.copy(selectedTab = intent.tab) }
                filterPlaylists()
            }
            is LibraryIntent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = intent.query) }
                filterPlaylists()
            }
            is LibraryIntent.ToggleSearch -> {
                _uiState.update { 
                    val newSearchActive = !it.isSearchActive
                    it.copy(
                        isSearchActive = newSearchActive,
                        searchQuery = if (newSearchActive) it.searchQuery else ""
                    )
                }
                filterPlaylists()
            }
            is LibraryIntent.CreatePlaylistClicked -> {
                viewModelScope.launch {
                    _effect.send(LibraryEffect.NavigateToCreatePlaylist)
                }
            }
        }
    }

    private fun loadPlaylists() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = libraryRepository.getMyPlaylists()

            result
                .onSuccess { dtos ->
                    val uiModels = dtos.map { dto ->
                        PlaylistUiModel(
                            id = dto.id,
                            name = dto.name,
                            description = dto.description,
                        )
                    }
                    _uiState.update { it.copy(isLoading = false, playlists = uiModels) }
                    filterPlaylists()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                    _effect.send(
                        LibraryEffect.ShowError(
                            error.message ?: "Çalma listeleri yüklenemedi."
                        )
                    )
                }
        }
    }

    private fun filterPlaylists() {
        _uiState.update { state ->
            val filtered = if (state.selectedTab != LibraryTab.PLAYLISTS) {
                emptyList()
            } else if (state.searchQuery.isBlank()) {
                state.playlists
            } else {
                state.playlists.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
            }
            state.copy(filteredPlaylists = filtered)
        }
    }

    private fun navigateToDetail(playlistId: String) {
        viewModelScope.launch {
            _effect.send(LibraryEffect.NavigateToPlaylistDetail(playlistId))
        }
    }
}