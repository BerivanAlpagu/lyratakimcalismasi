package com.turkcell.lyraapp.ui.playlist_detail

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.songs.SongDto
import com.turkcell.lyraapp.ui.icons.LyraIcons

@Composable
fun PlaylistDetailRoute(
    onNavigateBack: () -> Unit,
    onSongClick: (songId: String, title: String, artist: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PlaylistDetailEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is PlaylistDetailEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is PlaylistDetailEffect.NavigateToPlayer -> {
                    onSongClick(effect.songId, effect.title, effect.artist)
                }
            }
        }
    }

    PlaylistDetailScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
fun PlaylistDetailScreen(
    state: PlaylistDetailUiState,
    onIntent: (PlaylistDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = !state.isLoading && state.playlist != null,
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(200))
            ) {
                state.playlist?.let { playlist ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top Navigation bar inside scrollable list
                        item {
                            DetailTopBar(
                                onBackClick = { onIntent(PlaylistDetailIntent.BackClicked) },
                                onDeletePlaylist = { onIntent(PlaylistDetailIntent.DeletePlaylistClicked) }
                            )
                        }

                        // Header / Artwork
                        item {
                            PlaylistHeader(
                                playlistId = playlist.id,
                                name = playlist.name,
                                description = playlist.description,
                                songCount = playlist.songs.size,
                                totalDurationMs = playlist.songs.sumOf { it.durationMs ?: 0L }
                            )
                        }

                        // Action buttons
                        item {
                            ActionRow(
                                onPlayClick = {
                                    if (playlist.songs.isNotEmpty()) {
                                        val first = playlist.songs.first()
                                        onIntent(PlaylistDetailIntent.SongClicked(first.id, first.title, first.artist))
                                    }
                                },
                                onAddClick = {
                                    onIntent(PlaylistDetailIntent.AddSongClicked)
                                }
                            )
                        }

                        // Song list
                        itemsIndexed(
                            items = playlist.songs,
                            key = { _, song -> song.id }
                        ) { index, song ->
                            SongItem(
                                index = index + 1,
                                song = song,
                                isFirst = index == 0,
                                isLast = index == playlist.songs.lastIndex,
                                onClick = {
                                    onIntent(PlaylistDetailIntent.SongClicked(song.id, song.title, song.artist))
                                },
                                onRemoveSong = {
                                    onIntent(PlaylistDetailIntent.RemoveSongClicked(song.id))
                                },
                                onMoveUp = {
                                    onIntent(PlaylistDetailIntent.ReorderSongs(index, index - 1))
                                },
                                onMoveDown = {
                                    onIntent(PlaylistDetailIntent.ReorderSongs(index, index + 1))
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }

            if (state.isAddSongDialogVisible) {
                AddSongDialog(
                    songs = state.allSongs.filter { song ->
                        state.playlist?.songs?.none { it.id == song.id } ?: true
                    },
                    isLoading = state.isLoadingSongs,
                    onDismiss = { onIntent(PlaylistDetailIntent.DismissAddSongDialog) },
                    onSongSelected = { songId ->
                        onIntent(PlaylistDetailIntent.ConfirmAddSong(songId))
                    }
                )
            }

            if (state.errorMessage != null && !state.isLoading && state.playlist == null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { onIntent(PlaylistDetailIntent.RetryClicked) }) {
                        Icon(
                            imageVector = LyraIcons.Restart,
                            contentDescription = "Yeniden Dene",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTopBar(
    onBackClick: () -> Unit,
    onDeletePlaylist: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = LyraIcons.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = LyraIcons.MoreVert,
                    contentDescription = "Daha Fazla",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Çalma Listesini Sil") },
                    onClick = {
                        menuExpanded = false
                        onDeletePlaylist()
                    }
                )
            }
        }
    }
}

@Composable
private fun PlaylistHeader(
    playlistId: String,
    name: String,
    description: String?,
    songCount: Int,
    totalDurationMs: Long
) {
    val (startColor, endColor) = deterministicGradient(playlistId)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large Gradient Artwork
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(listOf(startColor, endColor))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = LyraIcons.LibraryMusic,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Playlist Title
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Description
        Text(
            text = description ?: "Kullanıcı çalma listesi",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Meta Info (e.g. LyraApp · 5 şarkı · 25 dk)
        val formattedTime = formatPlaylistDuration(totalDurationMs)
        Text(
            text = "LyraApp · $songCount şarkı · $formattedTime",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ActionRow(
    onPlayClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { }) {
            Icon(
                imageVector = LyraIcons.FavoriteOutlined,
                contentDescription = "Beğen",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = { }) {
            Icon(
                imageVector = LyraIcons.Download,
                contentDescription = "İndir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = onAddClick) {
            Icon(
                imageVector = LyraIcons.Add,
                contentDescription = "Ekle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { }) {
            Icon(
                imageVector = LyraIcons.Restart, // Shuffle placeholder
                contentDescription = "Karıştır",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        // Theme Primary Play Button
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary) 
                .clickable(onClick = onPlayClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = LyraIcons.Play,
                contentDescription = "Oynat",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun SongItem(
    index: Int,
    song: SongDto,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onRemoveSong: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reorder handle indicator
        Text(
            text = "☰",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.padding(end = 8.dp)
        )
        // Thumbnail or index
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.width(28.dp)
        )
        // Song artwork
        SongArtwork(songId = song.id)
        Spacer(modifier = Modifier.width(16.dp))
        // Title and artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // Duration
        Text(
            text = formatSongDuration(song.durationMs ?: 0L),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        IconButton(onClick = { }) {
            Icon(
                imageVector = LyraIcons.FavoriteOutlined,
                contentDescription = "Beğen",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = LyraIcons.MoreVert,
                    contentDescription = "Seçenekler",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Çalma Listesinden Kaldır") },
                    onClick = {
                        menuExpanded = false
                        onRemoveSong()
                    }
                )
                if (!isFirst) {
                    DropdownMenuItem(
                        text = { Text("Yukarı Taşı") },
                        onClick = {
                            menuExpanded = false
                            onMoveUp()
                        }
                    )
                }
                if (!isLast) {
                    DropdownMenuItem(
                        text = { Text("Aşağı Taşı") },
                        onClick = {
                            menuExpanded = false
                            onMoveDown()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SongArtwork(
    songId: String,
    modifier: Modifier = Modifier
) {
    val (startColor, endColor) = deterministicGradient(songId)

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                brush = Brush.linearGradient(listOf(startColor, endColor))
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = LyraIcons.Play,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun deterministicGradient(id: String): Pair<Color, Color> {
    val colors = listOf(
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.primaryContainer
    )
    val index = (id.hashCode() and 0x7FFFFFFF) % colors.size
    return colors[index]
}

private fun formatSongDuration(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L)) / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}

private fun formatPlaylistDuration(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L)) / 1000L
    val totalMinutes = totalSeconds / 60L
    return if (totalMinutes < 60L) {
        "$totalMinutes dakika"
    } else {
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        "$hours saat $minutes dakika"
    }
}

@Composable
private fun AddSongDialog(
    songs: List<SongDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSongSelected: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Çalma Listesine Şarkı Ekle",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (songs.isEmpty()) {
                    Text(
                        text = "Eklenebilecek şarkı bulunamadı.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(songs) { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSongSelected(song.id) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SongArtwork(songId = song.id)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = LyraIcons.Add,
                                    contentDescription = "Ekle",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}
