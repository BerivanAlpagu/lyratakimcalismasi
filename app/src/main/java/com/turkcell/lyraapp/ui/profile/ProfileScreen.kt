package com.turkcell.lyraapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.turkcell.lyraapp.ui.icons.LyraIcons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.profile.UserProfile
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = hiltViewModel(),
    onShowSnackbar: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ProfileEffect.ShowError -> onShowSnackbar(effect.message)
                is ProfileEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    ProfileScreen(
        uiState = uiState,
        onIntent = viewModel::handleIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit
) {
    val lightBackground = Color(0xFFFFF8F9)
    val textPrimary = Color(0xFF1D1B20)
    val textSecondary = Color(0xFF49454F)
    
    Scaffold(
        containerColor = lightBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Profil", 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.Medium,
                        color = textPrimary
                    ) 
                },
                actions = {
                    IconButton(onClick = { /* TODO: Settings */ }) {
                        Icon(
                            imageVector = LyraIcons.Settings,
                            contentDescription = "Ayarlar",
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = lightBackground
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
                    color = Color(0xFF9A5A6D),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.error != null && uiState.profile == null) {
                Text(
                    text = uiState.error, 
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.profile != null) {
                ProfileContent(
                    user = uiState.profile,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    user: UserProfile,
    textPrimary: Color,
    textSecondary: Color
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 100.dp) // Bottom bar offset
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF9A5A6D), Color(0xFFD4A373))
                    )
                )
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ZK",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Name and Username
        Text(
            text = user.name,
            color = textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${user.username} · ${user.status}",
            color = textSecondary,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = user.playlistCount.toString(), label = "Çalma listesi", textPrimary, textSecondary)
            StatItem(value = user.followerCount, label = "Takipçi", textPrimary, textSecondary)
            StatItem(value = user.followingCount.toString(), label = "Takip", textPrimary, textSecondary)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Settings List
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "Görünüm",
                color = textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Theme Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFFF6E9EB))
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF8B475D))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "☀ Açık", color = Color.White, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "☾ Koyu", color = textPrimary)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Items
            SettingsItem("Ses kalitesi", "Yüksek", textPrimary, textSecondary)
            SettingsItem("Çevrimdışı indirme", "Açık", textPrimary, textSecondary)
            SettingsItem("Bildirimler", null, textPrimary, textSecondary)
            SettingsItem("Gizlilik", null, textPrimary, textSecondary)
            SettingsItem("Yardım ve destek", null, textPrimary, textSecondary)
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, textPrimary: Color, textSecondary: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = textSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun SettingsItem(
    title: String, 
    value: String?, 
    textPrimary: Color, 
    textSecondary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder icon for demo, in real app map titles to specific icons
        Text("❖", fontSize = 18.sp, color = textPrimary, modifier = Modifier.padding(end = 16.dp))
        
        Text(
            text = title,
            color = textPrimary,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        
        if (value != null) {
            Text(
                text = value,
                color = textSecondary,
                fontSize = 14.sp
            )
        }
        
        Icon(
            imageVector = LyraIcons.KeyboardArrowRight,
            contentDescription = null,
            tint = textSecondary,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
