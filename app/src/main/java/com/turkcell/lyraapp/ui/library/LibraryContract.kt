package com.turkcell.lyraapp.ui.library

/**
 * Library (Kütüphane) ekranının MVI sözleşmesi: State (durum), Intent (kullanıcı niyeti)
 * ve Effect (tek seferlik olay) tek dosyada toplanmıştır.
 *
 * Referans: docs/architecture/mvi-contracts.md.
 */

/**
 * Ekranın gözlemlenebilir tüm durumu. Tek bir immutable kaynak (single source of truth).
 *
 * [playlists] boş liste ile başlar; [isLoading] ilk yükleme sırasında `true` olur.
 * [errorMessage] yalnızca hata durumunda dolu olur ve yükleme başlarken temizlenir.
 */
data class LibraryUiState(
    val playlists: List<PlaylistUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * Çalma listesi öğesinin UI katmanına ait temsili.
 *
 * DTO'dan (veri katmanı) bağımsızdır: ViewModel bu dönüşümü yapar; ekran DTO bilmez.
 */
data class PlaylistUiModel(
    val id: String,
    val name: String,
    val description: String?,
)

/**
 * Kullanıcıdan gelen niyetler. UI yalnızca bu tipleri yayımlar; iş mantığını çalıştırmaz.
 */
sealed interface LibraryIntent {

    /** Ekran ilk açıldığında veya yenileme istendiğinde. */
    data object LoadPlaylists : LibraryIntent

    /** Hata sonrası yeniden deneme butonu. */
    data object RetryClicked : LibraryIntent

    /** Kullanıcı bir çalma listesine tıkladı. */
    data class PlaylistClicked(val playlistId: String) : LibraryIntent
}

/**
 * Tek seferlik (one-shot) olaylar: navigasyon, snackbar vb.
 * State içinde tutulmaz; konfigürasyon değişiminde tekrar tetiklenmez.
 */
sealed interface LibraryEffect {

    /** Ağ/ayrıştırma hatası; kullanıcıya gösterilecek mesaj. */
    data class ShowError(val message: String) : LibraryEffect

    /** Çalma listesi detay ekranına geçiş (sonraki faz için hazır). */
    data class NavigateToPlaylistDetail(val playlistId: String) : LibraryEffect
}
