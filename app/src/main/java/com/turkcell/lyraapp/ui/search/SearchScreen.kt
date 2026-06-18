package com.turkcell.lyraapp.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.songs.SongDto
import com.turkcell.lyraapp.ui.icons.LyraIcons

/**
 * Arama akışının durumlu (stateful) giriş noktası.
 */
@Composable
fun SearchRoute(
    onSongClick: (songId: String, title: String, artist: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SearchEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is SearchEffect.NavigateToPlayer -> onSongClick(effect.songId, effect.title, effect.artist)
            }
        }
    }

    SearchScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Arama ekranı. Durumsuz (stateless) bileşendir.
 */
@Composable
fun SearchScreen(
    state: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
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
                .statusBarsPadding()
        ) {
            // Başlık "Ara"
            Text(
                text = "Ara",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // Arama Kutusu
            SearchTextField(
                value = state.query,
                onValueChange = { onIntent(SearchIntent.QueryChanged(it)) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // Filtre Çipleri
            FilterChipsRow(
                selectedFilter = state.selectedFilter,
                onFilterSelected = { onIntent(SearchIntent.FilterSelected(it)) },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val showGenreGrid = state.query.isEmpty() && state.selectedFilter == "Hepsi"

            if (showGenreGrid) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Text(
                            text = "Türlere göz at",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }
                    item {
                        GenreGrid(
                            onGenreClick = { genreName ->
                                onIntent(SearchIntent.FilterSelected(genreName))
                            },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            } else {
                // Arama Sonuçları, Yükleniyor veya Boş Durum
                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (state.searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sonuç bulunamadı.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.searchResults, key = { it.id }) { song ->
                            SongResultRow(
                                song = song,
                                onClick = { onIntent(SearchIntent.SongSelected(song)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        placeholder = {
            Text(
                text = "Şarkı, sanatçı veya albüm",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = LyraIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
    )
}

@Composable
private fun FilterChipsRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filters = listOf("Hepsi", "Pop", "Elektronik", "Akustik")
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(filters) { filter ->
            FilterChipItem(
                text = filter,
                isSelected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        } else {
            Color.Transparent
        },
        border = if (isSelected) {
            null
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = LyraIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

private data class GenreCardData(
    val title: String,
    val startColor: Color,
    val endColor: Color,
)

@Composable
private fun GenreGrid(
    onGenreClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val genres = listOf(
        GenreCardData("Pop", Color(0xFF4AC2A8), Color(0xFF1F6E5C)),
        GenreCardData("Elektronik", Color(0xFF7C83D9), Color(0xFF3E4486)),
        GenreCardData("Akustik", Color(0xFF8B6FB8), Color(0xFF4A3D6B)),
        GenreCardData("Lo-fi", Color(0xFF457B9D), Color(0xFF1D3557)),
        GenreCardData("Indie", Color(0xFF5E548E), Color(0xFF231942)),
        GenreCardData("Jazz", Color(0xFF6FBF5A), Color(0xFF356B2A)),
        GenreCardData("Klasik", Color(0xFFB85F6F), Color(0xFF602A34)),
        GenreCardData("Yolculuk", Color(0xFFE07A5F), Color(0xFF8D3F2D))
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (i in genres.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GenreCard(
                    genre = genres[i],
                    onClick = { onGenreClick(genres[i].title) },
                    modifier = Modifier.weight(1f)
                )
                if (i + 1 < genres.size) {
                    GenreCard(
                        genre = genres[i + 1],
                        onClick = { onGenreClick(genres[i + 1].title) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GenreCard(
    genre: GenreCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(genre.startColor, genre.endColor)))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        // Alt sağ köşedeki dekoratif iç içe geçmiş halkalar
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 12.dp)
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )

        Text(
            text = genre.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}

@Composable
private fun SongResultRow(
    song: SongDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (startColor, endColor) = remember(song.id) { artworkColorsFor(song.id) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Kapak görseli (gradyan kutu)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(listOf(startColor, endColor)))
                .background(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                    )
                ),
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
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

/**
 * Şarkı id'sinden deterministik kapak renkleri üretir.
 */
private fun artworkColorsFor(id: String): Pair<Color, Color> {
    val hue = (kotlin.math.abs(id.hashCode()) % 360).toFloat()
    val start = hslToColor(hue, saturation = 0.50f, lightness = 0.55f)
    val end = hslToColor(hue, saturation = 0.55f, lightness = 0.32f)
    return start to end
}

private fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color {
    val c = (1f - kotlin.math.abs(2f * lightness - 1f)) * saturation
    val hPrime = hue / 60f
    val x = c * (1f - kotlin.math.abs(hPrime % 2f - 1f))
    val (r1, g1, b1) = when {
        hPrime < 1f -> Triple(c, x, 0f)
        hPrime < 2f -> Triple(x, c, 0f)
        hPrime < 3f -> Triple(0f, c, x)
        hPrime < 4f -> Triple(0f, x, c)
        hPrime < 5f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val m = lightness - c / 2f
    val r = ((r1 + m) * 255f).toInt().coerceIn(0, 255)
    val g = ((g1 + m) * 255f).toInt().coerceIn(0, 255)
    val b = ((b1 + m) * 255f).toInt().coerceIn(0, 255)
    return Color(red = r, green = g, blue = b)
}