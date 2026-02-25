package com.blockremote.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.blockremote.ui.theme.CyberLime
import com.blockremote.ui.theme.MatrixGreen
import com.blockremote.ui.theme.PureBlack

@Composable
fun CyberButton(
    text: String,
    modifier: Modifier = Modifier,
    neonColor: Color = MatrixGreen,
    isAlert: Boolean = false,
    enableHaptic: Boolean = true,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val activeColor = if (isAlert) MaterialTheme.colorScheme.error else neonColor
    val shape = RoundedCornerShape(12.dp)

    val infiniteTransition = rememberInfiniteTransition(label = "cyber_btn_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .blur(16.dp)
                .background(activeColor.copy(alpha = glowAlpha * 0.3f))
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .clip(shape)
                .drawBehind {
                    drawRoundRect(
                        color = activeColor.copy(alpha = glowAlpha),
                        cornerRadius = CornerRadius(12.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            activeColor.copy(alpha = 0.15f),
                            PureBlack.copy(alpha = 0.9f)
                        )
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (enableHaptic) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onClick()
                }
                .padding(horizontal = 32.dp, vertical = 14.dp)
        ) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = activeColor
            )
        }
    }
}
