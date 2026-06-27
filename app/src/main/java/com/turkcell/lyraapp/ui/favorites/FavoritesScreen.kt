package com.turkcell.lyraapp.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.turkcell.lyraapp.ui.icons.LyraIcons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FavoritesRoute(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onShowSnackbar: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is FavoritesEffect.ShowError -> onShowSnackbar(effect.message)
            }
        }
    }

    FavoritesScreen(
        uiState = uiState,
        onIntent = viewModel::handleIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onIntent: (FavoritesIntent) -> Unit
) {
    // Tasarıma özel renkler
    val darkBackground = Color(0xFF120C10)
    val onDarkText = Color.White
    val onDarkTextSecondary = Color(0xFFAAA6AA)
    val brandPink = Color(0xFFFFAFD2)
    val buttonSurface = Color(0xFF282025)

    Scaffold(
        containerColor = darkBackground,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Back Navigation */ }) {
                        Icon(
                            imageVector = LyraIcons.ArrowBack,
                            contentDescription = "Geri",
                            tint = onDarkText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = darkBackground,
                    navigationIconContentColor = onDarkText
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = brandPink,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.error != null && uiState.favorites.isEmpty()) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp) // Bottom bar offset
                ) {
                    item {
                        val isDownloaded = uiState.favorites.isNotEmpty() && 
                                          uiState.favorites.all { uiState.downloadedSongIds.contains(it.id) }
                        val isDownloading = uiState.favorites.any { uiState.downloadingSongIds.contains(it.id) }
                        FavoritesHeader(
                            onDarkText = onDarkText,
                            onDarkTextSecondary = onDarkTextSecondary,
                            brandPink = brandPink,
                            buttonSurface = buttonSurface,
                            songCount = uiState.favorites.size,
                            isEmpty = uiState.favorites.isEmpty(),
                            totalDurationMs = uiState.favorites.sumOf { it.durationMs },
                            isDownloaded = isDownloaded,
                            isDownloading = isDownloading,
                            onDownloadClick = { onIntent(FavoritesIntent.DownloadFavoritesClicked) }
                        )
                    }

                    items(uiState.favorites) { song ->
                        FavoriteSongItem(
                            song = song,
                            onDarkText = onDarkText,
                            onDarkTextSecondary = onDarkTextSecondary,
                            brandPink = brandPink
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesHeader(
    onDarkText: Color,
    onDarkTextSecondary: Color,
    brandPink: Color,
    buttonSurface: Color,
    songCount: Int,
    isEmpty: Boolean,
    totalDurationMs: Long,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    onDownloadClick: () -> Unit,
) {
    
    fun formatPlaylistDuration(ms: Long): String {
        if (ms == 0L) return "0 dk"
        val totalSeconds = ms / 1000L
        val totalMinutes = totalSeconds / 60L
        return if (totalMinutes < 60L) {
            "$totalMinutes dk"
        } else {
            val hours = totalMinutes / 60L
            val minutes = totalMinutes % 60L
            "${hours}s ${minutes}d"
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Heart Gradient Box
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFAFD2), Color(0xFFFFDAB9))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LyraIcons.Favorite,
                    contentDescription = null,
                    tint = Color(0xFF4A102A),
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = if (isEmpty) "Favoriler" else "Beğenilen\nŞarkılar",
                    color = onDarkText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isEmpty) "$songCount şarkı" else "$songCount şarkı · ${formatPlaylistDuration(totalDurationMs)}",
                    color = onDarkTextSecondary,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandPink),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = LyraIcons.Play,
                    contentDescription = "Çal",
                    tint = Color(0xFF4A102A)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Çal", color = Color(0xFF4A102A), fontWeight = FontWeight.Bold)
            }
            
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(buttonSurface)
            ) {
                Icon(
                    imageVector = LyraIcons.Shuffle,
                    contentDescription = "Karıştır",
                    tint = onDarkTextSecondary
                )
            }
            
            IconButton(
                onClick = onDownloadClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(buttonSurface)
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = brandPink,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = LyraIcons.Download,
                        contentDescription = "İndir",
                        tint = if (isDownloaded) brandPink else onDarkTextSecondary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FavoriteSongItem(
    song: FavoriteSongUiModel,
    onDarkText: Color,
    onDarkTextSecondary: Color,
    brandPink: Color
) {
    // Generate a pseudo-random color based on song ID for the cover art
    val colorHash = song.id.hashCode()
    val r = (colorHash and 0xFF) / 255f
    val g = ((colorHash shr 8) and 0xFF) / 255f
    val b = ((colorHash shr 16) and 0xFF) / 255f
    val coverColor = Color(r, g, b).copy(alpha = 0.6f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cover Art
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(coverColor)
        )
        Spacer(modifier = Modifier.width(16.dp))
        
        // Texts
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = onDarkText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                color = onDarkTextSecondary,
                fontSize = 14.sp
            )
        }
        
        // Duration
        Text(
            text = song.duration,
            color = onDarkTextSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        
        // Heart Icon
        Icon(
            imageVector = LyraIcons.Favorite,
            contentDescription = "Favorilerden Çıkar",
            tint = brandPink,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        
        // More options
        Icon(
            imageVector = LyraIcons.MoreVert,
            contentDescription = "Daha fazla",
            tint = onDarkTextSecondary
        )
    }
}
