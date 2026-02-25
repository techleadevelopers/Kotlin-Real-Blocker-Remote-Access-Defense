package com.blockremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.blockremote.navigation.BlockRemoteNavGraph
import com.blockremote.navigation.screens
import com.blockremote.ui.components.SubscriptionOverlay
import com.blockremote.ui.theme.BlockRemoteTheme
import com.blockremote.ui.theme.CarbonGrey
import com.blockremote.ui.theme.MatrixGreen
import com.blockremote.ui.theme.OffWhite
import com.blockremote.ui.theme.PureBlack
import com.blockremote.viewmodel.BlockRemoteViewModel
import com.blockremote.viewmodel.SystemLicenseState

class MainActivity : ComponentActivity() {
    private val viewModel: BlockRemoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.state.collectAsState()

            BlockRemoteTheme(isAlert = state.isAlertMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val grayscaleMatrix = remember {
                    ColorMatrix(
                        floatArrayOf(
                            0.33f, 0.33f, 0.33f, 0f, 0f,
                            0.33f, 0.33f, 0.33f, 0f, 0f,
                            0.33f, 0.33f, 0.33f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        containerColor = PureBlack,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (state.isSystemDeactivated) {
                                    Modifier.drawWithContent {
                                        drawIntoCanvas {
                                            val paint = Paint().apply {
                                                colorFilter = ColorFilter.colorMatrix(grayscaleMatrix)
                                            }
                                        }
                                        drawContent()
                                    }
                                } else Modifier
                            ),
                        bottomBar = {
                            CyberBottomBar(
                                currentRoute = currentRoute,
                                isDeactivated = state.isSystemDeactivated,
                                webSocketConnected = state.webSocketState == com.blockremote.data.network.WebSocketState.AUTHENTICATED ||
                                    state.webSocketState == com.blockremote.data.network.WebSocketState.CONNECTED,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .background(PureBlack)
                        ) {
                            BlockRemoteNavGraph(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = state.showPaywall,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        val isLocked = state.licenseState == SystemLicenseState.LOCKED

                        BackHandler(enabled = isLocked) {}

                        SubscriptionOverlay(
                            trialDaysRemaining = state.trialDaysRemaining,
                            isLocked = isLocked,
                            onSubscribe = {
                                viewModel.addLogEntry("INFO", "BILLING", "Subscription flow initiated")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CyberBottomBar(
    currentRoute: String?,
    isDeactivated: Boolean,
    webSocketConnected: Boolean,
    onNavigate: (String) -> Unit
) {
    Column {
        if (webSocketConnected || isDeactivated) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDeactivated) CarbonGrey.copy(alpha = 0.5f) else CarbonGrey)
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDeactivated) OffWhite.copy(alpha = 0.3f)
                                else MatrixGreen
                            )
                    )
                    Text(
                        text = if (isDeactivated) "OFFLINE" else "SENTINEL RELAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDeactivated) OffWhite.copy(alpha = 0.3f) else MatrixGreen,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize
                    )
                }
                Text(
                    text = if (isDeactivated) "DEACTIVATED" else "ZERO-TRUST",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDeactivated) OffWhite.copy(alpha = 0.3f) else MatrixGreen.copy(alpha = 0.5f),
                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(CarbonGrey)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            screens.forEach { screen ->
                val isSelected = currentRoute == screen.route

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(screen.route) }
                        .padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MatrixGreen else PureBlack
                            )
                    )
                    Text(
                        text = screen.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MatrixGreen else OffWhite.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
