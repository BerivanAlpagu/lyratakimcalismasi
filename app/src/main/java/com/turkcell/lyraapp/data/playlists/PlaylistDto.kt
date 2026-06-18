package com.turkcell.lyraapp.data.playlists

import com.turkcell.lyraapp.data.songs.SongDto
import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/playlists` yanıtının zarfı; [data] playlist listesini taşır.
 *
 * OpenAPI şeması: `components/schemas/Playlist` listesi.
 */
@Serializable
data class PlaylistsPageDto(
    val data: List<PlaylistDto> = emptyList(),
)

/**
 * Tekil playlist'in API gösterimi (şarkısız).
 *
 * OpenAPI `Playlist` şemasının istemci tarafı karşılığı.
 * [description] ve [createdAt] opsiyoneldir; `Json { ignoreUnknownKeys }` ile
 * bilinmeyen alanlar yok sayılır.
 */
@Serializable
data class PlaylistDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: String? = null,
)

/**
 * `GET /api/v1/playlists/{id}` yanıtının zarfı.
 *
 * OpenAPI şeması: `PlaylistWithSongs`.
 */
@Serializable
data class PlaylistDetailEnvelopeDto(
    val data: PlaylistWithSongsDto,
)

/**
 * Şarkı listesi dahil playlist detayı.
 *
 * OpenAPI `PlaylistWithSongs` şemasının istemci tarafı karşılığı.
 * `allOf` yapısı Kotlin'de düz bir sınıfla modellenir; tekrar eden alanlar inline edilir.
 * [songs] sunucu tarafından track sırasına göre sıralanmış gelir.
 */
@Serializable
data class PlaylistWithSongsDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: String? = null,
    val songs: List<SongDto> = emptyList(),
)
