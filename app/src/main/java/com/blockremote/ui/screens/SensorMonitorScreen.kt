package com.blockremote.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.blockremote.ui.components.NeonCard
import com.blockremote.viewmodel.BlockRemoteViewModel
import com.blockremote.viewmodel.SensorReading

@Composable
fun SensorMonitorScreen(viewModel: BlockRemoteViewModel) {
    val state by viewModel.state.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "SENSOR MONITOR",
            style = MaterialTheme.typography.headlineLarge,
            color = colorScheme.primary
        )
        Text(
            text = "Real-time telemetry feed",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        NeonCard(title = "Accelerometer X-Axis") {
            SensorWaveCanvas(
                readings = state.sensorReadings,
                valueSelector = { it.accelerometerX },
                lineColor = colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        NeonCard(title = "Accelerometer Y-Axis") {
            SensorWaveCanvas(
                readings = state.sensorReadings,
                valueSelector = { it.accelerometerY },
                lineColor = colorScheme.secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        NeonCard(title = "Accelerometer Z-Axis") {
            SensorWaveCanvas(
                readings = state.sensorReadings,
                valueSelector = { it.accelerometerZ - 9.81f },
                lineColor = colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        NeonCard(title = "Gyroscope") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SensorWaveCanvas(
                    readings = state.sensorReadings,
                    valueSelector = { it.gyroscopeX },
                    lineColor = colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
                SensorWaveCanvas(
                    readings = state.sensorReadings,
                    valueSelector = { it.gyroscopeY },
                    lineColor = colorScheme.secondary.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        NeonCard(title = "Sensor Statistics") {
            val lastReading = state.sensorReadings.lastOrNull()
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatRow("Accel X", lastReading?.accelerometerX ?: 0f, colorScheme.primary)
                StatRow("Accel Y", lastReading?.accelerometerY ?: 0f, colorScheme.primary)
                StatRow("Accel Z", lastReading?.accelerometerZ ?: 0f, colorScheme.primary)
                StatRow("Gyro X", lastReading?.gyroscopeX ?: 0f, colorScheme.primary)
                StatRow("Gyro Y", lastReading?.gyroscopeY ?: 0f, colorScheme.primary)
                StatRow("Gyro Z", lastReading?.gyroscopeZ ?: 0f, colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SensorWaveCanvas(
    readings: List<SensorReading>,
    valueSelector: (SensorReading) -> Float,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (readings.size < 2) return@Canvas

        val width = size.width
        val height = size.height
        val centerY = height / 2
        val maxVal = 5f

        val gridColor = lineColor.copy(alpha = 0.1f)
        for (i in 0..4) {
            val y = height * i / 4
            drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
        }
        drawLine(
            lineColor.copy(alpha = 0.2f),
            Offset(0f, centerY),
            Offset(width, centerY),
            strokeWidth = 1f
        )

        val path = Path()
        val step = width / (readings.size - 1).coerceAtLeast(1)

        readings.forEachIndexed { index, reading ->
            val value = valueSelector(reading)
            val x = index * step
            val y = centerY - (value / maxVal) * (height / 2)
            val clampedY = y.coerceIn(0f, height)

            if (index == 0) path.moveTo(x, clampedY)
            else path.lineTo(x, clampedY)
        }

        drawPath(
            path = path,
            color = lineColor.copy(alpha = 0.15f),
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun StatRow(label: String, value: Float, valueColor: Color) {
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
            text = String.format("%+.3f", value),
            style = MaterialTheme.typography.bodySmall,
            color = valueColor
        )
    }
}
