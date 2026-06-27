package com.turkcell.lyraapp.ui.favorites

data class FavoriteSongUiModel(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val durationMs: Long
)

data class FavoritesUiState(
    val favorites: List<FavoriteSongUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val downloadedSongIds: Set<String> = emptySet(),
    val downloadingSongIds: Set<String> = emptySet()
)

sealed interface FavoritesIntent {
    data object LoadFavorites : FavoritesIntent
    data object DownloadFavoritesClicked : FavoritesIntent
}

sealed interface FavoritesEffect {
    data class ShowError(val message: String) : FavoritesEffect
}
