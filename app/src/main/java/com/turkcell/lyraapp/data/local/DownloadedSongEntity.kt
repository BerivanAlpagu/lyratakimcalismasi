package com.turkcell.lyraapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_songs")
data class DownloadedSongEntity(
    @PrimaryKey
    val songId: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val localFilePath: String,
    val downloadedAt: Long
)
