package com.turkcell.lyraapp.data.player

import com.turkcell.lyraapp.data.songs.SongsApi
import javax.inject.Inject

/**
 * [PlayerRepository]'nin gerçek API implementasyonu.
 *
 * [SongsApi.getStreamUrl] çağrısını sarmalar; ağ/HTTP hatalarını [runCatching] ile
 * [Result.failure]'a çevirir, böylece ViewModel hata akışını tek noktadan yönetir.
 */
class DefaultPlayerRepository @Inject constructor(
    private val songsApi: SongsApi,
) : PlayerRepository {

    override suspend fun getStreamUrl(songId: String): Result<String> = runCatching {
        songsApi.getStreamUrl(songId).data.url
    }
}
