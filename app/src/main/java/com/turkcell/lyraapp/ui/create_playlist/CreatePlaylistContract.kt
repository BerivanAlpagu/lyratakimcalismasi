package com.turkcell.lyraapp.ui.create_playlist

/**
 * Yeni Çalma Listesi Oluşturma ekranının MVI sözleşmesi.
 *
 * Referans: docs/architecture/mvi-contracts.md.
 */

/**
 * Şarkı listesi öğesinin UI katmanına ait temsili.
 */
data class SongUiModel(
    val id: String,
    val title: String,
    val artist: String,
)

/**
 * Ekranın gözlemlenebilir tüm durumu.
 */
data class CreatePlaylistUiState(
    val playlistName: String = "",
    val playlistDescription: String = "",
    val isPublic: Boolean = false,
    val allSongs: List<SongUiModel> = emptyList(),
    val selectedSongIds: Set<String> = emptySet(),
    val isLoadingSongs: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * Kullanıcıdan gelen niyetler.
 */
sealed interface CreatePlaylistIntent {

    /** Çalma listesi adı değiştiğinde. */
    data class NameChanged(val name: String) : CreatePlaylistIntent

    /** Açıklama değiştiğinde. */
    data class DescriptionChanged(val description: String) : CreatePlaylistIntent

    /** Herkese açık toggle değiştiğinde. */
    data object TogglePublic : CreatePlaylistIntent

    /** Şarkı seçimi/seçim kaldırma. */
    data class ToggleSongSelection(val songId: String) : CreatePlaylistIntent

    /** Kaydet butonuna tıklandığında. */
    data object SaveClicked : CreatePlaylistIntent

    /** Kapatma (X) butonuna tıklandığında. */
    data object CloseClicked : CreatePlaylistIntent
}

/**
 * Tek seferlik olaylar.
 */
sealed interface CreatePlaylistEffect {

    /** Ekrandan geri dönüş. */
    data object NavigateBack : CreatePlaylistEffect

    /** Hata mesajı gösterimi. */
    data class ShowError(val message: String) : CreatePlaylistEffect

    /** Çalma listesi başarıyla oluşturuldu. */
    data object PlaylistCreatedSuccessfully : CreatePlaylistEffect
}
