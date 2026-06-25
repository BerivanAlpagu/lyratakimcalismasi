package com.turkcell.lyraapp.ui.playlist_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.library.LibraryRepository
import com.turkcell.lyraapp.data.player.GlobalPlayerManager
import com.turkcell.lyraapp.data.songs.SongDto
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
    private val globalPlayerManager: GlobalPlayerManager,
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
            is PlaylistDetailIntent.AddSongClicked -> {
                _uiState.update { it.copy(isAddSongDialogVisible = true) }
                loadAllSongs()
            }
            is PlaylistDetailIntent.DismissAddSongDialog -> {
                _uiState.update { it.copy(isAddSongDialogVisible = false) }
            }
            is PlaylistDetailIntent.ConfirmAddSong -> {
                addSongToPlaylist(intent.songId)
            }
            is PlaylistDetailIntent.DeletePlaylistClicked -> {
                deletePlaylist()
            }
            is PlaylistDetailIntent.RemoveSongClicked -> {
                removeSong(intent.songId)
            }
            is PlaylistDetailIntent.ReorderSongs -> {
                reorderSongs(intent.fromIndex, intent.toIndex)
            }
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

    private fun loadAllSongs() {
        if (_uiState.value.allSongs.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSongs = true) }
            libraryRepository.getAllSongs()
                .onSuccess { songs ->
                    _uiState.update { it.copy(isLoadingSongs = false, allSongs = songs) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoadingSongs = false) }
                    _effect.send(
                        PlaylistDetailEffect.ShowError(
                            error.message ?: "Şarkılar yüklenemedi."
                        )
                    )
                }
        }
    }

    private fun addSongToPlaylist(songId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isAddSongDialogVisible = false) }
            libraryRepository.addSongToPlaylist(playlistId, songId)
                .onSuccess {
                    loadDetail(playlistId)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(
                        PlaylistDetailEffect.ShowError(
                            error.message ?: "Şarkı eklenemedi."
                        )
                    )
                }
        }
    }

    private fun playSong(songId: String, title: String, artist: String) {
        val songs = _uiState.value.playlist?.songs ?: emptyList()
        val index = songs.indexOfFirst { it.id == songId }
        globalPlayerManager.playSongWithQueue(songs, index)
        viewModelScope.launch {
            _effect.send(PlaylistDetailEffect.NavigateToPlayer(songId, title, artist))
        }
    }

    private fun deletePlaylist() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            libraryRepository.deletePlaylist(playlistId)
                .onSuccess {
                    _effect.send(PlaylistDetailEffect.NavigateBack)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(
                        PlaylistDetailEffect.ShowError(
                            error.message ?: "Çalma listesi silinemedi."
                        )
                    )
                }
        }
    }

    private fun removeSong(songId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            libraryRepository.removeSongFromPlaylist(playlistId, songId)
                .onSuccess {
                    loadDetail(playlistId)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(
                        PlaylistDetailEffect.ShowError(
                            error.message ?: "Şarkı çalma listesinden çıkarılamadı."
                        )
                    )
                }
        }
    }

    private fun reorderSongs(fromIndex: Int, toIndex: Int) {
        val playlist = _uiState.value.playlist ?: return
        val songs = playlist.songs.toMutableList()
        if (fromIndex in songs.indices && toIndex in songs.indices) {
            val moved = songs.removeAt(fromIndex)
            songs.add(toIndex, moved)
            _uiState.update { state ->
                state.copy(
                    playlist = playlist.copy(songs = songs)
                )
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(PlaylistDetailEffect.NavigateBack)
        }
    }
}
