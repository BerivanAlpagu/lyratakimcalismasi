package com.turkcell.lyraapp.data.playlists

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Streaming API'nin çalma listesi uç noktaları için Retrofit arayüzü.
 *
 * Base URL [com.turkcell.lyraapp.data.network.NetworkModule] tarafından sağlanır; buradaki
 * yollar ona görelidir. API üretimi [com.turkcell.lyraapp.data.network.NetworkModule.providePlaylists api]
 * üzerinden yapılır.
 *
 * Karar geçmişi için bkz. docs/decisions.md — Library Ekranı.
 */
interface PlaylistsApi {

    /**
     * Tüm çalma listelerini özet biçimde (şarkısız) döndürür.
     *
     * Yanıt: `{ data: Playlist[] }` — bkz. OpenAPI `GET /api/v1/playlists`.
     */
    @GET("api/v1/playlists")
    suspend fun getPlaylists(): PlaylistsResponseDto

    /**
     * Belirtilen çalma listesini şarkı sırasıyla birlikte döndürür.
     *
     * Yanıt: `{ data: PlaylistWithSongs }` — bkz. OpenAPI `GET /api/v1/playlists/{id}`.
     *
     * @param id Çalma listesi kimliği (örn. `p_late-night-drive`).
     */
    @GET("api/v1/playlists/{id}")
    suspend fun getPlaylistById(@Path("id") id: String): PlaylistWithSongsResponseDto
}
