package com.turkcell.lyraapp.data.favorites

import com.turkcell.lyraapp.data.songs.SongDto
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeFavoritesRepository @Inject constructor() : FavoritesRepository {
    override suspend fun getFavorites(): Result<List<SongDto>> {
        delay(800)
        val mockFavorites = listOf(
            SongDto(id = "s_1", title = "Gece Yarısı", artist = "Mavi Deniz", album = "3:34"),
            SongDto(id = "s_2", title = "Yıldız Tozu", artist = "Polaris", album = "4:07"),
            SongDto(id = "s_3", title = "İlk Işık", artist = "Sabah Ezgisi", album = "3:25"),
            SongDto(id = "s_4", title = "Neon Sokaklar", artist = "Şehir Işıkları", album = "3:43"),
            SongDto(id = "s_5", title = "Derin Mavi", artist = "Okyanus", album = "4:29")
        )
        return Result.success(mockFavorites)
    }
}
