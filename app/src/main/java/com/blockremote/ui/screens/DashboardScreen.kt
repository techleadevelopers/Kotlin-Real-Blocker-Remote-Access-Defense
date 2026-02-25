package com.blockremote.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockremote.ui.components.CyberButton
import com.blockremote.ui.components.NeonCard
import com.blockremote.viewmodel.BlockRemoteViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class RadarBlip(
    val angle: Float,
    val dist: Float,
    val size: Float,
    val isThreat: Boolean
)

@Composable
fun DashboardScreen(viewModel: BlockRemoteViewModel) {
    val state by viewModel.state.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state.isAlertMode) 3000 else 5000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_pulse"
    )

    val orbColor by animateColorAsState(
        targetValue = colorScheme.primary,
        animationSpec = tween(500),
        label = "radar_color"
    )

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.checkBillingStatus()
    }

    val blips = remember(state.isAlertMode) {
        val list = mutableListOf<RadarBlip>()
        for (i in 0 until 8) {
            list.add(
                RadarBlip(
                    angle = (Math.random() * 360f).toFloat(),
                    dist = (0.2f + Math.random().toFloat() * 0.65f),
                    size = 2f + Math.random().toFloat() * 2f,
                    isThreat = false
                )
            )
        }
        if (state.isAlertMode) {
            for (i in 0 until 4) {
                list.add(
                    RadarBlip(
                        angle = (Math.random() * 360f).toFloat(),
                        dist = (0.25f + Math.random().toFloat() * 0.55f),
                        size = 3f + Math.random().toFloat() * 3f,
                        isThreat = true
                    )
                )
            }
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "BLOCKREMOTE",
            style = MaterialTheme.typography.displayLarge,
            color = orbColor,
            textAlign = TextAlign.Center
        )

        Text(
            text = "SENTINEL DEFENSE SYSTEM",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                val maxR = (minOf(cx, cy)) - 8f
                val ringCount = 5
                val alertRed = Color(0xFFFF0040)
                val accentColor = if (state.isAlertMode) alertRed else orbColor

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.04f),
                            accentColor.copy(alpha = 0.015f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = maxR
                    ),
                    radius = maxR,
                    center = Offset(cx, cy)
                )

                for (i in 1..ringCount) {
                    val r = (maxR / ringCount) * i
                    val alpha = if (i == ringCount) 0.25f else 0.08f + (i * 0.02f)
                    drawCircle(
                        color = accentColor.copy(alpha = alpha),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = if (i == ringCount) 1.5.dp.toPx() else 0.5.dp.toPx())
                    )
                }

                for (a in 0 until 12) {
                    val ang = (a / 12f) * (2f * PI.toFloat())
                    drawLine(
                        color = accentColor.copy(alpha = 0.12f),
                        start = Offset(cx, cy),
                        end = Offset(
                            cx + cos(ang) * maxR,
                            cy + sin(ang) * maxR
                        ),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }

                rotate(sweepAngle) {
                    val segments = 40
                    val trailAngle = if (state.isAlertMode) 45f else 35f
                    for (i in 0 until segments) {
                        val t = i.toFloat() / segments
                        val startA = -trailAngle * t
                        val sweepA = -trailAngle / segments
                        val alpha = (1f - t) * (if (state.isAlertMode) 0.22f else 0.15f)
                        drawArc(
                            color = accentColor.copy(alpha = alpha),
                            startAngle = startA,
                            sweepAngle = sweepA,
                            useCenter = true,
                            topLeft = Offset(cx - maxR, cy - maxR),
                            size = Size(maxR * 2, maxR * 2)
                        )
                    }

                    drawLine(
                        color = accentColor.copy(alpha = 0.9f),
                        start = Offset(cx, cy),
                        end = Offset(cx + maxR, cy),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = accentColor.copy(alpha = 0.4f),
                        start = Offset(cx, cy),
                        end = Offset(cx + maxR, cy),
                        strokeWidth = 5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                blips.forEach { blip ->
                    var angleDiff = sweepAngle - blip.angle
                    while (angleDiff < 0) angleDiff += 360f
                    while (angleDiff > 360f) angleDiff -= 360f

                    val blipAlpha = if (angleDiff < 30f) {
                        (1f - angleDiff / 30f) * (if (blip.isThreat) 1.0f else 0.7f)
                    } else if (angleDiff < 180f) {
                        ((180f - angleDiff) / 180f) * 0.15f
                    } else {
                        0f
                    }

                    if (blipAlpha > 0.01f) {
                        val bAngle = blip.angle * (PI.toFloat() / 180f)
                        val bx = cx + cos(bAngle) * blip.dist * maxR
                        val by = cy + sin(bAngle) * blip.dist * maxR
                        val blipColor = if (blip.isThreat) alertRed else accentColor
                        val pulsedSize = blip.size + pulseAlpha * 1.5f

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    blipColor.copy(alpha = blipAlpha * 0.6f),
                                    blipColor.copy(alpha = blipAlpha * 0.2f),
                                    Color.Transparent
                                ),
                                center = Offset(bx, by),
                                radius = pulsedSize * 4.dp.toPx()
                            ),
                            radius = pulsedSize * 4.dp.toPx(),
                            center = Offset(bx, by)
                        )

                        drawCircle(
                            color = blipColor.copy(alpha = blipAlpha),
                            radius = pulsedSize.dp.toPx(),
                            center = Offset(bx, by)
                        )

                        if (blip.isThreat && blipAlpha > 0.3f) {
                            val crossR = (pulsedSize + 5f).dp.toPx()
                            drawLine(
                                color = alertRed.copy(alpha = blipAlpha * 0.5f),
                                start = Offset(bx - crossR, by),
                                end = Offset(bx + crossR, by),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = alertRed.copy(alpha = blipAlpha * 0.5f),
                                start = Offset(bx, by - crossR),
                                end = Offset(bx, by + crossR),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawCircle(
                                color = alertRed.copy(alpha = blipAlpha * 0.3f),
                                radius = crossR + 3.dp.toPx(),
                                center = Offset(bx, by),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                    }
                }

                val pulseRingAlpha = (1f - pulseAlpha) * 0.15f
                drawCircle(
                    color = accentColor.copy(alpha = pulseRingAlpha),
                    radius = maxR * pulseAlpha,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx())
                )

                drawCircle(
                    color = accentColor.copy(alpha = 0.9f),
                    radius = 4.dp.toPx(),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = accentColor.copy(alpha = 0.4f),
                    radius = 8.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                if (state.isAlertMode) {
                    val edgeAlpha = pulseAlpha * 0.12f
                    drawCircle(
                        color = alertRed.copy(alpha = edgeAlpha + 0.08f),
                        radius = maxR,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        NeonCard(
            title = "System Status",
            neonColor = orbColor,
            isAlert = state.isAlertMode
        ) {
            Text(
                text = if (state.isSystemSafe) "ALL SYSTEMS NOMINAL" else "⚠ THREAT DETECTED",
                style = MaterialTheme.typography.headlineMedium,
                color = colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (state.isSystemSafe)
                    "No unauthorized remote access detected.\nAll sensors operating within parameters."
                else
                    "Anomalous activity detected.\nCountermeasures engaged. Stand by.",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        NeonCard(title = "Telemetry Summary") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TelemetryRow("Threat Level", "${(state.threatLevel * 100).toInt()}%", colorScheme.primary)
                TelemetryRow("Sensors Active", "3/3", colorScheme.secondary)
                TelemetryRow("Accessibility Guard", "ARMED", colorScheme.primary)
                TelemetryRow("Network Monitor", "ACTIVE", colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        CyberButton(
            text = if (state.isAlertMode) "NEUTRALIZING..." else "SIMULAR AMEAÇA",
            isAlert = state.isAlertMode,
            onClick = {
                if (!state.isAlertMode) viewModel.simulateThreat()
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun TelemetryRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor
        )
    }
}
