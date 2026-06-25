package com.turkcell.lyraapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.favoritesDataStore by preferencesDataStore(name = "favorites_prefs")

@Singleton
class FavoritesStore @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val FAVORITE_SONGS = stringSetPreferencesKey("favorite_songs")
    }

    val favoriteSongs: Flow<List<FavoriteSong>> = context.favoritesDataStore.data.map { prefs ->
        val jsonSet = prefs[Keys.FAVORITE_SONGS] ?: emptySet()
        jsonSet.mapNotNull { 
            try { Json.decodeFromString<FavoriteSong>(it) } catch (e: Exception) { null } 
        }.sortedBy { it.title }
    }

    val favoriteSongIds: Flow<Set<String>> = favoriteSongs.map { songs ->
        songs.map { it.id }.toSet()
    }

    suspend fun addFavorite(song: FavoriteSong) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_SONGS] ?: emptySet()
            // Remove existing song with same ID if any, then add new one
            val filtered = current.filterNot { it.contains("\"id\":\"${song.id}\"") }
            prefs[Keys.FAVORITE_SONGS] = filtered.toSet() + Json.encodeToString(song)
        }
    }

    suspend fun removeFavorite(songId: String) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_SONGS] ?: emptySet()
            prefs[Keys.FAVORITE_SONGS] = current.filterNot { it.contains("\"id\":\"$songId\"") }.toSet()
        }
    }
    
    suspend fun toggleFavorite(song: FavoriteSong) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_SONGS] ?: emptySet()
            val isFavorite = current.any { it.contains("\"id\":\"${song.id}\"") }
            if (isFavorite) {
                prefs[Keys.FAVORITE_SONGS] = current.filterNot { it.contains("\"id\":\"${song.id}\"") }.toSet()
            } else {
                prefs[Keys.FAVORITE_SONGS] = current + Json.encodeToString(song)
            }
        }
    }
}
