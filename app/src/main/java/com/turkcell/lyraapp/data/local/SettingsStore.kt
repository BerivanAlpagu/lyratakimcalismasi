package com.turkcell.lyraapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

@Singleton
class SettingsStore @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    val isDarkMode: Flow<Boolean?> = context.settingsDataStore.data.map { it[Keys.IS_DARK_MODE] }

    suspend fun setDarkMode(isDark: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.IS_DARK_MODE] = isDark
        }
    }
}
