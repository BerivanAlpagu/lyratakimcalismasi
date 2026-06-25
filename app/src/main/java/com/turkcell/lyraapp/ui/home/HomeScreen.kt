package com.turkcell.lyraapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.home.HomeSong
import com.turkcell.lyraapp.data.home.PlaylistForYou
import com.turkcell.lyraapp.data.home.QuickPick
import com.turkcell.lyraapp.data.home.RecentlyPlayed
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.player.LyraMiniPlayer
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Home akışının durumlu (stateful) giriş noktası.
 *
 * [HomeViewModel]'i Hilt'ten alır, durumu yaşam döngüsüne duyarlı şekilde toplar ve
 * tek seferlik [HomeEffect]'leri tüketir. Yükleme hatasında snackbar üzerinden
 * "Tekrar dene" aksiyonu [HomeIntent.Retry] niyetine köprülenir.
 */
@Composable
fun HomeRoute(
    onSongClick: (songId: String, title: String, artist: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowError -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "Tekrar dene",
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(HomeIntent.Retry)
                    }
                }

                is HomeEffect.NavigateToPlayer ->
                    onSongClick(effect.songId, effect.title, effect.artist)
            }
        }
    }

    HomeScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Ana sayfa ("Ne dinlemek istersin?") ekranı.
 *
 * Tamamen durumsuzdur (stateless): durumu [state] üzerinden alır, kullanıcı etkileşimlerini
 * [onIntent] ile yukarı yayımlar. Alt çubuk boşluğu dış Scaffold'dan (LyraNavHost) gelir;
 * burada yalnızca durum çubuğu (status bar) boşluğu yönetilir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.nowPlayingSong != null) {
                LyraMiniPlayer(
                    title = state.nowPlayingSong.title,
                    artist = state.nowPlayingSong.artist,
                    coverUrl = "", // In a real scenario we'd use URL, but currently it's local gradient. Wait, HomeSong doesn't have coverUrl? We'll leave it empty to trigger fallback.
                    isPlaying = state.isPlaying,
                    isFavorite = state.isFavorite,
                    onTogglePlayPause = { onIntent(HomeIntent.TogglePlayPause) },
                    onToggleFavorite = { onIntent(HomeIntent.ToggleFavorite) },
                    onSkipNext = { onIntent(HomeIntent.SkipNext) },
                    onClick = { onIntent(HomeIntent.SongSelected(state.nowPlayingSong)) },
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp, end = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        if (state.isLoading && state.quickPicks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = { onIntent(HomeIntent.Retry) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { 
                        HomeHeader(
                            greeting = state.greeting, 
                            userInitials = state.userInitials,
                            isDarkMode = state.isDarkMode,
                            onToggleTheme = { onIntent(HomeIntent.ToggleTheme) }
                        ) 
                    }
                    item { 
                        QuickPickGrid(
                            quickPicks = state.quickPicks,
                            onItemClick = { item ->
                                onIntent(HomeIntent.SongSelected(HomeSong(item.id, item.title, item.artist, item.artworkStartColor, item.artworkEndColor)))
                            }
                        ) 
                    }
                    item { SectionHeader(title = "Son çalınanlar", trailingText = "Tümü") }
                    item { 
                        RecentlyPlayedRow(
                            items = state.recentlyPlayed,
                            onItemClick = { item ->
                                onIntent(HomeIntent.SongSelected(HomeSong(item.id, item.title, item.subtitle, item.artworkStartColor, item.artworkEndColor)))
                            }
                        ) 
                    }
                    item { SectionHeader(title = "Senin için çalma listeleri") }
                    item { PlaylistsForYouRow(items = state.playlistsForYou) }
                }
            }
        }
    }
}

/** Selamlama + başlık ile tema ikonu ve kullanıcı avatarını içeren üst bölüm. */
@Composable
private fun HomeHeader(
    greeting: String,
    userInitials: String,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Ne dinlemek istersin?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Icon(
            imageVector = if (isDarkMode) LyraIcons.LightMode else LyraIcons.DarkMode,
            contentDescription = "Temayı Değiştir",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .clickable { onToggleTheme() }
        )
        Spacer(Modifier.width(16.dp))
        UserAvatar(initials = userInitials)
    }
}

@Composable
private fun UserAvatar(initials: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

/** Hızlı seçimlerin 2 sütunlu sabit grid'i (6 öğe; dikey scroll LazyColumn'a aittir). */
@Composable
private fun QuickPickGrid(quickPicks: List<QuickPick>, onItemClick: (QuickPick) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        quickPicks.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { item ->
                    QuickPickCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickPickCard(
    item: QuickPick,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(
            startColor = item.artworkStartColor,
            endColor = item.artworkEndColor,
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight(),
        )
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
    }
}

/** Bölüm başlığı; [trailingText] verilirse sağda vurgu rengiyle gösterilir (örn. "Tümü"). */
@Composable
private fun SectionHeader(
    title: String,
    trailingText: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/**
 * API'dan gelen tek bir şarkı satırı: küçük gradyan kapak + başlık ve sanatçı.
 *
 * Satıra tıklanınca [onClick] tetiklenir ve oynatıcı ekranına gidilir.
 */
@Composable
private fun SongRow(
    song: HomeSong,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(
            startColor = song.artworkStartColor,
            endColor = song.artworkEndColor,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** "Son çalınanlar" yatay scrollable kart listesi. */
@Composable
private fun RecentlyPlayedRow(items: List<RecentlyPlayed>, onItemClick: (RecentlyPlayed) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(items, key = { it.id }) { item ->
            Column(
                modifier = Modifier
                    .width(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onItemClick(item) }
            ) {
                Artwork(
                    startColor = item.artworkStartColor,
                    endColor = item.artworkEndColor,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** "Senin için çalma listeleri" yatay scrollable büyük kart listesi. */
@Composable
private fun PlaylistsForYouRow(items: List<PlaylistForYou>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(items, key = { it.id }) { item ->
            Column(modifier = Modifier.width(170.dp)) {
                Artwork(
                    startColor = item.artworkStartColor,
                    endColor = item.artworkEndColor,
                    modifier = Modifier
                        .size(170.dp)
                        .clip(RoundedCornerShape(20.dp)),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Kapak görseli yer tutucusu: modeldeki ARGB renk çiftinden köşegen gradyan + hafif
 * radyal parlama çizer. Gerçek API görsel URL'si sağladığında bu composable görsel
 * yükleyiciyle değiştirilir.
 */
@Composable
private fun Artwork(
    startColor: Long,
    endColor: Long,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Brush.linearGradient(listOf(Color(startColor), Color(endColor))))
            .background(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                ),
            ),
    )
}

// Removed NowPlayingBar in favor of LyraMiniPlayer

private val previewState = HomeUiState(
    greeting = "İyi akşamlar",
    userInitials = "ZK",
    songs = listOf(
        HomeSong("s_neon-tide", "Neon Tide", "Aurora Drift", 0xFF8B6FB8, 0xFF4A3D6B),
        HomeSong("s_city-lights", "City Lights", "Aurora Drift", 0xFF4AC2A8, 0xFF1F6E5C),
        HomeSong("s_polaris", "Polaris", "Stardust", 0xFFD98E4A, 0xFF8A5526),
    ),
    quickPicks = listOf(
        QuickPick("qp-1", "Gece Sürüşü", "Aurora Drift", 0xFF8B6FB8, 0xFF4A3D6B),
        QuickPick("qp-2", "Sabah Kahvesi", "Stardust", 0xFF7C83D9, 0xFF3E4486),
        QuickPick("qp-3", "Neon Sokaklar", "Polaris", 0xFFD98E4A, 0xFF8A5526),
        QuickPick("qp-4", "Odaklan", "Aurora Drift", 0xFF4AC2A8, 0xFF1F6E5C),
        QuickPick("qp-5", "Derin Mavi", "Okyanus", 0xFF6FBF5A, 0xFF356B2A),
        QuickPick("qp-6", "Yaz Anıları", "Stardust", 0xFF5AAFC9, 0xFF2A5F73),
    ),
    recentlyPlayed = listOf(
        RecentlyPlayed("rp-1", "Neon Sokaklar", "Şehir Işıkları", 0xFFD98E4A, 0xFF8A5526),
        RecentlyPlayed("rp-2", "Derin Mavi", "Okyanus", 0xFF6FBF5A, 0xFF356B2A),
        RecentlyPlayed("rp-3", "Yıldız Tozu", "Polaris", 0xFF3D5A80, 0xFF1B2A45),
    ),
    playlistsForYou = listOf(
        PlaylistForYou("pl-1", "Haftalık Keşif", 0xFF9B7FC4, 0xFF5A4480),
        PlaylistForYou("pl-2", "Sakin Akşamlar", 0xFF6B5FB8, 0xFF3A3270),
        PlaylistForYou("pl-3", "Enerji Ver", 0xFF3FAE9C, 0xFF1E5D52),
    ),
)

@Preview(name = "Home - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        HomeScreen(state = previewState, onIntent = {})
    }
}

@Preview(name = "Home - Light", showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        HomeScreen(state = previewState, onIntent = {})
    }
}
