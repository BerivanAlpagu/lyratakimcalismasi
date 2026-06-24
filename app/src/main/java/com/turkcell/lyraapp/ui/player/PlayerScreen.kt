package com.turkcell.lyraapp.ui.player

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.DarkPrimary
import com.turkcell.lyraapp.ui.theme.DarkSurface

// ─── Route (stateful) ────────────────────────────────────────────────────────

@Composable
fun PlayerRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlayerScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

// ─── Screen (stateless) ──────────────────────────────────────────────────────

@Composable
fun PlayerScreen(
    state: PlayerUiState,
    onIntent: (PlayerIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dominantColor = state.dominantColor ?: DarkSurface
    val animatedTop by animateColorAsState(
        targetValue = dominantColor.copy(alpha = 0.85f),
        animationSpec = tween(durationMillis = 600),
        label = "gradientTop",
    )

    val gradient = Brush.verticalGradient(
        colors = listOf(
            animatedTop,
            DarkSurface.copy(alpha = 0.95f),
            DarkSurface,
        ),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // ── Üst bar ──────────────────────────────────────────────────────
            Spacer(Modifier.height(52.dp))
            TopBar(
                playlistName = state.playlistName,
                onNavigateBack = onNavigateBack,
                onMoreClick = { /* TODO: bottom sheet */ },
            )

            Spacer(Modifier.height(32.dp))

            // ── Kapak fotoğrafı ───────────────────────────────────────────────
            CoverArt(
                coverUrl = state.coverUrl,
                onDominantColor = { color ->
                    onIntent(PlayerIntent.UpdateDominantColor(color))
                },
            )

            Spacer(Modifier.height(32.dp))

            // ── Şarkı adı + sanatçı + favori ─────────────────────────────────
            TrackInfo(
                title = state.title,
                artist = state.artist,
                isFavorite = state.isFavorite,
                onToggleFavorite = { onIntent(PlayerIntent.ToggleFavorite) },
            )

            Spacer(Modifier.height(20.dp))

            // ── Progress bar ──────────────────────────────────────────────────
            ProgressSection(
                positionMs = state.positionMs,
                durationMs = state.durationMs,
                onSeek = { ms -> onIntent(PlayerIntent.SeekTo(ms)) },
            )

            Spacer(Modifier.height(24.dp))

            // ── Oynatma kontrolleri ───────────────────────────────────────────
            PlaybackControls(
                isPlaying = state.isPlaying,
                isShuffling = state.isShuffling,
                repeatMode = state.repeatMode,
                canSkipPrevious = state.canSkipPrevious,
                canSkipNext = state.canSkipNext,
                onTogglePlayPause = { onIntent(PlayerIntent.TogglePlayPause) },
                onSkipPrevious = { onIntent(PlayerIntent.SkipPrevious) },
                onSkipNext = { onIntent(PlayerIntent.SkipNext) },
                onToggleShuffle = { onIntent(PlayerIntent.ToggleShuffle) },
                onToggleRepeat = { onIntent(PlayerIntent.ToggleRepeat) },
            )

            Spacer(Modifier.weight(1f))

            // ── Alt bar ───────────────────────────────────────────────────────
            BottomBar(
                onCastClick = { /* TODO */ },
                onBackgroundClick = { /* TODO */ },
                onQueueClick = { /* TODO */ },
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Üst bar ─────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(
    playlistName: String,
    onNavigateBack: () -> Unit,
    onMoreClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = LyraIcons.ChevronDown,
                contentDescription = "Kapat",
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ŞİMDİ ÇALIYOR",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 1.5.sp,
            )
            Text(
                text = playlistName.ifBlank { "Çalma Listesi" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = LyraIcons.MoreVert,
                contentDescription = "Daha fazla",
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// ─── Kapak fotoğrafı ─────────────────────────────────────────────────────────

@Composable
private fun CoverArt(
    coverUrl: String,
    onDominantColor: (Int?) -> Unit,
) {
    val context = LocalContext.current

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(coverUrl.ifBlank { null })
            .allowHardware(false)
            .crossfade(true)
            .build(),
        contentDescription = "Albüm kapağı",
        contentScale = ContentScale.Crop,
        onSuccess = { state ->
            val drawable = state.result.drawable
            val bitmap = (drawable as? BitmapDrawable)?.bitmap
            if (bitmap != null) {
                Palette.from(bitmap).generate { palette ->
                    onDominantColor(palette?.getDominantColor(DarkSurface.toArgb()))
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            // DÜZELTME: Gölgeyi kesilmemesi için kırpmadan (clip) ÖNCE uyguluyoruz.
            .shadow(elevation = 24.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
    )
}

// ─── Şarkı bilgisi ───────────────────────────────────────────────────────────

@Composable
private fun TrackInfo(
    title: String,
    artist: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title.ifBlank { "Bilinmeyen şarkı" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = artist.ifBlank { "Bilinmeyen sanatçı" },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) LyraIcons.Favorite else LyraIcons.FavoriteOutlined,
                contentDescription = if (isFavorite) "Favorilerden çıkar" else "Favorilere ekle",
                tint = if (isFavorite) DarkPrimary else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

// ─── Progress bar ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressSection(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val duration = durationMs.coerceAtLeast(0L)
    val positionFraction = if (duration > 0L) {
        (positionMs.toFloat() / duration).coerceIn(0f, 1f)
    } else 0f

    val sliderValue = if (isDragging) dragFraction else positionFraction
    val shownPositionMs = if (isDragging) (dragFraction * duration).toLong() else positionMs

    Column {
        Slider(
            value = sliderValue,
            onValueChange = {
                isDragging = true
                dragFraction = it
            },
            onValueChangeFinished = {
                onSeek((dragFraction * duration).toLong())
                isDragging = false
            },
            enabled = duration > 0L,
            colors = SliderDefaults.colors(
                thumbColor = DarkPrimary,
                activeTrackColor = DarkPrimary,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f),
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatTime(shownPositionMs),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

// ─── Oynatma kontrolleri ─────────────────────────────────────────────────────

@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    isShuffling: Boolean,
    repeatMode: Int,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    onTogglePlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Shuffle
        IconButton(onClick = onToggleShuffle, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = LyraIcons.Shuffle,
                contentDescription = "Karıştır",
                tint = if (isShuffling) DarkPrimary else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp),
            )
        }

        // Önceki Şarkı Butonu
        IconButton(onClick = onSkipPrevious, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = LyraIcons.SkipPrevious,
                contentDescription = "Önceki",
                // DÜZELTME: canSkipPrevious durumuna göre rengi söndürülür ama buton tıklanabilir kalır.
                tint = if (canSkipPrevious) Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(28.dp),
            )
        }

        // Oynat / Duraklat
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(color = DarkPrimary, shape = CircleShape)
                .shadow(elevation = 16.dp, shape = CircleShape, ambientColor = DarkPrimary),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.size(72.dp),
            ) {
                Icon(
                    imageVector = if (isPlaying) LyraIcons.Pause else LyraIcons.Play,
                    contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp),
                )
            }
        }

        // Sonraki Şarkı Butonu
        IconButton(onClick = onSkipNext, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = LyraIcons.SkipNext,
                contentDescription = "Sonraki",
                // DÜZELTME: canSkipNext durumuna göre rengi söndürülür ama talep doğrultusunda tıklanabilir kalır.
                tint = if (canSkipNext) Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(28.dp),
            )
        }

        // Repeat
        IconButton(onClick = onToggleRepeat, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = when (repeatMode) {
                    2 -> LyraIcons.RepeatOne
                    else -> LyraIcons.Repeat
                },
                contentDescription = "Tekrar",
                tint = if (repeatMode != 0) DarkPrimary else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// ─── Alt bar ─────────────────────────────────────────────────────────────────

@Composable
private fun BottomBar(
    onCastClick: () -> Unit,
    onBackgroundClick: () -> Unit,
    onQueueClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCastClick) {
            Icon(
                imageVector = LyraIcons.Cast,
                contentDescription = "Cihaza aktar",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            IconButton(onClick = onBackgroundClick) {
                Icon(
                    imageVector = LyraIcons.NotificationsOutlined,
                    contentDescription = "Arkaplan",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = "Arkaplan",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
            )
        }

        IconButton(onClick = onQueueClick) {
            Icon(
                imageVector = LyraIcons.QueueMusic,
                contentDescription = "Kuyruk",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

// ─── Yardımcı ────────────────────────────────────────────────────────────────

private fun formatTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0L) / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}