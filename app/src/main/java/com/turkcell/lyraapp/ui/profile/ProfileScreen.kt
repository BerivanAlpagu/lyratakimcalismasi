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
import androidx.compose.ui.text.input.KeyboardType
import com.turkcell.lyraapp.data.profile.UserProfile
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = hiltViewModel(),
    onShowSnackbar: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ProfileEffect.ShowError -> onShowSnackbar(effect.message)
                is ProfileEffect.NavigateToLogin -> onNavigateToLogin()
                is ProfileEffect.NavigateToPremium -> onNavigateToPremium()
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
    val containerColor = MaterialTheme.colorScheme.background
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val sheetState = rememberModalBottomSheetState()
    
    if (uiState.showEditSheet) {
        ModalBottomSheet(
            onDismissRequest = { onIntent(ProfileIntent.DismissEditSheet) },
            sheetState = sheetState
        ) {
            EditProfileSheet(
                profile = uiState.profile,
                onSave = { first, last, birth ->
                    onIntent(ProfileIntent.SaveProfile(first, last, birth))
                },
                onCancel = { onIntent(ProfileIntent.DismissEditSheet) }
            )
        }
    }
    
    Scaffold(
        containerColor = containerColor,
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
                    containerColor = containerColor
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
                    isDarkMode = uiState.isDarkMode,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    onIntent = onIntent
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    user: UserProfile,
    isDarkMode: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    onIntent: (ProfileIntent) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 100.dp) // Bottom bar offset
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Dynamic Initials
        val initialF = user.firstName.takeIf { it.isNotBlank() }?.take(1)?.uppercase() ?: "Z"
        val initialL = user.lastName.takeIf { it.isNotBlank() }?.take(1)?.uppercase() ?: "K"
        val initials = "$initialF$initialL"
        
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFE91E63), Color(0xFFFF9800))
                    )
                )
                .align(Alignment.CenterHorizontally)
                .clickable { onIntent(ProfileIntent.EditProfileClicked) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
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
        
        // Premium Banner
        val isPremium = user.membership?.status == "active"
        val expiresAt = user.membership?.expiresAt
        
        val daysLeft = try {
            if (expiresAt != null) {
                val expiry = Instant.parse(expiresAt)
                val now = Instant.now()
                ChronoUnit.DAYS.between(now, expiry)
            } else null
        } catch (e: Exception) {
            null
        }

        val bannerText = when {
            isPremium && daysLeft != null && daysLeft <= 3 -> "Premium - $daysLeft gün kaldı"
            isPremium -> "Premium"
            else -> "Premium'a Geç"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFE91E63), Color(0xFF9C27B0))))
                .clickable { onIntent(ProfileIntent.PremiumClicked) }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bannerText,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (!isDarkMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onIntent(ProfileIntent.ThemeChanged(false)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "☀ Açık", color = if (!isDarkMode) MaterialTheme.colorScheme.onPrimary else textPrimary, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (isDarkMode) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onIntent(ProfileIntent.ThemeChanged(true)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "☾ Koyu", color = if (isDarkMode) MaterialTheme.colorScheme.onPrimary else textPrimary, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Items
            SettingsItem("Ses kalitesi", "Yüksek", textPrimary, textSecondary)
            SettingsItem("Çevrimdışı indirme", "Açık", textPrimary, textSecondary)
            SettingsItem("Bildirimler", null, textPrimary, textSecondary)
            SettingsItem("Gizlilik", null, textPrimary, textSecondary)
            SettingsItem("Yardım ve destek", null, textPrimary, textSecondary)

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { onIntent(ProfileIntent.LogoutClicked) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Çıkış Yap", color = MaterialTheme.colorScheme.onError)
            }
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

@Composable
fun EditProfileSheet(
    profile: UserProfile?,
    onSave: (String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var firstName by remember { mutableStateOf(profile?.name?.substringBefore(" ") ?: "") }
    var lastName by remember { mutableStateOf(profile?.name?.substringAfter(" ", "") ?: "") }
    var birthDate by remember { mutableStateOf("") } // UserProfile'da birthDate tutulmuyor, boş başlıyoruz.

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Profili Düzenle", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Ad") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Soyad") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = birthDate,
            onValueChange = { birthDate = it },
            label = { Text("Doğum Tarihi (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("İptal")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { onSave(firstName, lastName, birthDate) }) {
                Text("Kaydet")
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
