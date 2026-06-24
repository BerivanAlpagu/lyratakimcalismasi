package com.turkcell.lyraapp.data.library

import com.turkcell.lyraapp.data.playlists.PlaylistDto
import com.turkcell.lyraapp.data.playlists.PlaylistWithSongsDto

/**
 * Kütüphane özelliğinin veri sözleşmesi.
 *
 * Bu arayüz yalnızca domain/UI katmanının ihtiyaç duyduğu operasyonları tanımlar;
 * ağ, veritabanı veya önbellek gibi altyapı detayları buraya sızmaz.
 *
 * Referans: docs/decisions.md — Library Ekranı.
 */
interface LibraryRepository {

    /**
     * Kullanıcının çalma listelerini getirir.
     *
     * Başarı durumunda [Result.success] içinde liste, ağ/ayrıştırma hatasında
     * [Result.failure] içinde istisna döner. Boş liste geçerli bir başarı durumudur.
     */
    suspend fun getPlaylists(): Result<List<PlaylistDto>>

    /**
     * Oturumu açık olan kullanıcının kendi çalma listelerini getirir.
     */
    suspend fun getMyPlaylists(): Result<List<PlaylistDto>>

    /**
     * Kullanıcı için yeni bir çalma listesi oluşturur.
     */
    suspend fun createPlaylist(name: String, description: String?): Result<PlaylistDto>

    /**
     * Belirli bir çalma listesinin detayını ve içindeki şarkıları getirir.
     */
    suspend fun getPlaylistDetail(id: String): Result<PlaylistWithSongsDto>
}
