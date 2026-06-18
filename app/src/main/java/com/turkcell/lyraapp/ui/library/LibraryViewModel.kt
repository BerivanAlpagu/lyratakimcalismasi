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
 *
 * ViewModel içinde Android/Compose/Context bağımlılığı yoktur.
 * DTO → UiModel dönüşümü burada yapılır; UI katmanı [PlaylistUiModel] bilir, DTO bilmez.
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
     *
     * DTO → [PlaylistUiModel] dönüşümü burada yapılır; ekran DTO bilmez.
     */
    private fun loadPlaylists() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            libraryRepository.getPlaylists()
                .onSuccess { dtos ->
                    val uiModels = dtos.map { dto ->
                        PlaylistUiModel(
                            id = dto.id,
                            name = dto.name,
                            description = dto.description,
                        )
                    }
                    _uiState.update { it.copy(isLoading = false, playlists = uiModels) }
                }
                .onFailure { error ->
                    // Hata state'e yazılır → LibraryScreen inline hata / retry gösterir.
                    // Effect gönderilmez: kalıcı hata ekranı (RetryClicked ile sıfırlanır)
                    // tek seferlik Snackbar'dan daha iyi UX sağlar.
                    val message = error.message ?: "Çalma listeleri yüklenemedi."
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
