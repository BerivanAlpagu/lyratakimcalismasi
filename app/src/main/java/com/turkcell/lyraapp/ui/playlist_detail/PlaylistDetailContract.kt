package com.turkcell.lyraapp.ui.playlist_detail

import com.turkcell.lyraapp.data.playlists.PlaylistWithSongsDto

/**
 * Çalma Listesi Detay Ekranının MVI Sözleşmesi.
 */
data class PlaylistDetailUiState(
    val playlist: PlaylistWithSongsDto? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface PlaylistDetailIntent {
    /** Detay verilerini yükleme isteği. */
    data class LoadDetail(val id: String) : PlaylistDetailIntent

    /** Hata durumunda yeniden deneme isteği. */
    data object RetryClicked : PlaylistDetailIntent

    /** Geri tuşuna basıldığında. */
    data object BackClicked : PlaylistDetailIntent

    /** Bir şarkıya tıklanıp oynatılmak istendiğinde. */
    data class SongClicked(val songId: String, val title: String, val artist: String) : PlaylistDetailIntent
}

sealed interface PlaylistDetailEffect {
    /** Önceki ekrana geri dönme olayı. */
    data object NavigateBack : PlaylistDetailEffect

    /** Oynatıcı ekranına yönlendirme olayı. */
    data class NavigateToPlayer(val songId: String, val title: String, val artist: String) : PlaylistDetailEffect

    /** Hata mesajı gösterme olayı. */
    data class ShowError(val message: String) : PlaylistDetailEffect
}
