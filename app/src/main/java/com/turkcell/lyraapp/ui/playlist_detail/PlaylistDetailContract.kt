package com.turkcell.lyraapp.ui.playlist_detail

import com.turkcell.lyraapp.data.playlists.PlaylistWithSongsDto
import com.turkcell.lyraapp.data.songs.SongDto

/**
 * Çalma Listesi Detay Ekranının MVI Sözleşmesi.
 */
data class PlaylistDetailUiState(
    val playlist: PlaylistWithSongsDto? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val allSongs: List<SongDto> = emptyList(),
    val isAddSongDialogVisible: Boolean = false,
    val isLoadingSongs: Boolean = false,
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

    /** Playlist'e şarkı ekleme butonuna basıldığında. */
    data object AddSongClicked : PlaylistDetailIntent

    /** Şarkı ekleme diyaloğu kapatıldığında. */
    data object DismissAddSongDialog : PlaylistDetailIntent

    /** Diyalogdan bir şarkı seçildiğinde. */
    data class ConfirmAddSong(val songId: String) : PlaylistDetailIntent

    /** Çalma listesini tamamen silme isteği. */
    data object DeletePlaylistClicked : PlaylistDetailIntent

    /** Çalma listesinden şarkı çıkarma isteği. */
    data class RemoveSongClicked(val songId: String) : PlaylistDetailIntent

    /** Şarkıların sırasını sürükleyerek değiştirme isteği. */
    data class ReorderSongs(val fromIndex: Int, val toIndex: Int) : PlaylistDetailIntent
}

sealed interface PlaylistDetailEffect {
    /** Önceki ekrana geri dönme olayı. */
    data object NavigateBack : PlaylistDetailEffect

    /** Oynatıcı ekranına yönlendirme olayı. */
    data class NavigateToPlayer(val songId: String, val title: String, val artist: String) : PlaylistDetailEffect

    /** Hata mesajı gösterme olayı. */
    data class ShowError(val message: String) : PlaylistDetailEffect
}
