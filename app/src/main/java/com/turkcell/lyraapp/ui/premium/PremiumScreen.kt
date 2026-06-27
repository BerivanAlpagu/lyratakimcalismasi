package com.turkcell.lyraapp.ui.premium

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.premium.PremiumPlan
import com.turkcell.lyraapp.data.premium.PremiumPlanType
import com.turkcell.lyraapp.ui.icons.LyraIcons

@Composable
fun PremiumRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (PremiumPlanType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PremiumEffect.NavigateBack -> onNavigateBack()
                is PremiumEffect.NavigateToPayment -> onNavigateToPayment(effect.planType)
                is PremiumEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    PremiumScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun PremiumScreen(
    state: PremiumUiState,
    onIntent: (PremiumIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PremiumBackground),
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
        ) {
            IconButton(
                onClick = { onIntent(PremiumIntent.BackClicked) },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Icon(
                    imageVector = LyraIcons.ArrowBack,
                    contentDescription = "Geri",
                    tint = Color.White,
                )
            }

            PremiumHero()
            Spacer(Modifier.height(24.dp))
            PremiumBenefits()
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Planini sec",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))

            if (state.isLoading && state.plans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = PremiumPink)
                }
            } else if (state.errorMessage != null && state.plans.isEmpty()) {
                PremiumError(
                    message = state.errorMessage,
                    onRetry = { onIntent(PremiumIntent.RetryClicked) },
                )
            } else {
                state.plans
                    .sortedBy { if (it.type == PremiumPlanType.Recurring) 0 else 1 }
                    .forEach { plan ->
                        PlanOption(
                            plan = plan,
                            selected = plan.type == state.selectedPlanType,
                            onClick = { onIntent(PremiumIntent.PlanSelected(plan.type)) },
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                Button(
                    onClick = { onIntent(PremiumIntent.ContinueClicked) },
                    enabled = !state.isLoading && state.plans.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PremiumPink,
                        contentColor = PremiumTextDark,
                        disabledContainerColor = PremiumMuted,
                        disabledContentColor = PremiumSoftText,
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                ) {
                    Text(
                        text = "Devam et",
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.size(10.dp))
                    Icon(
                        imageVector = LyraIcons.ArrowForward,
                        contentDescription = null,
                    )
                }

                val selected = state.plans.firstOrNull { it.type == state.selectedPlanType }
                if (selected != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = selected.footerText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumHero() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.linearGradient(listOf(PremiumPink, PremiumPeach))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LyraIcons.Check,
                contentDescription = null,
                tint = PremiumTextDark,
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "LyraApp Premium",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Reklamsiz, sinirsiz ve cevrimdisi muzigin keyfini cikar.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun PremiumBenefits() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        BenefitItem(LyraIcons.Close, "Reklamsiz dinleme", "Kesintisiz, sinirsiz muzik")
        BenefitItem(LyraIcons.SkipNext, "Sinirsiz atlama", "Istedigin sarkiya gec")
        BenefitItem(LyraIcons.Download, "Cevrimdisi indirme", "Internet olmadan dinle")
        BenefitItem(LyraIcons.Waveform, "Yuksek ses kalitesi", "320 kbps net ses")
        BenefitItem(LyraIcons.Cast, "Tum cihazlarinda", "Telefon, tablet ve masaustu")
    }
}

@Composable
private fun BenefitItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PremiumSurface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PremiumPink,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun PlanOption(
    plan: PremiumPlan,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) PremiumPink else PremiumOutline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
            .background(if (selected) PremiumSelectedSurface else PremiumCardSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = PremiumPink,
                unselectedColor = PremiumOutline,
            ),
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = plan.displayName(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                )
                if (plan.type == PremiumPlanType.Recurring) {
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text = "Populer",
                        style = MaterialTheme.typography.labelSmall,
                        color = PremiumTextDark,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PremiumPink)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
            Text(
                text = plan.descriptionText(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
            )
        }
        Text(
            text = plan.priceText(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PremiumError(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PremiumCardSurface)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PremiumPink),
        ) {
            Text("Tekrar dene", color = PremiumTextDark)
        }
    }
}

private fun PremiumPlan.displayName(): String =
    when (type) {
        PremiumPlanType.Recurring -> "Aylik\nabonelik"
        PremiumPlanType.OneTime -> "Tek seferlik"
    }

private fun PremiumPlan.descriptionText(): String =
    when (type) {
        PremiumPlanType.Recurring -> "Istedigin zaman iptal et"
        PremiumPlanType.OneTime -> "$durationDays gun erisim - otomatik yenileme yok"
    }

private fun PremiumPlan.priceText(): String {
    val amount = priceKurus / 100.0
    val suffix = if (type == PremiumPlanType.Recurring) " / ay" else ""
    return "₺${"%.2f".format(amount)}$suffix"
}

private fun PremiumPlan.footerText(): String =
    when (type) {
        PremiumPlanType.Recurring -> "${priceText()}. Diledigin zaman iptal edebilirsin."
        PremiumPlanType.OneTime -> "$durationDays gunluk tek seferlik erisim."
    }

private val PremiumBackground = Color(0xFF160D11)
private val PremiumSurface = Color(0xFF2A1D22)
private val PremiumCardSurface = Color(0xFF20161A)
private val PremiumSelectedSurface = Color(0xFF3A2029)
private val PremiumMuted = Color(0xFF3D2D33)
private val PremiumOutline = Color(0xFF76545F)
private val PremiumPink = Color(0xFFFFA6C8)
private val PremiumPeach = Color(0xFFFFC4A5)
private val PremiumTextDark = Color(0xFF4A1230)
private val PremiumSoftText = Color(0xFFAA8C96)
