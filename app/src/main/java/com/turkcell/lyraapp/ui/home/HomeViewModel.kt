package com.turkcell.lyraapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.home.HomeRepository
import com.turkcell.lyraapp.data.home.HomeSong
import com.turkcell.lyraapp.data.local.FavoritesStore
import com.turkcell.lyraapp.data.local.SettingsStore
import com.turkcell.lyraapp.data.player.GlobalPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val globalPlayerManager: GlobalPlayerManager,
    private val favoritesStore: FavoritesStore,
    private val settingsStore: SettingsStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(greeting = greetingForNow()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    init {
        loadFeed()

        // Observe global player state and favorite status
        viewModelScope.launch {
            combine(
                globalPlayerManager.playerState,
                favoritesStore.favoriteSongIds,
                settingsStore.isDarkMode
            ) { playerState, favoriteIds, isDarkMode ->
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        nowPlayingSong = playerState.songId?.let {
                            HomeSong(
                                id = it,
                                title = playerState.title,
                                artist = playerState.artist,
                                artworkStartColor = playerState.artworkStartColor,
                                artworkEndColor = playerState.artworkEndColor
                            )
                        },
                        isPlaying = playerState.isPlaying,
                        isFavorite = playerState.songId?.let { favoriteIds.contains(it) } ?: false,
                        isDarkMode = isDarkMode ?: true
                    )
                }
            }.collect {}
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Retry -> loadFeed()
            is HomeIntent.TogglePlayPause -> {
                globalPlayerManager.togglePlayPause()
            }
            is HomeIntent.SongSelected -> viewModelScope.launch {
                globalPlayerManager.playSong(
                    songId = intent.song.id,
                    title = intent.song.title,
                    artist = intent.song.artist,
                    artworkStartColor = intent.song.artworkStartColor,
                    artworkEndColor = intent.song.artworkEndColor
                )
                _effect.send(
                    HomeEffect.NavigateToPlayer(
                        songId = intent.song.id,
                        title = intent.song.title,
                        artist = intent.song.artist,
                    ),
                )
            }
            is HomeIntent.ToggleFavorite -> viewModelScope.launch {
                val currentSong = _uiState.value.nowPlayingSong
                if (currentSong != null) {
                    val currentState = globalPlayerManager.playerState.value
                    favoritesStore.toggleFavorite(
                        com.turkcell.lyraapp.data.local.FavoriteSong(
                            id = currentSong.id,
                            title = currentSong.title,
                            artist = currentSong.artist,
                            duration = formatSongDuration(currentState.durationMs),
                            durationMs = currentState.durationMs,
                            artworkStartColor = currentSong.artworkStartColor,
                            artworkEndColor = currentSong.artworkEndColor
                        )
                    )
                }
            }
            is HomeIntent.SkipNext -> {
                globalPlayerManager.playNext()
            }
            is HomeIntent.ToggleTheme -> {
                viewModelScope.launch {
                    val currentIsDark = _uiState.value.isDarkMode
                    settingsStore.setDarkMode(!currentIsDark)
                }
            }
        }
    }

    private fun formatSongDuration(ms: Long): String {
        val totalSeconds = (ms.coerceAtLeast(0L)) / 1000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return "%d:%02d".format(minutes, seconds)
    }

    private fun loadFeed() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = homeRepository.getHomeFeed()
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { feed ->
                    _uiState.update {
                        it.copy(
                            userInitials = feed.userInitials,
                            songs = feed.songs,
                            quickPicks = feed.quickPicks,
                            recentlyPlayed = feed.recentlyPlayed,
                            playlistsForYou = feed.playlistsForYou,
                        )
                    }
                }
                .onFailure { error ->
                    _effect.send(HomeEffect.ShowError(error.message ?: "Ana sayfa yüklenemedi."))
                }
        }
    }

    // java.time yerine Calendar: minSdk 24'te desugaring gerektirmez.
    private fun greetingForNow(): String =
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Günaydın"
            in 12..17 -> "İyi günler"
            else -> "İyi akşamlar"
        }
}
