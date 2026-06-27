package com.turkcell.lyraapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.turkcell.lyraapp.data.player.GlobalPlayerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject

/**
 * Arka planda müzik çalmayı ve sistem bildirim panelinde medya kontrolcüsünü
 * (Mini Player) sağlayan Media3 MediaSessionService.
 *
 * onStartCommand içinde hemen startForeground() çağrılır (Android 5 saniyelik kural).
 * playerState değiştikçe (isPlaying, isFavorite vb.) bildirim otomatik güncellenir.
 */
@AndroidEntryPoint
class LyraMediaService : MediaSessionService() {

    @Inject
    lateinit var globalPlayerManager: GlobalPlayerManager

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val mainHandler = Handler(Looper.getMainLooper())
    private var artworkBitmap: Bitmap? = null
    private var lastArtworkUrl: String? = null

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "lyra_music_channel"
        const val NOTIFICATION_ID = 1001
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, globalPlayerManager.player)
            .setSessionActivity(openAppIntent)
            .build()

        // playerState her değiştiğinde bildirimi güncelle
        globalPlayerManager.playerState
            .onEach { state ->
                val coverUrl = "https://picsum.photos/seed/${state.songId ?: "default"}/300/300"
                if (coverUrl != lastArtworkUrl) {
                    lastArtworkUrl = coverUrl
                    serviceScope.launch {
                        artworkBitmap = loadBitmap(coverUrl)
                        mainHandler.post { notifyUpdate() }
                    }
                } else {
                    mainHandler.post { notifyUpdate() }
                }
            }
            .launchIn(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Android: startForegroundService() sonrası 5 saniye içinde startForeground() zorunlu
        startForeground(NOTIFICATION_ID, buildNotification())
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun notifyUpdate() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val state = globalPlayerManager.playerState.value

        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseIcon  = if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val playPauseLabel = if (state.isPlaying) "Duraklat" else "Çal"
        val favoriteIcon   = if (state.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(state.title.ifBlank { "LyraApp" })
            .setContentText(state.artist.ifBlank { "Müzik çalıyor..." })
            .setLargeIcon(artworkBitmap)
            .setContentIntent(openAppIntent)
            .setOngoing(state.isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(favoriteIcon,               "Favori",  broadcastIntent(LyraNotificationReceiver.ACTION_FAVORITE,  4))
            .addAction(R.drawable.ic_skip_previous,"Önceki",  broadcastIntent(LyraNotificationReceiver.ACTION_PREVIOUS,  2))
            .addAction(playPauseIcon, playPauseLabel,          broadcastIntent(LyraNotificationReceiver.ACTION_PLAY_PAUSE,1))
            .addAction(R.drawable.ic_skip_next,   "Sonraki",  broadcastIntent(LyraNotificationReceiver.ACTION_NEXT,      3))
            .build()
    }

    private fun broadcastIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(action).apply { setPackage(packageName) }
        return PendingIntent.getBroadcast(
            this, requestCode, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun loadBitmap(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 3000
            connection.readTimeout    = 3000
            connection.connect()
            BitmapFactory.decodeStream(connection.getInputStream())
        } catch (e: Exception) {
            null
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Müzik Çalıcı",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "LyraApp müzik çalar bildirimi"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
