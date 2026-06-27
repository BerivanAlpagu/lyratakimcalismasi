package com.turkcell.lyraapp.data.player

import com.turkcell.lyraapp.data.songs.SongsApi
import javax.inject.Inject

/**
 * [PlayerRepository]'nin gerçek API implementasyonu.
 *
 * [SongsApi.getStreamUrl] çağrısını sarmalar; ağ/HTTP hatalarını [runCatching] ile
 * [Result.failure]'a çevirir, böylece ViewModel hata akışını tek noktadan yönetir.
 */
import com.turkcell.lyraapp.data.me.MeApi
import com.turkcell.lyraapp.data.me.PlaybackItemDto
import com.turkcell.lyraapp.data.me.PlaybackNextRequestDto
import com.turkcell.lyraapp.data.me.AdCompleteRequestDto

class DefaultPlayerRepository @Inject constructor(
    private val songsApi: SongsApi,
    private val meApi: MeApi
) : PlayerRepository {

    override suspend fun getStreamUrl(songId: String): Result<String> = runCatching {
        songsApi.getStreamUrl(songId).data.url
    }

    override suspend fun getPlaybackNext(songId: String): Result<PlaybackItemDto> = runCatching {
        meApi.getPlaybackNext(PlaybackNextRequestDto(songId)).data
    }

    override suspend fun completeAd(impressionId: String): Result<Boolean> = runCatching {
        meApi.completeAd(AdCompleteRequestDto(impressionId)).data.completed
    }
}
