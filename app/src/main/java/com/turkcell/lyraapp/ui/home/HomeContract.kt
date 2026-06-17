package com.turkcell.lyraapp.ui.home

import com.turkcell.lyraapp.data.home.HomeSong
import com.turkcell.lyraapp.data.home.PlaylistForYou
import com.turkcell.lyraapp.data.home.QuickPick
import com.turkcell.lyraapp.data.home.RecentlyPlayed

/**
 * Home ekranının MVI sözleşmesi: UiState + Intent + Effect (bkz. mvi-contracts.md).
 *
 * Şarkı satırına tıklama oynatıcıya yönlendirir ([HomeIntent.SongSelected] →
 * [HomeEffect.NavigateToPlayer]); diğer kartlar ve "Tümü" hâlâ davranışsızdır.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val greeting: String = "",
    val userInitials: String = "",
    val songs: List<HomeSong> = emptyList(),
    val quickPicks: List<QuickPick> = emptyList(),
    val recentlyPlayed: List<RecentlyPlayed> = emptyList(),
    val playlistsForYou: List<PlaylistForYou> = emptyList(),
)

sealed interface HomeIntent {
    /** Besleme yüklemesi başarısız olduğunda kullanıcı yeniden dener. */
    data object Retry : HomeIntent

    /** Kullanıcı bir şarkı satırına tıkladı; oynatıcıya yönlendirilecek. */
    data class SongSelected(val song: HomeSong) : HomeIntent
}

sealed interface HomeEffect {
    data class ShowError(val message: String) : HomeEffect

    /** Seçilen şarkı için oynatıcı ekranına git. */
    data class NavigateToPlayer(
        val songId: String,
        val title: String,
        val artist: String,
    ) : HomeEffect
}
