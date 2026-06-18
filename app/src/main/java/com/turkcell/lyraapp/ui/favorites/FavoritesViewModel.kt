package com.turkcell.lyraapp.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.favorites.FavoritesRepository
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
    private val repository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<FavoritesEffect>()
    val effect: SharedFlow<FavoritesEffect> = _effect.asSharedFlow()

    init {
        handleIntent(FavoritesIntent.LoadFavorites)
    }

    fun handleIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.LoadFavorites -> loadFavorites()
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getFavorites()
                .onSuccess { favorites ->
                    val uiModels = favorites.map { song ->
                        FavoriteSongUiModel(
                            id = song.id,
                            title = song.title,
                            artist = song.artist,
                            duration = song.album ?: "0:00" // Album field is used for duration in mock
                        )
                    }
                    _uiState.update { it.copy(favorites = uiModels, isLoading = false) }
                }
                .onFailure { error ->
                    val errorMsg = error.message ?: "Bilinmeyen bir hata oluştu"
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                    _effect.emit(FavoritesEffect.ShowError(errorMsg))
                }
        }
    }
}
