package com.turkcell.lyraapp.ui.create_playlist

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
 * Yeni Çalma Listesi Oluşturma ekranının MVI ViewModel'i.
 *
 * Tek giriş noktası [onIntent]'tir. Durum [uiState] üzerinden gözlemlenir; tek seferlik
 * olaylar [effect] kanalından akar.
 *
 * Referans: docs/architecture/mvi-viewmodel-rules.md.
 */
@HiltViewModel
class CreatePlaylistViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePlaylistUiState())
    val uiState: StateFlow<CreatePlaylistUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CreatePlaylistEffect>(Channel.BUFFERED)
    val effect: Flow<CreatePlaylistEffect> = _effect.receiveAsFlow()

    init {
        loadSongs()
    }

    fun onIntent(intent: CreatePlaylistIntent) {
        when (intent) {
            is CreatePlaylistIntent.NameChanged -> {
                _uiState.update { it.copy(playlistName = intent.name) }
            }
            is CreatePlaylistIntent.DescriptionChanged -> {
                _uiState.update { it.copy(playlistDescription = intent.description) }
            }
            is CreatePlaylistIntent.TogglePublic -> {
                _uiState.update { it.copy(isPublic = !it.isPublic) }
            }
            is CreatePlaylistIntent.ToggleSongSelection -> {
                _uiState.update { state ->
                    val newSet = state.selectedSongIds.toMutableSet()
                    if (newSet.contains(intent.songId)) {
                        newSet.remove(intent.songId)
                    } else {
                        newSet.add(intent.songId)
                    }
                    state.copy(selectedSongIds = newSet)
                }
            }
            is CreatePlaylistIntent.SaveClicked -> {
                savePlaylist()
            }
            is CreatePlaylistIntent.CloseClicked -> {
                viewModelScope.launch {
                    _effect.send(CreatePlaylistEffect.NavigateBack)
                }
            }
        }
    }

    private fun loadSongs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSongs = true, errorMessage = null) }

            libraryRepository.getAllSongs()
                .onSuccess { dtos ->
                    val uiModels = dtos.map { dto ->
                        SongUiModel(
                            id = dto.id,
                            title = dto.title,
                            artist = dto.artist,
                        )
                    }
                    _uiState.update { it.copy(isLoadingSongs = false, allSongs = uiModels) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoadingSongs = false, errorMessage = error.message) }
                    _effect.send(
                        CreatePlaylistEffect.ShowError(
                            error.message ?: "Sarkilar yuklenemedi."
                        )
                    )
                }
        }
    }

    private fun savePlaylist() {
        val state = _uiState.value

        if (state.playlistName.isBlank()) {
            viewModelScope.launch {
                _effect.send(CreatePlaylistEffect.ShowError("Calma listesi ismi bos olamaz."))
            }
            return
        }

        if (state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val description = state.playlistDescription.takeIf { it.isNotBlank() }

            libraryRepository.createPlaylist(state.playlistName, description)
                .onSuccess { playlist ->
                    // Secili sarkilari sirayla playliste ekle
                    var allAdded = true
                    for (songId in state.selectedSongIds) {
                        libraryRepository.addSongToPlaylist(playlist.id, songId)
                            .onFailure {
                                allAdded = false
                            }
                    }

                    _uiState.update { it.copy(isSaving = false) }

                    if (!allAdded) {
                        _effect.send(
                            CreatePlaylistEffect.ShowError(
                                "Calma listesi olusturuldu ancak bazi sarkilar eklenemedi."
                            )
                        )
                    }

                    _effect.send(CreatePlaylistEffect.PlaylistCreatedSuccessfully)
                    _effect.send(CreatePlaylistEffect.NavigateBack)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    _effect.send(
                        CreatePlaylistEffect.ShowError(
                            error.message ?: "Calma listesi olusturulamadi."
                        )
                    )
                }
        }
    }
}
