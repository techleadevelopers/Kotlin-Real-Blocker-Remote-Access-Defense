package com.blockremote.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.blockremote.ui.components.NeonCard
import com.blockremote.ui.components.SentinelSwitch
import com.blockremote.viewmodel.BlockRemoteViewModel

@Composable
fun AppShieldScreen(viewModel: BlockRemoteViewModel) {
    val state by viewModel.state.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "APP SHIELD",
            style = MaterialTheme.typography.headlineLarge,
            color = colorScheme.primary
        )
        Text(
            text = "Accessibility service & permission guard",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        NeonCard(title = "Shield Summary") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShieldStat("Monitored", "${state.appPermissions.size}", colorScheme.primary)
                ShieldStat("Blocked", "${state.appPermissions.count { it.isBlocked }}", colorScheme.error)
                ShieldStat("Critical", "${state.appPermissions.count { it.riskLevel == "CRITICAL" }}", colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(state.appPermissions) { index, permission ->
                val riskColor = when (permission.riskLevel) {
                    "CRITICAL" -> colorScheme.error
                    "HIGH" -> colorScheme.error.copy(alpha = 0.7f)
                    "MEDIUM" -> colorScheme.secondary
                    else -> colorScheme.primary
                }

                NeonCard(
                    neonColor = riskColor,
                    isAlert = permission.riskLevel == "CRITICAL" && !permission.isBlocked
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = permission.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = permission.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            RiskBadge(permission.riskLevel, riskColor)
                        }

                        SentinelSwitch(
                            label = if (permission.isBlocked) "BLOCKED" else "ALLOWED",
                            subtitle = if (permission.isAccessibilityService) "Accessibility Service" else "Standard Permission",
                            checked = permission.isBlocked,
                            onCheckedChange = { viewModel.togglePermission(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShieldStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge,
            color = color
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun RiskBadge(level: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = level,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
