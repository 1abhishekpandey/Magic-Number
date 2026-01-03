package com.abhishek.magicnumber.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Mystical purple and gold color scheme for the Magic Number app.
 *
 * This is a dark-only theme to maintain the magical, mysterious atmosphere.
 */
private val MysticalColorScheme = darkColorScheme(
    // Primary colors - Gold for main interactive elements
    primary = Gold,
    onPrimary = Purple900,
    primaryContainer = GoldDark,
    onPrimaryContainer = White,

    // Secondary colors - Light gold for secondary elements
    secondary = GoldLight,
    onSecondary = Purple900,
    secondaryContainer = Purple600,
    onSecondaryContainer = GoldLight,

    // Tertiary colors - Purple accent
    tertiary = Purple500,
    onTertiary = White,
    tertiaryContainer = Purple700,
    onTertiaryContainer = Purple300,

    // Background colors - Deep purple
    background = Purple900,
    onBackground = White,

    // Surface colors - Slightly lighter purple for cards
    surface = Purple800,
    onSurface = White,
    surfaceVariant = Purple700,
    onSurfaceVariant = WhiteAlpha80,

    // Outline colors
    outline = Purple500,
    outlineVariant = Purple600,

    // Error colors
    error = Color(0xFFFF6B6B),
    onError = White,

    // Inverse colors (for snackbars, etc.)
    inverseSurface = GoldLight,
    inverseOnSurface = Purple900,
    inversePrimary = Purple700
)

/**
 * Main theme composable for the Magic Number app.
 *
 * Always uses dark mystical theme regardless of system settings.
 * Sets status bar color to match the theme.
 */
@Composable
fun MagicNumberTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = MysticalColorScheme

    // Set status bar color to match background
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
