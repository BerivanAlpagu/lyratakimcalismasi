package com.turkcell.lyraapp

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.turkcell.lyraapp.data.player.GlobalPlayerManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LyraMediaService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    @Inject
    lateinit var globalPlayerManager: GlobalPlayerManager

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        try {
            // Reflection ile projedeki mevcut oynatıcıyı çekiyoruz
            val playerField = GlobalPlayerManager::class.java.getDeclaredField("player")
            playerField.isAccessible = true
            val sharedExoPlayer = playerField.get(globalPlayerManager) as ExoPlayer

            // Doğrudan sisteme bağlıyoruz, Media3 varsayılan bildirim kanalını kendi açar
            mediaSession = MediaSession.Builder(this, sharedExoPlayer).build()
        } catch (e: Exception) {
            e.printStackTrace()
            val fallbackPlayer = ExoPlayer.Builder(this).build()
            mediaSession = MediaSession.Builder(this, fallbackPlayer).build()
        }

        // Ekstra hiçbir provider veya kanal ismi dayatması yapmıyoruz!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}