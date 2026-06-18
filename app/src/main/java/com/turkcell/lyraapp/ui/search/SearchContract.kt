package com.turkcell.lyraapp.ui.search

import com.turkcell.lyraapp.data.songs.SongDto

/**
 * Arama ekranının MVI sözleşmesi.
 */
data class SearchUiState(
    val query: String = "",
    val selectedFilter: String = "Hepsi",
    val isLoading: Boolean = false,
    val searchResults: List<SongDto> = emptyList(),
)

sealed interface SearchIntent {
    data class QueryChanged(val value: String) : SearchIntent
    data class FilterSelected(val filter: String) : SearchIntent
    data class SongSelected(val song: SongDto) : SearchIntent
}

sealed interface SearchEffect {
    data class NavigateToPlayer(val songId: String, val title: String, val artist: String) : SearchEffect
    data class ShowError(val message: String) : SearchEffect
}