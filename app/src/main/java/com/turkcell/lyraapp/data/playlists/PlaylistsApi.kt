package com.turkcell.lyraapp.data.playlists

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Playlist API uç noktaları.
 *
 * OpenAPI rotaları (bkz. docs/api/openapi.json):
 *  - `GET /api/v1/playlists`        → tüm playlist'leri listeler (şarkısız)
 *  - `GET /api/v1/playlists/{id}`   → tek playlist detayı (şarkılarıyla birlikte)
 *
 * Retrofit bu interface'i `NetworkModule` üzerinden sağlanan [retrofit2.Retrofit] instance'ı
 * ile oluşturur; [LibraryModule] içinde `@Provides` ile bağlanır.
 */
interface PlaylistsApi {

    /**
     * Tüm playlist'leri şarkısız olarak döndürür.
     *
     * Başarı durumunda [PlaylistsPageDto.data] dolmuş gelir; hata durumunda
     * Retrofit exception fırlatır — çağıran [runCatching] ile sarar.
     */
    @GET("api/v1/playlists")
    suspend fun getPlaylists(): PlaylistsPageDto

    /**
     * Verilen [id]'ye sahip playlist'i şarkılarıyla birlikte döndürür.
     *
     * 404 durumunda Retrofit `HttpException` fırlatır.
     */
    @GET("api/v1/playlists/{id}")
    suspend fun getPlaylistDetail(@Path("id") id: String): PlaylistDetailEnvelopeDto
}
