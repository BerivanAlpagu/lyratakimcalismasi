package com.turkcell.lyraapp.ui.player

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons

/**
 * Oynatıcının durumlu (stateful) giriş noktası.
 *
 * [PlayerViewModel]'i Hilt'ten alır, durumu yaşam döngüsüne duyarlı toplar ve geri navigasyonu
 * [onNavigateBack] ile köprüler (ViewModel navigasyon bilmez).
 */
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

/**
 * Oynatıcı ekranı (durumsuz). Tasarım sade tutulmuştur; amaç tüm ExoPlayer kontrollerini
 * (oynat/duraklat, baştan başlat, 10sn ileri/geri ve çubuktan konuma atlama) sunmaktır.
 */
@Composable
fun PlayerScreen(
    state: PlayerUiState,
    onIntent: (PlayerIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Üst bar: geri butonu
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = LyraIcons.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Başlık + sanatçı
            Text(
                text = state.title.ifBlank { "Bilinmeyen şarkı" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = state.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(32.dp))

            when {
                state.errorMessage != null -> ErrorContent(
                    message = state.errorMessage,
                    onRetry = { onIntent(PlayerIntent.Retry) },
                )

                else -> PlaybackControls(state = state, onIntent = onIntent)
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun PlaybackControls(
    state: PlayerUiState,
    onIntent: (PlayerIntent) -> Unit,
) {
    // Sürükleme sırasında çubuk, yoklamayla çakışmasın diye yerel değer kullanılır.
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val duration = state.durationMs.coerceAtLeast(0L)
    val positionFraction = if (duration > 0L) {
        (state.positionMs.toFloat() / duration).coerceIn(0f, 1f)
    } else {
        0f
    }
    val sliderValue = if (isDragging) dragFraction else positionFraction

    Slider(
        value = sliderValue,
        onValueChange = {
            isDragging = true
            dragFraction = it
        },
        onValueChangeFinished = {
            onIntent(PlayerIntent.SeekTo((dragFraction * duration).toLong()))
            isDragging = false
        },
        enabled = duration > 0L,
        modifier = Modifier.fillMaxWidth(),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        val shownPosition = if (isDragging) (dragFraction * duration).toLong() else state.positionMs
        Text(
            text = formatTime(shownPosition),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = formatTime(duration),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Spacer(Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Baştan başlat
        ControlButton(
            icon = LyraIcons.Restart,
            contentDescription = "Baştan başlat",
            onClick = { onIntent(PlayerIntent.Restart) },
        )
        Spacer(Modifier.size(8.dp))
        // 10sn geri
        ControlButton(
            icon = LyraIcons.Rewind10,
            contentDescription = "10 saniye geri",
            onClick = { onIntent(PlayerIntent.SeekBackward) },
        )
        Spacer(Modifier.size(8.dp))

        // Oynat / Duraklat (orta, büyük) — yükleme/tamponlamada spinner
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (state.isLoading || state.isBuffering) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
            } else {
                IconButton(
                    onClick = { onIntent(PlayerIntent.TogglePlayPause) },
                    modifier = Modifier.size(72.dp),
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) LyraIcons.Pause else LyraIcons.Play,
                        contentDescription = if (state.isPlaying) "Duraklat" else "Oynat",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp),
                    )
                }
            }
        }

        Spacer(Modifier.size(8.dp))
        // 10sn ileri
        ControlButton(
            icon = LyraIcons.Forward10,
            contentDescription = "10 saniye ileri",
            onClick = { onIntent(PlayerIntent.SeekForward) },
        )
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRetry) {
            Text(text = "Tekrar dene")
        }
    }
}

/** Milisaniyeyi "d:ss" biçiminde gösterir (negatif/0 değerler "0:00"). */
private fun formatTime(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L)) / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
