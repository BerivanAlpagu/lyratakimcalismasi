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
 * Kütüphane ekranının MVI ViewModel'i.
 *
 * Tek giriş noktası [onIntent]'tir. Durum [uiState] üzerinden gözlemlenir; tek seferlik
 * olaylar [effect] kanalından akar.
 *
 * Kural referansı: mvi-viewmodel-rules.md §2-6.
 * Referans implementasyon: LoginViewModel.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LibraryEffect>(Channel.BUFFERED)
    val effect: Flow<LibraryEffect> = _effect.receiveAsFlow()

    /**
     * Tek giriş noktası. Tüm [LibraryIntent] dalları exhaustive olarak ele alınır.
     */
    fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.LoadPlaylists -> loadPlaylists()
            is LibraryIntent.TabSelected -> onTabSelected(intent.tab)
            is LibraryIntent.PlaylistClicked -> onPlaylistClicked(intent.playlistId)
            is LibraryIntent.RetryClicked -> loadPlaylists()
        }
    }

    /**
     * Playlist listesini API'dan çeker. Çift çağrıya karşı [isLoading] ile korunur.
     */
    private fun loadPlaylists() {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            libraryRepository.getPlaylists()
                .onSuccess { playlists ->
                    _uiState.update { it.copy(isLoading = false, playlists = playlists) }
                }
                .onFailure { error ->
                    // State'e yazilir → LibraryScreen'deki inline hata / retry ekrani gorünür.
                    // Effect ayrica gönderilmez; tek seferlik Snackbar yerine kalici
                    // hata ekrani (RetryClicked ile sifirlanir) daha iyi UX saglar.
                    val message = error.message ?: "Playlist listesi yuklenemedi."
                    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                }
        }
    }

    /**
     * Sekme seçimini state'e yansıtır. Veri çekme bu fazda yalnızca PLAYLISTS sekmesinde
     * gerçekleşir; diğer sekmeler boş durum gösterir.
     */
    private fun onTabSelected(tab: LibraryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == LibraryTab.PLAYLISTS && _uiState.value.playlists.isEmpty()) {
            loadPlaylists()
        }
    }

    /**
     * Playlist tıklanmasını detay navigasyonu Effect'ine dönüştürür.
     * ViewModel, navigasyon API'si bilmez (bkz. mvi-viewmodel-rules.md §3-6).
     */
    private fun onPlaylistClicked(playlistId: String) {
        viewModelScope.launch {
            _effect.send(LibraryEffect.NavigateToPlaylistDetail(playlistId))
        }
    }
}
