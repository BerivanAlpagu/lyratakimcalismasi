package com.turkcell.lyraapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedSongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDownloadedSong(song: DownloadedSongEntity): Long

    @Query("DELETE FROM downloaded_songs WHERE songId = :songId")
    fun deleteDownloadedSong(songId: String): Int

    @Query("SELECT * FROM downloaded_songs WHERE songId = :songId")
    fun getDownloadedSongById(songId: String): DownloadedSongEntity?

    @Query("SELECT * FROM downloaded_songs ORDER BY downloadedAt DESC")
    fun getAllDownloadedSongs(): Flow<List<DownloadedSongEntity>>

    @Query("SELECT songId FROM downloaded_songs")
    fun getAllDownloadedSongIds(): Flow<List<String>>
}
