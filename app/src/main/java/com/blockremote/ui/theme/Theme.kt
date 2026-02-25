package com.blockremote.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CyberDarkScheme = darkColorScheme(
    primary = MatrixGreen,
    secondary = CyberLime,
    tertiary = CyberLime,
    background = PureBlack,
    surface = CarbonGrey,
    surfaceVariant = SurfaceVariant,
    onPrimary = PureBlack,
    onSecondary = PureBlack,
    onBackground = OffWhite,
    onSurface = OffWhite,
    outline = CardBorder,
    error = AlertRed
)

private val AlertScheme = darkColorScheme(
    primary = AlertRed,
    secondary = AlertRed,
    tertiary = AlertRedDark,
    background = PureBlack,
    surface = AlertRedDark,
    surfaceVariant = SurfaceVariant,
    onPrimary = OffWhite,
    onSecondary = OffWhite,
    onBackground = OffWhite,
    onSurface = OffWhite,
    outline = AlertRed,
    error = AlertRed
)

@Composable
fun BlockRemoteTheme(
    isAlert: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isAlert) AlertScheme else CyberDarkScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PureBlack.toArgb()
            window.navigationBarColor = PureBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BlockRemoteTypography,
        content = content
    )
}
