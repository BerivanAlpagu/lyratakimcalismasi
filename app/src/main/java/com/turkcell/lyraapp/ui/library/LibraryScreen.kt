package com.turkcell.lyraapp.ui.library

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Library akışının durumlu (stateful) giriş noktası.
 *
 * [LibraryViewModel]'i Hilt'ten alır, durumu yaşam döngüsüne duyarlı şekilde toplar ve
 * tek seferlik [LibraryEffect]'leri tüketir. UI ile iş mantığı arasındaki tek köprü burasıdır.
 *
 * Navigasyon lambda'ları NavHost'tan sağlanır; ViewModel navigasyon API'si bilmez —
 * bkz. docs/architecture/mvi-viewmodel-rules.md §6.
 */
@Composable
fun LibraryRoute(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LibraryEffect.ShowError ->
                    snackbarHostState.showSnackbar(effect.message)

                is LibraryEffect.NavigateToPlaylistDetail -> {
                    // Sonraki fazda: navController.navigate(playlistDetailRoute(effect.playlistId))
                    snackbarHostState.showSnackbar("Playlist: ${effect.playlistId}")
                }
            }
        }
    }

    LibraryScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Kütüphane ekranı.
 *
 * Tamamen durumsuzdur (stateless): durumu [state] üzerinden alır, kullanıcı etkileşimlerini
 * [onIntent] ile yukarı yayımlar. İş mantığı veya state sahipliği bu katmanda bulunmaz.
 */
@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onIntent: (LibraryIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
        ) {
            LibraryTopBar()
            LibraryTabRow()
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = state.isLoading,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300)),
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                AnimatedVisibility(
                    visible = !state.isLoading && state.playlists.isNotEmpty(),
                    enter = fadeIn(tween(400)),
                    exit = fadeOut(tween(200)),
                ) {
                    PlaylistList(
                        playlists = state.playlists,
                        onPlaylistClick = { onIntent(LibraryIntent.PlaylistClicked(it)) },
                    )
                }

                AnimatedVisibility(
                    visible = !state.isLoading && state.playlists.isEmpty() && state.errorMessage == null,
                    enter = fadeIn(tween(400)),
                    exit = fadeOut(tween(200)),
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    EmptyState()
                }

                if (state.errorMessage != null && !state.isLoading) {
                    ErrorState(
                        onRetry = { onIntent(LibraryIntent.RetryClicked) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Kütüphane",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = LyraIcons.Search,
                contentDescription = "Ara",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { },
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = LyraIcons.Waveform,
                contentDescription = "Ekle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun LibraryTabRow() {
    val tabs = listOf("Çalma listeleri", "Sanatçılar", "Albümler")
    var selectedTab by remember { mutableIntStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEachIndexed { index, label ->
            FilterChip(
                selected = selectedTab == index,
                onClick = { selectedTab = index },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun PlaylistList(
    playlists: List<PlaylistUiModel>,
    onPlaylistClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(items = playlists, key = { it.id }) { playlist ->
            PlaylistItem(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist.id) },
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun PlaylistItem(
    playlist: PlaylistUiModel,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaylistArtwork(playlistId = playlist.id)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = playlist.description ?: "Çalma listesi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Icon(
            imageVector = LyraIcons.Waveform,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp),
        )
    }
}

/**
 * Çalma listesi için renkli gradient arka planlı kare görsel.
 *
 * API kapak resmi sağlamaz; [playlistId]'den deterministik renk çifti türetilir
 * (aynı playlist her zaman aynı rengi alır — bkz. decisions.md Şarkı Listesi kararı).
 */
@Composable
private fun PlaylistArtwork(
    playlistId: String,
    modifier: Modifier = Modifier,
) {
    val (startColor, endColor) = remember(playlistId) { deterministicGradient(playlistId) }

    Box(
        modifier = modifier
            .size(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                brush = Brush.linearGradient(listOf(startColor, endColor))
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = LyraIcons.LibraryMusic,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(22.dp),
        )
    }
}

/**
 * [playlistId] string'inden 0-360 arası ton değeri türetir ve iki uyumlu HSL rengi döndürür.
 * Saf rastgele yerine deterministik: aynı id her zaman aynı rengi verir.
 */
private fun deterministicGradient(id: String): Pair<Color, Color> {
    val hue = ((id.hashCode() and 0x7FFFFFFF) % 360).toFloat()
    val start = Color.hsl(hue, saturation = 0.60f, lightness = 0.42f)
    val end = Color.hsl((hue + 40f) % 360f, saturation = 0.50f, lightness = 0.30f)
    return start to end
}

@Composable
private fun EmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = LyraIcons.LibraryMusicOutlined,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = "Henüz çalma listesi yok",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Yüklenirken bir hata oluştu",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onRetry) {
            Text(text = "Tekrar dene")
        }
    }
}

@Preview(name = "Library - Dolu Liste", showBackground = true, showSystemUi = true)
@Composable
private fun LibraryScreenLoadedPreview() {
    LyraAppTheme(darkTheme = true) {
        LibraryScreen(
            state = LibraryUiState(
                playlists = listOf(
                    PlaylistUiModel("p_1", "Beğenilen Şarkılar", "Çalma listesi · 5 şarkı"),
                    PlaylistUiModel("p_2", "Gece Sürüşü", "Çalma listesi · 6 şarkı"),
                    PlaylistUiModel("p_3", "Sabah Kahvesi", "Çalma listesi · 5 şarkı"),
                    PlaylistUiModel("p_4", "Odaklan", null),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Library - Yükleniyor", showBackground = true, showSystemUi = true)
@Composable
private fun LibraryScreenLoadingPreview() {
    LyraAppTheme(darkTheme = true) {
        LibraryScreen(
            state = LibraryUiState(isLoading = true),
            onIntent = {},
        )
    }
}
