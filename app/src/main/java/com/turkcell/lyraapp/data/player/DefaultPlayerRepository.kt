package com.turkcell.lyraapp.data.player

import com.turkcell.lyraapp.data.songs.SongsApi
import com.turkcell.lyraapp.data.me.AdCompleteRequest
import com.turkcell.lyraapp.data.me.MeApi
import javax.inject.Inject

/**
 * [PlayerRepository]'nin gerçek API implementasyonu.
 *
 * [SongsApi.getStreamUrl] çağrısını sarmalar; ağ/HTTP hatalarını [runCatching] ile
 * [Result.failure]'a çevirir, böylece ViewModel hata akışını tek noktadan yönetir.
 */
class DefaultPlayerRepository @Inject constructor(
    private val songsApi: SongsApi,
    private val meApi: MeApi,
) : PlayerRepository {

    override suspend fun getStreamUrl(songId: String): Result<String> = runCatching {
        songsApi.getStreamUrl(songId).data.url
    }

    override suspend fun resolveNextPlayback(songId: String): Result<PlaybackDecision> = runCatching {
        val data = meApi.resolveNextPlayback(com.turkcell.lyraapp.data.me.PlaybackNextRequest(songId)).data
        if (data.type == "ad") {
            val ad = checkNotNull(data.ad) { "Ad payload missing." }
            val adStream = checkNotNull(data.adStream) { "Ad stream missing." }
            val impressionId = checkNotNull(data.impressionId) { "Ad impression missing." }
            PlaybackDecision.AdThenSong(
                adId = ad.id,
                adTitle = ad.title,
                advertiser = ad.advertiser,
                adStreamUrl = adStream.url,
                impressionId = impressionId,
                songId = data.song.id,
                title = data.song.title,
                artist = data.song.artist,
                streamUrl = data.stream.url,
            )
        } else {
            PlaybackDecision.Song(
                songId = data.song.id,
                title = data.song.title,
                artist = data.song.artist,
                streamUrl = data.stream.url,
            )
        }
    }

    override suspend fun completeAd(impressionId: String): Result<Boolean> = runCatching {
        meApi.completeAd(AdCompleteRequest(impressionId)).data.completed
    }
}
