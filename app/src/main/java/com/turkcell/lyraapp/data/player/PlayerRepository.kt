package com.turkcell.lyraapp.data.player

/**
 * Oynatma için imzalı stream URL'si sağlayan veri kaynağı soyutlaması.
 *
 * ExoPlayer'a verilecek URL backend tarafından kısa ömürlü (signed) üretildiğinden,
 * oynatmadan hemen önce alınır (bkz. docs/api/openapi.json — /songs/{id}/stream-url).
 */
interface PlayerRepository {

    /** [songId] için ExoPlayer'a verilebilecek imzalı stream URL'sini döndürür. (Premium Only) */
    suspend fun getStreamUrl(songId: String): Result<String>

    /** Free/Premium fark etmeksizin sıradaki oynatılacak içeriği (Şarkı veya Reklam+Şarkı) döndürür. */
    suspend fun getPlaybackNext(songId: String): Result<com.turkcell.lyraapp.data.me.PlaybackItemDto>

    /** Reklam gösterimi tamamlandığında çağrılır. */
    suspend fun completeAd(impressionId: String): Result<Boolean>
}
