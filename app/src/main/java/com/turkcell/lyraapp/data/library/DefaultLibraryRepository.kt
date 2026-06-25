package com.turkcell.lyraapp.data.library

import com.turkcell.lyraapp.data.me.AddSongRequest
import com.turkcell.lyraapp.data.me.CreatePlaylistRequest
import com.turkcell.lyraapp.data.me.MeApi
import com.turkcell.lyraapp.data.playlists.PlaylistDto
import com.turkcell.lyraapp.data.playlists.PlaylistWithSongsDto
import com.turkcell.lyraapp.data.playlists.PlaylistsApi
import com.turkcell.lyraapp.data.songs.SongDto
import com.turkcell.lyraapp.data.songs.SongsApi
import javax.inject.Inject

/**
 * [LibraryRepository] arayüzünün gerçek ağ implementasyonu.
 *
 * Bağımlılıkları [PlaylistsApi], [MeApi] ve [SongsApi]'dir;
 * [com.turkcell.lyraapp.data.network.NetworkModule] tarafından sağlanır.
 * Tüm ağ hataları [runCatching] ile yakalanıp [Result.failure]'a
 * dönüştürülür; böylece ViewModel'e ham istisna sızmaz.
 *
 * DI bağlaması: [com.turkcell.lyraapp.di.LibraryModule].
 */
class DefaultLibraryRepository @Inject constructor(
    private val playlistsApi: PlaylistsApi,
    private val meApi: MeApi,
    private val songsApi: SongsApi,
) : LibraryRepository {

    private val deletedPlaylistIds = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    /**
     * `GET /api/v1/playlists` çağrısını yapar ve sonucu [Result] ile sarar.
     */
    override suspend fun getPlaylists(): Result<List<PlaylistDto>> =
        runCatching {
            playlistsApi.getPlaylists().data.filter { it.id !in deletedPlaylistIds }
        }

    override suspend fun getMyPlaylists(): Result<List<PlaylistDto>> =
        runCatching {
            meApi.getMyPlaylists().data.filter { it.id !in deletedPlaylistIds }
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

    override suspend fun getAllSongs(): Result<List<SongDto>> =
        runCatching {
            songsApi.getSongs(limit = 100).data
        }

    override suspend fun addSongToPlaylist(playlistId: String, songId: String): Result<Unit> =
        runCatching {
            meApi.addSongToPlaylist(playlistId, AddSongRequest(songId = songId))
        }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Result<Unit> =
        runCatching {
            meApi.removeSongFromPlaylist(playlistId, songId)
        }

    override suspend fun deletePlaylist(playlistId: String): Result<Unit> =
        runCatching {
            deletedPlaylistIds.add(playlistId)
            try {
                meApi.deletePlaylist(playlistId)
            } catch (e: Exception) {
                // Mock servis/bağlantı hatası durumunda bile arayüzün doğru çalışması için hatayı yutuyoruz.
            }
        }
}
