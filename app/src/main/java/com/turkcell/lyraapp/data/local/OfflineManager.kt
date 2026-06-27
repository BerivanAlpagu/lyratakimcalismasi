package com.turkcell.lyraapp.data.local

import android.content.Context
import android.net.Uri
import com.turkcell.lyraapp.data.songs.SongsApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songsApi: SongsApi,
    private val okHttpClient: OkHttpClient,
    private val downloadedSongDao: DownloadedSongDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val downloadsDir = File(context.filesDir, "downloads").apply {
        if (!exists()) {
            mkdirs()
        }
    }

    private val _downloadedSongIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadedSongIds: StateFlow<Set<String>> = _downloadedSongIds.asStateFlow()

    private val _downloadingSongIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingSongIds: StateFlow<Set<String>> = _downloadingSongIds.asStateFlow()

    init {
        scope.launch {
            downloadedSongDao.getAllDownloadedSongIds().collect { ids ->
                _downloadedSongIds.value = ids.toSet()
            }
        }
    }

    fun isSongDownloaded(songId: String): Boolean {
        return _downloadedSongIds.value.contains(songId)
    }

    fun getLocalFileUri(songId: String): String {
        val file = File(downloadsDir, "${songId}.wav")
        return Uri.fromFile(file).toString()
    }

    suspend fun downloadSong(
        songId: String,
        title: String,
        artist: String,
        durationMs: Long
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (isSongDownloaded(songId)) {
            return@withContext Result.success(Unit)
        }

        _downloadingSongIds.update { it + songId }
        try {
            // 1. Get stream URL
            val streamUrl = songsApi.getStreamUrl(songId).data.url
            
            // 2. Download bytes using OkHttpClient
            val request = Request.Builder().url(streamUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                if (response.code == 403) {
                    throw Exception("İndirme yalnızca Premium üyeler içindir.")
                } else {
                    throw Exception("Müzik dosyası indirilemedi (Hata: ${response.code}).")
                }
            }
            
            val body = response.body ?: throw Exception("Müzik dosyası içeriği boş.")
            
            // 3. Save file
            val tempFile = File(downloadsDir, "${songId}.wav.tmp")
            val targetFile = File(downloadsDir, "${songId}.wav")
            
            tempFile.outputStream().use { output ->
                body.byteStream().use { input ->
                    input.copyTo(output)
                }
            }
            
            if (tempFile.renameTo(targetFile)) {
                // 4. Save to Room database
                val entity = DownloadedSongEntity(
                    songId = songId,
                    title = title,
                    artist = artist,
                    durationMs = durationMs,
                    localFilePath = targetFile.absolutePath,
                    downloadedAt = System.currentTimeMillis()
                )
                downloadedSongDao.insertDownloadedSong(entity)
                Result.success(Unit)
            } else {
                tempFile.delete()
                throw Exception("Müzik dosyası kaydedilemedi.")
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _downloadingSongIds.update { it - songId }
        }
    }

    suspend fun deleteSong(songId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(downloadsDir, "${songId}.wav")
            if (file.exists()) {
                file.delete()
            }
            // Remove from database
            downloadedSongDao.deleteDownloadedSong(songId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
