package com.turkcell.lyraapp.ui.create_playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.turkcell.lyraapp.ui.icons.LyraIcons

/**
 * CreatePlaylist akisinin durumlu (stateful) giris noktasi.
 */
@Composable
fun CreatePlaylistRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreatePlaylistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CreatePlaylistEffect.ShowError ->
                    snackbarHostState.showSnackbar(effect.message)

                is CreatePlaylistEffect.NavigateBack ->
                    onNavigateBack()

                is CreatePlaylistEffect.PlaylistCreatedSuccessfully -> { }
            }
        }
    }

    CreatePlaylistScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Yeni calma listesi olusturma ekrani.
 */
@Composable
fun CreatePlaylistScreen(
    state: CreatePlaylistUiState,
    onIntent: (CreatePlaylistIntent) -> Unit,
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
            // Ust Bar
            CreatePlaylistTopBar(
                isSaving = state.isSaving,
                isSaveEnabled = state.playlistName.isNotBlank() && !state.isSaving,
                onClose = { onIntent(CreatePlaylistIntent.CloseClicked) },
                onSave = { onIntent(CreatePlaylistIntent.SaveClicked) },
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Kapak + Input bolumu
                item {
                    CoverAndInputSection(
                        playlistName = state.playlistName,
                        playlistDescription = state.playlistDescription,
                        onNameChanged = { onIntent(CreatePlaylistIntent.NameChanged(it)) },
                        onDescriptionChanged = { onIntent(CreatePlaylistIntent.DescriptionChanged(it)) },
                    )
                }

                // Herkese acik toggle
                item {
                    PublicToggleSection(
                        isPublic = state.isPublic,
                        onToggle = { onIntent(CreatePlaylistIntent.TogglePublic) },
                    )
                }

                // Ayirici cizgi
                item {
                    Divider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    )
                }

                // Sarki ekle basligi
                item {
                    SongAddHeader(selectedCount = state.selectedSongIds.size)
                }

                // Yukleniyor durumu
                if (state.isLoadingSongs) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                // Sarki listesi
                items(items = state.allSongs, key = { it.id }) { song ->
                    SongSelectionItem(
                        song = song,
                        isSelected = state.selectedSongIds.contains(song.id),
                        onClick = { onIntent(CreatePlaylistIntent.ToggleSongSelection(song.id)) },
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun CreatePlaylistTopBar(
    isSaving: Boolean,
    isSaveEnabled: Boolean,
    onClose: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = LyraIcons.Close,
                contentDescription = "Kapat",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = "Yeni calma listesi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Kaydet butonu
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isSaveEnabled)
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    else
                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                )
                .clickable(enabled = isSaveEnabled, onClick = onSave)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                Text(
                    text = "Kaydet",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSaveEnabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
            }
        }
    }
}

@Composable
private fun CoverAndInputSection(
    playlistName: String,
    playlistDescription: String,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Gradient kapak gorseli
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                        )
                    )
                ),
            contentAlignment = Alignment.BottomStart,
        ) {
            // Kucuk edit/kalem ikonu
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LyraIcons.Settings,
                    contentDescription = "Duzenle",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Isim ve aciklama alanlari
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val transparentColors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            TextField(
                value = playlistName,
                onValueChange = onNameChanged,
                placeholder = {
                    Text(
                        text = "Calma listesi adi",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                colors = transparentColors,
                modifier = Modifier.fillMaxWidth(),
            )

            TextField(
                value = playlistDescription,
                onValueChange = onDescriptionChanged,
                placeholder = {
                    Text(
                        text = "Aciklama ekle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                colors = transparentColors,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PublicToggleSection(
    isPublic: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Kucuk dunya ikonu (Person yerine kullaniyoruz)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LyraIcons.PersonOutlined,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    text = "Herkese acik",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Profilinde gorunur",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = isPublic,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
        )
    }
}

@Composable
private fun SongAddHeader(selectedCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Sarki ekle",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "$selectedCount secili",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SongSelectionItem(
    song: SongUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Gradient kapak
        SongSelectionArtwork(songId = song.id)
        Spacer(modifier = Modifier.width(16.dp))
        // Sarki adi ve sanatci
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        // Secim ikonu (yuvarlak)
        SelectionIndicator(isSelected = isSelected)
    }
}

@Composable
private fun SongSelectionArtwork(
    songId: String,
    modifier: Modifier = Modifier,
) {
    val colors = deterministicSongGradient(songId)

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.linearGradient(listOf(colors.first, colors.second))
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = LyraIcons.LibraryMusic,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SelectionIndicator(isSelected: Boolean) {
    if (isSelected) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LyraIcons.Check,
                contentDescription = "Secili",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                ),
        )
    }
}

@Composable
private fun deterministicSongGradient(id: String): Pair<Color, Color> {
    val colors = listOf(
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.primaryContainer,
    )
    val index = (id.hashCode() and 0x7FFFFFFF) % colors.size
    return colors[index]
}
