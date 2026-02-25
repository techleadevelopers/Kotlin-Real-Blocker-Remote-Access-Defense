package com.blockremote.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import com.blockremote.ui.theme.CarbonGrey
import com.blockremote.ui.theme.MatrixGreen
import com.blockremote.ui.theme.OffWhite

@Composable
fun SentinelSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 0.dp,
        animationSpec = tween(250),
        label = "thumb_offset"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) MatrixGreen.copy(alpha = 0.3f) else CarbonGrey,
        animationSpec = tween(250),
        label = "track_color"
    )

    val thumbColor by animateColorAsState(
        targetValue = if (checked) MatrixGreen else OffWhite.copy(alpha = 0.5f),
        animationSpec = tween(250),
        label = "thumb_color"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = OffWhite
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OffWhite.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .width(52.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(trackColor)
                .border(
                    width = 1.dp,
                    color = if (checked) MatrixGreen.copy(alpha = 0.6f) else OffWhite.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(2.dp)
        ) {
            Box(contentAlignment = Alignment.CenterStart) {
                if (checked) {
                    Box(
                        modifier = Modifier
                            .offset(x = thumbOffset)
                            .size(24.dp)
                            .blur(8.dp)
                            .clip(CircleShape)
                            .background(MatrixGreen.copy(alpha = 0.5f))
                    )
                }
                Box(
                    modifier = Modifier
                        .offset(x = thumbOffset)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(thumbColor)
                )
            }
        }
    }
}
