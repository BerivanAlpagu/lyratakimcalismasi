package com.turkcell.lyraapp.data.playlists

import com.turkcell.lyraapp.data.songs.SongDto
import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/playlists` yanıtının zarfı; asıl liste [data] içindedir.
 *
 * API her zaman songs alanı olmayan özet nesneler döner; detay için
 * bkz. [PlaylistWithSongsResponseDto].
 */
@Serializable
data class PlaylistsResponseDto(
    val data: List<PlaylistDto> = emptyList(),
)

/**
 * OpenAPI `Playlist` şemasının istemci tarafı karşılığı.
 *
 * Liste görünümünde yalnızca [id] ve [name] zorunludur; [description] ve
 * [createdAt] varsa gösterilir. Bilinmeyen alanlar `Json { ignoreUnknownKeys }`
 * ile yok sayılır.
 */
@Serializable
data class PlaylistDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: String? = null,
    val ownerId: String? = null,
)

/**
 * `GET /api/v1/playlists/{id}` yanıtının zarfı; yük [data] içindedir.
 */
@Serializable
data class PlaylistWithSongsResponseDto(
    val data: PlaylistWithSongsDto,
)

/**
 * OpenAPI `PlaylistWithSongs` şemasının istemci tarafı karşılığı.
 *
 * [songs] şarkıları parça sırasında içerir. [SongDto] tipi `data/songs`
 * paketinden yeniden kullanılır; ayrı tanım yapılmaz (bkz. agents.md §2.2).
 */
@Serializable
data class PlaylistWithSongsDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: String? = null,
    val ownerId: String? = null,
    val songs: List<SongDto> = emptyList(),
)
