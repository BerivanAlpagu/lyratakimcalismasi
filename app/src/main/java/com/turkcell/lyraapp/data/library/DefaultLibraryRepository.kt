package com.turkcell.lyraapp.data.library

import com.turkcell.lyraapp.data.me.CreatePlaylistRequest
import com.turkcell.lyraapp.data.me.MeApi
import com.turkcell.lyraapp.data.playlists.PlaylistDto
import com.turkcell.lyraapp.data.playlists.PlaylistWithSongsDto
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
    private val meApi: MeApi,
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

    override suspend fun getMyPlaylists(): Result<List<PlaylistDto>> =
        runCatching {
            meApi.getMyPlaylists().data
        }

    override suspend fun createPlaylist(name: String, description: String?): Result<PlaylistDto> =
        runCatching {
            val request = CreatePlaylistRequest(name = name, description = description)
            meApi.createPlaylist(request).data
        }

    override suspend fun getPlaylistDetail(id: String): Result<PlaylistWithSongsDto> =
        runCatching {
            playlistsApi.getPlaylistById(id).data
        }
}
