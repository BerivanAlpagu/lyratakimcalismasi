package com.turkcell.lyraapp.data.home

/**
 * Ana sayfa beslemesinin (feed) tamamı: tek repository çağrısıyla dönen aggregate model.
 *
 * Kapak görselleri henüz bir CDN/görsel servisi olmadığından gradyan renk çifti
 * (`artworkStartColor`/`artworkEndColor`, ARGB hex) ile temsil edilir. Gerçek API
 * geldiğinde bu alanlar görsel URL'siyle değiştirilebilir; UI katmanı yalnızca
 * bu modeli çizer (bkz. docs/decisions.md — Ana Sayfa Veri Katmanı).
 */
data class HomeFeed(
    val userInitials: String,
    val songs: List<HomeSong>,
    val quickPicks: List<QuickPick>,
    val recentlyPlayed: List<RecentlyPlayed>,
    val playlistsForYou: List<PlaylistForYou>,
)

/**
 * "Şarkılar" bölümündeki, API'dan (`GET /api/v1/songs`) gelen şarkı.
 *
 * API'da kapak görseli/BG bilgisi olmadığından [artworkStartColor]/[artworkEndColor] gradyan
 * renk çifti (ARGB hex) şarkı [id]'sinden deterministik olarak türetilir; aynı şarkı her zaman
 * aynı rengi alır (bkz. docs/decisions.md — Şarkı Listesi API Entegrasyonu).
 */
data class HomeSong(
    val id: String,
    val title: String,
    val artist: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

/** "Ne dinlemek istersin?" grid'indeki hızlı seçim öğesi. */
data class QuickPick(
    val id: String,
    val title: String,
    val artist: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

/** "Son çalınanlar" bölümündeki öğe; [subtitle] sanatçı/albüm bilgisini taşır. */
data class RecentlyPlayed(
    val id: String,
    val title: String,
    val subtitle: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

/** "Senin için çalma listeleri" bölümündeki öğe. */
data class PlaylistForYou(
    val id: String,
    val title: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)
