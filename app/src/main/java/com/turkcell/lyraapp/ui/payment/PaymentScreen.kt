package com.turkcell.lyraapp.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.turkcell.lyraapp.ui.icons.LyraIcons

@Composable
fun PaymentRoute(
    viewModel: PaymentViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PaymentEffect.NavigateBack -> onNavigateBack()
                is PaymentEffect.PaymentSuccessful -> onPaymentSuccess()
                is PaymentEffect.ShowError -> {
                    // TODO: show error snackbar
                }
            }
        }
    }

    PaymentScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun PaymentScreen(
    state: PaymentState,
    onIntent: (PaymentIntent) -> Unit
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
            IconButton(onClick = { onIntent(PaymentIntent.BackClicked) }) {
                Icon(
                    imageVector = LyraIcons.ArrowBack,
                    contentDescription = "Geri",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Ödeme Bilgileri",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            OutlinedTextField(
                value = state.cardNumber,
                onValueChange = { onIntent(PaymentIntent.CardNumberChanged(it)) },
                label = { Text("Kart Numarası") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.expMonth,
                    onValueChange = { onIntent(PaymentIntent.ExpMonthChanged(it)) },
                    label = { Text("Ay (AA)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = state.expYear,
                    onValueChange = { onIntent(PaymentIntent.ExpYearChanged(it)) },
                    label = { Text("Yıl (YYYY)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.cvc,
                onValueChange = { onIntent(PaymentIntent.CvcChanged(it)) },
                label = { Text("CVC") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.holderName,
                onValueChange = { onIntent(PaymentIntent.HolderNameChanged(it)) },
                label = { Text("Kart Üzerindeki İsim") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onIntent(PaymentIntent.SubmitPayment) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Ödemeyi Tamamla",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
