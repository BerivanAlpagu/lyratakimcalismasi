package com.turkcell.lyraapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.songs.SongsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Arama ekranının ViewModel'i.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val songsApi: SongsApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SearchEffect>(Channel.BUFFERED)
    val effect: Flow<SearchEffect> = _effect.receiveAsFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        queryFlow
            .debounce(300L)
            .distinctUntilChanged()
            .onEach { query ->
                performSearch(query)
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> {
                _uiState.update { it.copy(query = intent.value) }
                queryFlow.value = intent.value
            }
            is SearchIntent.FilterSelected -> {
                _uiState.update { it.copy(selectedFilter = intent.filter) }
                performSearch(_uiState.value.query)
            }
            is SearchIntent.SongSelected -> {
                viewModelScope.launch {
                    _effect.send(SearchEffect.NavigateToPlayer(intent.song.id, intent.song.title, intent.song.artist))
                }
            }
        }
    }

    private fun performSearch(query: String) {
        val searchTarget = when {
            query.isNotBlank() -> query
            _uiState.value.selectedFilter != "Hepsi" -> _uiState.value.selectedFilter
            else -> ""
        }

        if (searchTarget.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isLoading = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                songsApi.getSongs(query = searchTarget)
            }.onSuccess { response ->
                _uiState.update { it.copy(searchResults = response.data, isLoading = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _effect.send(SearchEffect.ShowError(error.message ?: "Arama sırasında bir hata oluştu."))
            }
        }
    }
}