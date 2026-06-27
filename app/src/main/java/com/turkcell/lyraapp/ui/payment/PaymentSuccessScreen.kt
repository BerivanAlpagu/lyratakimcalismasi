package com.turkcell.lyraapp.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.turkcell.lyraapp.ui.icons.LyraIcons

@Composable
fun PaymentSuccessRoute(
    onStartListening: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PaymentSuccessScreen(
        onStartListening = onStartListening,
        modifier = modifier,
    )
}

@Composable
fun PaymentSuccessScreen(
    onStartListening: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF66424E), Color(0xFF130C10)),
                ),
            )
            .padding(horizontal = 28.dp, vertical = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFFFA6C8), Color(0xFFFFC4A5)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LyraIcons.Check,
                    contentDescription = null,
                    tint = Color(0xFF4A1230),
                    modifier = Modifier.size(38.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Premium aktif!",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Premium erisimin basladi. Reklamsiz, sinirsiz ve cevrimdisi dinlemenin keyfini cikar.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Premium",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            )
        }

        Button(
            onClick = onStartListening,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFA6C8),
                contentColor = Color(0xFF4A1230),
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(58.dp),
        ) {
            Text(
                text = "Dinlemeye basla",
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
