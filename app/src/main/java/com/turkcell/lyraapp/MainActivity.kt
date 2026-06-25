package com.turkcell.lyraapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.turkcell.lyraapp.ui.navigation.LyraNavHost
import com.turkcell.lyraapp.ui.theme.LyraAppTheme
import com.turkcell.lyraapp.data.local.SettingsStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsStore: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkPref by settingsStore.isDarkMode.collectAsState(initial = null)
            val isSystemDark = isSystemInDarkTheme()
            val isDark = isDarkPref ?: isSystemDark

            LyraAppTheme(darkTheme = isDark) {
                LyraNavHost()
            }
        }
    }
}
