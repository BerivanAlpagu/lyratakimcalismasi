package com.turkcell.lyraapp.ui.library

import com.turkcell.lyraapp.data.playlists.PlaylistDto

/**
 * Kütüphane ekranının MVI sözleşmesi: State (durum), Intent (kullanıcı niyeti) ve
 * Effect (tek seferlik olay) tek dosyada toplanmıştır.
 *
 * Referans: [com.turkcell.lyraapp.ui.auth.login.LoginContract] (bkz. mvi-contracts.md).
 */

/**
 * Kütüphane sekmelerini temsil eder.
 *
 * ARTISTS ve ALBUMS sekmeleri bu fazda backend desteği olmadığından boş durum gösterir.
 */
enum class LibraryTab(val label: String) {
    PLAYLISTS("Calma listeleri"),
    ARTISTS("Sanatcilar"),
    ALBUMS("Albumler"),
}

/**
 * Ekranın gözlemlenebilir tüm durumu. Tek bir immutable kaynak (single source of truth).
 *
 * [isLoading] ağ isteği devam ederken `true`'dur.
 * [playlists] yalnızca [selectedTab] == [LibraryTab.PLAYLISTS] olduğunda anlamlıdır.
 * [errorMessage] `null` değilken hata banner'ı gösterilir.
 */
data class LibraryUiState(
    val isLoading: Boolean = false,
    val playlists: List<PlaylistDto> = emptyList(),
    val errorMessage: String? = null,
    val selectedTab: LibraryTab = LibraryTab.PLAYLISTS,
)

/**
 * Kullanıcıdan gelen niyetler. UI yalnızca bu tipleri yayımlar; iş mantığını çalıştırmaz.
 */
sealed interface LibraryIntent {

    /** Ekran ilk açıldığında ya da "Yenile" tıklandığında yayımlanır. */
    data object LoadPlaylists : LibraryIntent

    /** Üst sekme çubuğunda sekme değiştirildiğinde yayımlanır. */
    data class TabSelected(val tab: LibraryTab) : LibraryIntent

    /** Playlist kartına dokunulduğunda yayımlanır; detay navigasyonunu tetikler. */
    data class PlaylistClicked(val playlistId: String) : LibraryIntent

    /** Hata durumunda "Tekrar dene" butonuna basıldığında yayımlanır. */
    data object RetryClicked : LibraryIntent
}

/**
 * Tek seferlik (one-shot) olaylar: navigasyon, snackbar vb. State içinde tutulmaz,
 * böylece konfigürasyon değişiminde tekrar tetiklenmez.
 */
sealed interface LibraryEffect {

    /** Playlist'e tıklandığında detay ekranına geçiş sinyali. */
    data class NavigateToPlaylistDetail(val playlistId: String) : LibraryEffect

    /** Kurtarılamayan hata mesajı; Snackbar ile gösterilir. */
    data class ShowError(val message: String) : LibraryEffect
}
