package com.blockremote.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.blockremote.ui.theme.CarbonGrey
import com.blockremote.ui.theme.MatrixGreen

@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    neonColor: Color = MatrixGreen,
    isAlert: Boolean = false,
    content: @Composable () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isAlert) MaterialTheme.colorScheme.error else neonColor,
        animationSpec = tween(durationMillis = 500),
        label = "neon_border_color"
    )

    val glowColor = borderColor.copy(alpha = 0.15f)
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    color = glowColor,
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    style = Stroke(width = 6.dp.toPx())
                )
                drawRoundRect(
                    color = glowColor.copy(alpha = 0.05f),
                    cornerRadius = CornerRadius(20.dp.toPx())
                )
            }
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.6f),
                shape = shape
            )
            .background(CarbonGrey.copy(alpha = 0.85f))
            .padding(16.dp)
    ) {
        Column {
            if (title != null) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = borderColor,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            content()
        }
    }
}
