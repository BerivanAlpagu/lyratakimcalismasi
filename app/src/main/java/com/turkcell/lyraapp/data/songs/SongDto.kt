package com.turkcell.lyraapp.data.songs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/songs` yanıtının tek sayfasını temsil eder (cursor pagination).
 *
 * [nextCursor] bir sonraki sayfayı çekmek için kullanılır; sayfa tükendiğinde `null` döner.
 * Bu fazda yalnızca ilk sayfa kullanılır (bkz. docs/decisions.md — Şarkı Listesi API Entegrasyonu).
 */
@Serializable
data class SongsPageDto(
    val data: List<SongDto> = emptyList(),
    val nextCursor: String? = null,
)

/**
 * OpenAPI `Song` şemasının istemci tarafı karşılığı.
 *
 * Listeleme için yalnızca [id], [title] ve [artist] zorunludur; diğer alanlar (album, süre vb.)
 * API tarafından gönderilse de bu fazda kullanılmaz. Bilinmeyen alanlar `Json { ignoreUnknownKeys }`
 * ile yok sayılır, bu nedenle şemanın tamamı modellenmez.
 */
@Serializable
data class SongDto(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val durationMs: Long? = null,
)

/**
 * `GET /api/v1/songs/{id}/stream-url` yanıtının zarfı; yük [data] içindedir.
 */
@Serializable
data class StreamUrlEnvelope(
    val data: StreamUrlDto,
)

/**
 * İmzalı stream URL yükü. Yalnızca [url] oynatma için zorunludur; [expiresAt] ve [mimeType]
 * bilgilendirme amaçlıdır (bu fazda kullanılmaz).
 */
@Serializable
data class StreamUrlDto(
    val url: String,
    val expiresAt: String? = null,
    val mimeType: String? = null,
)
