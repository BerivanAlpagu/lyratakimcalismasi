package com.turkcell.lyraapp.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.premium.PremiumPlan
import com.turkcell.lyraapp.data.premium.PremiumPlanType
import com.turkcell.lyraapp.ui.icons.LyraIcons

@Composable
fun PaymentRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PaymentEffect.NavigateBack -> onNavigateBack()
                PaymentEffect.NavigateToSuccess -> onNavigateToSuccess()
                is PaymentEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    PaymentScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun PaymentScreen(
    state: PaymentUiState,
    onIntent: (PaymentIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaymentBackground),
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
                .padding(horizontal = 22.dp)
                .padding(bottom = 26.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { onIntent(PaymentIntent.BackClicked) }) {
                    Icon(
                        imageVector = LyraIcons.ArrowBack,
                        contentDescription = "Geri",
                        tint = Color.White,
                    )
                }
                Text(
                    text = "Odeme",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(14.dp))
            PaymentCardPreview(state)
            Spacer(Modifier.height(18.dp))

            PaymentTextField(
                label = "Kart numarasi",
                value = state.cardNumber,
                placeholder = "0000 0000 0000 0000",
                keyboardType = KeyboardType.Number,
                onValueChange = { onIntent(PaymentIntent.CardNumberChanged(it)) },
            )
            Spacer(Modifier.height(12.dp))
            PaymentTextField(
                label = "Kart uzerindeki isim",
                value = state.holderName,
                placeholder = "Ad Soyad",
                keyboardType = KeyboardType.Text,
                onValueChange = { onIntent(PaymentIntent.HolderNameChanged(it)) },
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PaymentTextField(
                    label = "Son kullanma",
                    value = state.expiry,
                    placeholder = "AA/YY",
                    keyboardType = KeyboardType.Number,
                    onValueChange = { onIntent(PaymentIntent.ExpiryChanged(it)) },
                    modifier = Modifier.weight(1f),
                )
                PaymentTextField(
                    label = "CVC",
                    value = state.cvc,
                    placeholder = "123",
                    keyboardType = KeyboardType.Number,
                    onValueChange = { onIntent(PaymentIntent.CvcChanged(it)) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(18.dp))
            OrderSummary(plan = state.selectedPlan)
            Spacer(Modifier.height(18.dp))

            Button(
                onClick = { onIntent(PaymentIntent.PayClicked) },
                enabled = state.isPayEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PaymentPink,
                    contentColor = PaymentTextDark,
                    disabledContainerColor = PaymentDisabled,
                    disabledContentColor = PaymentSoftText,
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = PaymentTextDark,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Icon(
                        imageVector = LyraIcons.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "${state.selectedPlan?.priceText() ?: ""} ode",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Odemen 256-bit SSL ile guvende",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PaymentCardPreview(state: PaymentUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF9D3D61), Color(0xFFC07674))))
            .padding(18.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp, 32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFE8C64A)),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(92.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White.copy(alpha = 0.12f)),
        )
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = state.cardNumber.ifBlank { ".... .... .... ...." },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("KART SAHIBI", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    Text(
                        text = state.holderName.ifBlank { "AD SOYAD" }.uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("SKT", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    Text(
                        text = state.expiry.ifBlank { "AA/YY" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentTextField(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = PaymentPink,
                unfocusedBorderColor = PaymentOutline,
                focusedPlaceholderColor = PaymentSoftText,
                unfocusedPlaceholderColor = PaymentSoftText,
                cursorColor = PaymentPink,
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun OrderSummary(plan: PremiumPlan?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PaymentCardSurface)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PaymentPink),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LyraIcons.Check,
                    contentDescription = null,
                    tint = PaymentTextDark,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    text = "LyraApp Premium",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = plan?.summaryLabel() ?: "Plan yukleniyor",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                )
            }
            Text(
                text = plan?.priceText() ?: "",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PaymentOutline.copy(alpha = 0.5f)),
        )
        Spacer(Modifier.height(14.dp))
        Row {
            Text(
                text = "Bugun odenecek",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = plan?.priceText()?.substringBefore(" /") ?: "",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun PremiumPlan.summaryLabel(): String =
    when (type) {
        PremiumPlanType.Recurring -> "Aylik abonelik"
        PremiumPlanType.OneTime -> "$durationDays gunluk erisim"
    }

private fun PremiumPlan.priceText(): String {
    val amount = priceKurus / 100.0
    val suffix = if (type == PremiumPlanType.Recurring) " / ay" else ""
    return "₺${"%.2f".format(amount)}$suffix"
}

private val PaymentBackground = Color(0xFF160D11)
private val PaymentCardSurface = Color(0xFF24181D)
private val PaymentOutline = Color(0xFF7A5B65)
private val PaymentPink = Color(0xFFFFA6C8)
private val PaymentDisabled = Color(0xFF3B2C32)
private val PaymentSoftText = Color(0xFF9C818B)
private val PaymentTextDark = Color(0xFF4A1230)
