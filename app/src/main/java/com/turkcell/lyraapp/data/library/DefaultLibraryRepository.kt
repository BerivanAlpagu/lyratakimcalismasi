package com.turkcell.lyraapp.data.library

import com.turkcell.lyraapp.data.playlists.PlaylistDto
import com.turkcell.lyraapp.data.playlists.PlaylistsApi
import com.turkcell.lyraapp.data.playlists.PlaylistWithSongsDto
import javax.inject.Inject

/**
 * [LibraryRepository] arayüzünün Retrofit tabanlı gerçek implementasyonu.
 *
 * Tüm ağ çağrıları [runCatching] ile sarılır; [PlaylistsApi] exception fırlattığında
 * [Result.Failure]'a dönüştürülür. ViewModel hiçbir zaman try/catch yazmak zorunda kalmaz.
 *
 * [PlaylistsApi] bağımlılığı [di.LibraryModule] tarafından sağlanır.
 */
class DefaultLibraryRepository @Inject constructor(
    private val api: PlaylistsApi,
) : LibraryRepository {

    /**
     * `GET /api/v1/playlists` — şarkısız playlist listesi.
     */
    override suspend fun getPlaylists(): Result<List<PlaylistDto>> =
        runCatching { api.getPlaylists().data }

    /**
     * `GET /api/v1/playlists/{id}` — şarkılı playlist detayı.
     */
    override suspend fun getPlaylistDetail(playlistId: String): Result<PlaylistWithSongsDto> =
        runCatching { api.getPlaylistDetail(playlistId).data }
}
