package com.turkcell.lyraapp.data.player

interface PlayerRepository {

    suspend fun getStreamUrl(songId: String): Result<String>

    suspend fun resolveNextPlayback(songId: String): Result<PlaybackDecision>

    suspend fun completeAd(impressionId: String): Result<Boolean>
}

sealed interface PlaybackDecision {
    data class Song(
        val songId: String,
        val title: String,
        val artist: String,
        val streamUrl: String,
    ) : PlaybackDecision

    data class AdThenSong(
        val adId: String,
        val adTitle: String,
        val advertiser: String,
        val adStreamUrl: String,
        val impressionId: String,
        val songId: String,
        val title: String,
        val artist: String,
        val streamUrl: String,
    ) : PlaybackDecision
}
