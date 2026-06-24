package com.turkcell.lyraapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.home.HomeRepository
import com.turkcell.lyraapp.data.home.HomeSong
import com.turkcell.lyraapp.data.player.GlobalPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val globalPlayerManager: GlobalPlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(greeting = greetingForNow()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    init {
        loadFeed()

        // Observe global player state
        viewModelScope.launch {
            globalPlayerManager.playerState.collect { playerState ->
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
                        isPlaying = playerState.isPlaying
                    )
                }
            }
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
        }
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
