package com.turkcell.lyraapp.data.favorites

import com.turkcell.lyraapp.data.songs.SongDto

interface FavoritesRepository {
    suspend fun getFavorites(): Result<List<SongDto>>
}
