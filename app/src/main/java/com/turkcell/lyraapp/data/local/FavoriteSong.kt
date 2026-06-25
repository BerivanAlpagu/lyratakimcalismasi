package com.turkcell.lyraapp.data.local

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteSong(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val durationMs: Long = 0L,
    val artworkStartColor: Long = 0xFF4A102A,
    val artworkEndColor: Long = 0xFFFFAFD2
)
