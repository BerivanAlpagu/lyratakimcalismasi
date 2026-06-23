package com.turkcell.lyraapp.ui.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.playlists.PlaylistDto

// ── Playlist cover renk paleti — her playlist hash'e göre bir gradient alır ──
private val coverGradients: List<List<Color>> = listOf(
    listOf(Color(0xFFFFB1C8), Color(0xFF7B2949)),  // pembeli gül
    listOf(Color(0xFF9B59B6), Color(0xFF3B1F6B)),  // mor
    listOf(Color(0xFF5B8EF5), Color(0xFF1A3A8F)),  // mavi
    listOf(Color(0xFF2ECC71), Color(0xFF0E6640)),  // yeşil
    listOf(Color(0xFFE67E22), Color(0xFF7D3C0D)),  // turuncu
    listOf(Color(0xFF1ABC9C), Color(0xFF0B5345)),  // turkuaz
    listOf(Color(0xFFEC407A), Color(0xFF880E4F)),  // fuşya
    listOf(Color(0xFF78909C), Color(0xFF263238)),  // çelik mavi
)

private fun gradientForId(id: String): List<Color> {
    val index = Math.abs(id.hashCode()) % coverGradients.size
    return coverGradients[index]
}

// ─────────────────────────────────────────────────────────────────────────────
// Route (durumlu / stateful) — MVI köprüsü
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Kütüphane ekranının durumlu giriş noktası.
 *
 * ViewModel'i [hiltViewModel] ile alır, state'i [collectAsStateWithLifecycle] ile toplar
 * ve Effect'leri [LaunchedEffect] içinde tüketir. İş mantığı veya repository çağrısı içermez.
 */
@Composable
fun LibraryRoute(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Ekran ilk açıldığında playlist'leri yükle
    LaunchedEffect(Unit) {
        viewModel.onIntent(LibraryIntent.LoadPlaylists)
    }

    // Tek seferlik Effect'leri tüket
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LibraryEffect.ShowError ->
                    snackbarHostState.showSnackbar(effect.message)
                is LibraryEffect.NavigateToPlaylistDetail ->
                    // Detay ekranı ilerleyen fazda bağlanacak; şimdilik bilgilendirme gösterir.
                    snackbarHostState.showSnackbar("Playlist: ${effect.playlistId}")
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

// ─────────────────────────────────────────────────────────────────────────────
// Screen (durumsuz / stateless) — yalnızca çizim ve Intent yayımı
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Kütüphane ekranının durumsuz Composable'ı.
 *
 * Preview edilebilir; [state] ve [onIntent] dışında hiçbir bağımlılık almaz.
 */
@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onIntent: (LibraryIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
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
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            LibraryTopBar()

            LibraryTabRow(
                selectedTab = state.selectedTab,
                onTabSelected = { onIntent(LibraryIntent.TabSelected(it)) },
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                state.isLoading -> LibraryLoadingContent()
                state.errorMessage != null -> LibraryErrorContent(
                    message = state.errorMessage,
                    onRetry = { onIntent(LibraryIntent.RetryClicked) },
                )
                state.selectedTab == LibraryTab.PLAYLISTS && state.playlists.isEmpty() ->
                    LibraryEmptyContent(message = "Henuz playlist yok.")
                state.selectedTab != LibraryTab.PLAYLISTS ->
                    LibraryEmptyContent(message = "Yakin zamanda geliyor.")
                else -> LibraryPlaylistList(
                    playlists = state.playlists,
                    onPlaylistClicked = { onIntent(LibraryIntent.PlaylistClicked(it)) },
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Alt bileşenler
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LibraryTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Kutüphane",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row {
            IconButton(onClick = { /* Arama — kapsam dışı */ }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Ara",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun LibraryTabRow(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LibraryTab.entries.forEach { tab ->
            LibraryTabChip(
                label = tab.label,
                isSelected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
            )
        }
    }
}

@Composable
private fun LibraryTabChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(durationMillis = 200),
        label = "tabChipBackground",
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "tabChipText",
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun LibraryPlaylistList(
    playlists: List<PlaylistDto>,
    onPlaylistClicked: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items(items = playlists, key = { it.id }) { playlist ->
            PlaylistListItem(
                playlist = playlist,
                onClick = { onPlaylistClicked(playlist.id) },
            )
        }
    }
}

@Composable
private fun PlaylistListItem(
    playlist: PlaylistDto,
    onClick: () -> Unit,
) {
    val gradientColors = gradientForId(playlist.id)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Playlist cover — gradient + nota ikonu
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.linearGradient(colors = gradientColors),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(24.dp),
            )
        }

        // Playlist bilgileri
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Calma listesi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LibraryLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun LibraryErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRetry) {
                Text(text = "Tekrar dene")
            }
        }
    }
}

@Composable
private fun LibraryEmptyContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
