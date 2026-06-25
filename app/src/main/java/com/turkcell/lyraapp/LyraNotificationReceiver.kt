package com.turkcell.lyraapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.turkcell.lyraapp.data.player.GlobalPlayerManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Bildirim butonlarına basıldığında çalışan BroadcastReceiver.
 * Play/Pause, Next, Previous ve Favorite aksiyonlarını GlobalPlayerManager üzerinden yönetir.
 */
@AndroidEntryPoint
class LyraNotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var globalPlayerManager: GlobalPlayerManager

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PLAY_PAUSE -> globalPlayerManager.togglePlayPause()
            ACTION_NEXT      -> globalPlayerManager.skipToNext()
            ACTION_PREVIOUS  -> globalPlayerManager.skipToPrevious()
            ACTION_FAVORITE  -> globalPlayerManager.toggleFavorite()
        }
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.turkcell.lyraapp.PLAY_PAUSE"
        const val ACTION_NEXT       = "com.turkcell.lyraapp.NEXT"
        const val ACTION_PREVIOUS   = "com.turkcell.lyraapp.PREVIOUS"
        const val ACTION_FAVORITE   = "com.turkcell.lyraapp.FAVORITE"
    }
}
