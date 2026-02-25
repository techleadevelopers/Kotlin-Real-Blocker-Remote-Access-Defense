package com.blockremote.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blockremote.ui.theme.AlertRed
import com.blockremote.ui.theme.CarbonGrey
import com.blockremote.ui.theme.MatrixGreen
import com.blockremote.ui.theme.OffWhite
import com.blockremote.ui.theme.PureBlack

@Composable
fun SubscriptionOverlay(
    trialDaysRemaining: Int,
    isLocked: Boolean,
    onSubscribe: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "paywall_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isLocked) "SYSTEM LOCKED" else "TRIAL EXPIRED",
                style = MaterialTheme.typography.displayLarge,
                color = if (isLocked) AlertRed else MatrixGreen,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isLocked)
                    "Your account has been locked by the server.\nContact support for assistance."
                else
                    "Your 7-day trial has ended.\nSubscribe to maintain sentinel protection.",
                style = MaterialTheme.typography.bodyMedium,
                color = OffWhite.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!isLocked) {
                Text(
                    text = "Without an active subscription, your device\nremains vulnerable to remote access threats.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AlertRed.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            NeonCard(
                neonColor = MatrixGreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SENTINEL PRO",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MatrixGreen,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "R$ 14,90/mês",
                        style = MaterialTheme.typography.headlineLarge,
                        color = OffWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val features = listOf(
                        "Real-time sensor monitoring",
                        "Accessibility guard protection",
                        "Network traffic analysis",
                        "Priority threat detection",
                        "Audit log retention (30 days)"
                    )

                    features.forEach { feature ->
                        Text(
                            text = "› $feature",
                            style = MaterialTheme.typography.bodySmall,
                            color = OffWhite.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isLocked) {
                CyberButton(
                    text = "ATIVAR ASSINATURA",
                    onClick = onSubscribe
                )
            }
        }
    }
}
