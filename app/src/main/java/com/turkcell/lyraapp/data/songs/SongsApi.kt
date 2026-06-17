package com.turkcell.lyraapp.data.songs

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Streaming API'nin şarkı uç noktaları için Retrofit arayüzü.
 *
 * Base URL [com.turkcell.lyraapp.data.network.NetworkModule] tarafından sağlanır; buradaki
 * yollar ona görelidir.
 */
interface SongsApi {

    /**
     * Şarkı kataloğunun bir sayfasını döndürür (cursor pagination + arama).
     *
     * @param limit Sayfa boyutu (1..100, varsayılan API tarafında 20).
     * @param cursor Önceki yanıttan gelen `nextCursor`; ilk sayfa için `null`.
     * @param query Başlık/sanatçı/albüm üzerinde büyük-küçük harf duyarsız arama.
     */
    @GET("api/v1/songs")
    suspend fun getSongs(
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null,
        @Query("q") query: String? = null,
    ): SongsPageDto

    /**
     * Bir şarkı için kısa ömürlü, imzalı stream URL'si üretir (TTL ~300sn).
     *
     * Dönen [StreamUrlEnvelope.data] içindeki `url` doğrudan ExoPlayer'a verilir; Range
     * isteklerini desteklediğinden seek (ilerletme/geri alma) çalışır. URL listeyle birlikte
     * önbelleğe alınmamalı, oynatmadan hemen önce alınmalıdır (bkz. openapi.json /stream-url).
     */
    @GET("api/v1/songs/{id}/stream-url")
    suspend fun getStreamUrl(@Path("id") id: String): StreamUrlEnvelope
}
