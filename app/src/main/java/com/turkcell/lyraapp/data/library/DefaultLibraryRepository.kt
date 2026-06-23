package com.turkcell.lyraapp.data.library

import com.turkcell.lyraapp.data.playlists.PlaylistDto
import com.turkcell.lyraapp.data.playlists.PlaylistsApi
import javax.inject.Inject

/**
 * [LibraryRepository] arayüzünün gerçek ağ implementasyonu.
 *
 * Tek bağımlılığı [PlaylistsApi]'dir; [com.turkcell.lyraapp.data.network.NetworkModule]
 * tarafından sağlanır. Tüm ağ hataları [runCatching] ile yakalanıp [Result.failure]'a
 * dönüştürülür; böylece ViewModel'e ham istisna sızmaz.
 *
 * DI bağlaması: [com.turkcell.lyraapp.di.LibraryModule].
 */
class DefaultLibraryRepository @Inject constructor(
    private val playlistsApi: PlaylistsApi,
) : LibraryRepository {

    /**
     * `GET /api/v1/playlists` çağrısını yapar ve sonucu [Result] ile sarar.
     *
     * Ağ erişilemez, zaman aşımı veya JSON ayrıştırma hatası olursa
     * [Result.failure] döner; ViewModel hata mesajını kullanıcıya iletir.
     */
    override suspend fun getPlaylists(): Result<List<PlaylistDto>> =
        runCatching {
            playlistsApi.getPlaylists().data
        }
}
