package com.turkcell.lyraapp.data.library

import com.turkcell.lyraapp.data.playlists.PlaylistDto
import com.turkcell.lyraapp.data.playlists.PlaylistWithSongsDto

/**
 * Kütüphane (Library) veri katmanı sözleşmesi.
 *
 * ViewModel bu interface'e bağımlıdır; gerçek implementasyon [DefaultLibraryRepository]'dir.
 * İleride backend değiştiğinde yalnızca implementasyon ve [di.LibraryModule]'deki bağlama
 * hedefi güncellenir — ViewModel ve Contract değişmez (bkz. mvi-overview.md §6).
 *
 * Her suspend fonksiyon [Result] döndürür: ağ/serileştirme hataları exception olarak değil
 * [Result.Failure] olarak yüzeye çıkar; çağıranın try/catch'e ihtiyacı olmaz.
 */
interface LibraryRepository {

    /**
     * Tüm playlist'leri şarkısız olarak getirir.
     *
     * Backend: `GET /api/v1/playlists`
     */
    suspend fun getPlaylists(): Result<List<PlaylistDto>>

    /**
     * Belirtilen [playlistId]'ye sahip playlist'i şarkılarıyla birlikte getirir.
     *
     * Backend: `GET /api/v1/playlists/{id}`
     */
    suspend fun getPlaylistDetail(playlistId: String): Result<PlaylistWithSongsDto>
}
