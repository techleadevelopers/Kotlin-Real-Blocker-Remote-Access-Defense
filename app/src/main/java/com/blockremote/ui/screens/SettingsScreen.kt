package com.blockremote.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blockremote.ui.components.NeonCard
import com.blockremote.ui.components.SentinelSwitch
import com.blockremote.viewmodel.BlockRemoteViewModel

@Composable
fun SettingsScreen(viewModel: BlockRemoteViewModel) {
    val state by viewModel.state.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.headlineLarge,
            color = colorScheme.primary
        )
        Text(
            text = "Detection & sensitivity configuration",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        NeonCard(title = "Detection Thresholds") {
            Column {
                CyberSlider(
                    label = "Accessibility Detection Threshold",
                    value = state.accessibilityThreshold,
                    onValueChange = { viewModel.updateAccessibilityThreshold(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                CyberSlider(
                    label = "Sensor Sensitivity",
                    value = state.sensorSensitivity,
                    onValueChange = { viewModel.updateSensorSensitivity(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        NeonCard(title = "Module Control") {
            Column {
                SentinelSwitch(
                    label = "Motion Detection",
                    subtitle = "Accelerometer & gyroscope anomaly analysis",
                    checked = state.motionDetectionEnabled,
                    onCheckedChange = { viewModel.toggleMotionDetection(it) }
                )

                SentinelSwitch(
                    label = "Network Monitor",
                    subtitle = "Outbound connection traffic analysis",
                    checked = state.networkMonitorEnabled,
                    onCheckedChange = { viewModel.toggleNetworkMonitor(it) }
                )

                SentinelSwitch(
                    label = "Accessibility Guard",
                    subtitle = "Service injection & overlay detection",
                    checked = state.accessibilityGuardEnabled,
                    onCheckedChange = { viewModel.toggleAccessibilityGuard(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        NeonCard(title = "Connection") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow("Relay", state.webSocketState.name)
                InfoRow("Protocol", "Zero-Trust WSS")
                InfoRow("License", state.licenseState.name)
                InfoRow("Heartbeat", "30s interval")
                InfoRow("API", "api.blockremote.io/v1")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        NeonCard(title = "System Info") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow("Version", "2.0.0-alpha")
                InfoRow("Engine", "Sentinel Core v3")
                InfoRow("Sensor Polling", "20Hz (GAME)")
                InfoRow("Log Buffer", "200 entries")
                InfoRow("Architecture", "Clean MVVM")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun CyberSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.primary
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.1f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = colorScheme.primary,
                activeTrackColor = colorScheme.primary,
                inactiveTrackColor = colorScheme.surface,
                activeTickColor = colorScheme.secondary,
                inactiveTickColor = colorScheme.background
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.secondary
        )
    }
}
