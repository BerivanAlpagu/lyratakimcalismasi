package com.turkcell.lyraapp.ui.library

/**
 * Library (Kütüphane) ekranının MVI sözleşmesi: State, Intent ve Effect tek dosyada.
 *
 * Referans: [com.turkcell.lyraapp.ui.auth.login.LoginContract] (bkz. mvi-contracts.md).
 *
 * NOT: Veri katmanı tipi olan [com.turkcell.lyraapp.data.playlists.PlaylistDto] bu dosyaya
 * sızdırılmaz; UI katmanı yalnızca [PlaylistUiModel]'i bilir. Dönüşüm ViewModel'de yapılır.
 */

/**
 * Kütüphane sekmelerini temsil eder.
 *
 * ARTISTS ve ALBUMS sekmeleri bu fazda backend desteği olmadığından boş durum gösterir.
 */
enum class LibraryTab(val label: String) {
    PLAYLISTS("Çalma listeleri"),
    ARTISTS("Sanatçılar"),
    ALBUMS("Albümler"),
}

/**
 * Çalma listesi öğesinin UI katmanına ait temsili.
 *
 * DTO'dan (veri katmanı) bağımsızdır; ViewModel bu dönüşümü yapar.
 * Ekran DTO bilmez — bkz. agents.md §2.4 ve mvi-contracts.md §2.
 */
data class PlaylistUiModel(
    val id: String,
    val name: String,
    val description: String?,
)

/**
 * Ekranın gözlemlenebilir tüm durumu. Tek bir immutable kaynak (single source of truth).
 *
 * [isLoading] ağ isteği devam ederken `true`'dur.
 * [playlists] yalnızca [selectedTab] == [LibraryTab.PLAYLISTS] olduğunda anlamlıdır.
 * [errorMessage] `null` değilken inline hata durumu gösterilir; Retry ile sıfırlanır.
 */
data class LibraryUiState(
    val isLoading: Boolean = false,
    val playlists: List<PlaylistUiModel> = emptyList(),
    val errorMessage: String? = null,
    val selectedTab: LibraryTab = LibraryTab.PLAYLISTS,
)

/**
 * Kullanıcıdan gelen niyetler. UI yalnızca bu tipleri yayımlar; iş mantığını çalıştırmaz.
 */
sealed interface LibraryIntent {

    /** Ekran ilk açıldığında ya da yenileme istendiğinde yayımlanır. */
    data object LoadPlaylists : LibraryIntent

    /** Üst sekme çubuğunda sekme değiştirildiğinde yayımlanır. */
    data class TabSelected(val tab: LibraryTab) : LibraryIntent

    /** Playlist kartına dokunulduğunda yayımlanır; detay navigasyonunu tetikler. */
    data class PlaylistClicked(val playlistId: String) : LibraryIntent

    /** Hata durumunda "Tekrar dene" butonuna basıldığında yayımlanır. */
    data object RetryClicked : LibraryIntent
}

/**
 * Tek seferlik (one-shot) olaylar: navigasyon, snackbar vb.
 * State içinde tutulmaz; konfigürasyon değişiminde tekrar tetiklenmez.
 */
sealed interface LibraryEffect {

    /** Playlist'e tıklandığında detay ekranına geçiş sinyali. */
    data class NavigateToPlaylistDetail(val playlistId: String) : LibraryEffect

    /** Kurtarılamayan hata mesajı; Snackbar ile gösterilir. */
    data class ShowError(val message: String) : LibraryEffect
}
