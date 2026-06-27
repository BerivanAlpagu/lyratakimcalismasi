package com.turkcell.lyraapp.ui.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.turkcell.lyraapp.data.premium.PremiumPlan
import com.turkcell.lyraapp.ui.icons.LyraIcons

@Composable
fun PremiumScreen(
    viewModel: PremiumViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PremiumEffect.NavigateBack -> onNavigateBack()
                is PremiumEffect.NavigateToPayment -> onNavigateToPayment(effect.planId)
                is PremiumEffect.ShowError -> {
                    // TODO: Show snackbar
                }
            }
        }
    }

    PremiumContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
private fun PremiumContent(
    state: PremiumState,
    onIntent: (PremiumIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onIntent(PremiumIntent.BackClicked) }) {
                Icon(
                    imageVector = LyraIcons.ArrowBack,
                    contentDescription = "Geri",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Premium",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(state.plans) { plan ->
                    PremiumPlanCard(
                        plan = plan,
                        onClick = { onIntent(PremiumIntent.PlanSelected(plan.type)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumPlanCard(
    plan: PremiumPlan,
    onClick: () -> Unit
) {
    val isRecurring = plan.type == "recurring"
    val cardBackground = if (isRecurring) {
        Brush.linearGradient(
            colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
        )
    }

    val textColor = if (isRecurring) Color.White else MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .clickable(onClick = onClick)
            .then(
                if (!isRecurring) Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                else Modifier
            )
            .padding(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                )
                Text(
                    text = "${plan.price} ${plan.currency}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = plan.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textColor.copy(alpha = 0.8f)
                )
            )
        }
    }
}
