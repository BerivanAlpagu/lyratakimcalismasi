package com.turkcell.lyraapp.data.player

/**
 * Oynatma için imzalı stream URL'si sağlayan veri kaynağı soyutlaması.
 *
 * ExoPlayer'a verilecek URL backend tarafından kısa ömürlü (signed) üretildiğinden,
 * oynatmadan hemen önce alınır (bkz. docs/api/openapi.json — /songs/{id}/stream-url).
 */
interface PlayerRepository {

    /** [songId] için ExoPlayer'a verilebilecek imzalı stream URL'sini döndürür. */
    suspend fun getStreamUrl(songId: String): Result<String>
}
