package com.pushtofinance.infinapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Akcenty
val AccentPurple = Color(0xFF7C4DFF)
val AccentTeal = Color(0xFF00E5FF)
val SuccessGreen = Color(0xFF4CAF50)
val WarningOrange = Color(0xFFFF9800)
val ErrorRed = Color(0xFFEF5350)

// Ciemny motyw
val DarkBg = Color(0xFF0E0F12)
val DarkSurf = Color(0xFF1A1B20)
val DarkSurfVar = Color(0xFF24262C)
val DarkTxtPri = Color(0xFFF5F5F7)
val DarkTxtSec = Color(0xFFB0B2B8)
val DarkTxtMuted = Color(0xFF70727A)

// Jasny motyw
val LightBg = Color(0xFFF6F6F8)
val LightSurf = Color(0xFFFFFFFF)
val LightSurfVar = Color(0xFFEDEDF1)
val LightTxtPri = Color(0xFF1A1A1F)
val LightTxtSec = Color(0xFF66666E)
val LightTxtMuted = Color(0xFF9999A0)

private val DarkColors = darkColorScheme(
    primary = AccentPurple,
    onPrimary = Color.White,
    primaryContainer = AccentPurple.copy(alpha = 0.2f),
    onPrimaryContainer = Color(0xFFD6C7FF),
    secondary = AccentTeal,
    onSecondary = Color(0xFF00282E),
    secondaryContainer = AccentTeal.copy(alpha = 0.14f),
    onSecondaryContainer = Color(0xFFD4F7FF),
    background = DarkBg,
    onBackground = DarkTxtPri,
    surface = DarkSurf,
    onSurface = DarkTxtPri,
    surfaceVariant = DarkSurfVar,
    onSurfaceVariant = DarkTxtSec,
    outline = Color(0xFF3A3D45),
    error = ErrorRed,
    onError = Color.White
)

private val LightColors = lightColorScheme(
    primary = AccentPurple,
    onPrimary = Color.White,
    primaryContainer = AccentPurple.copy(alpha = 0.15f),
    onPrimaryContainer = Color(0xFF4A2B9E),
    secondary = Color(0xFF00A5BB),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB9F3FA),
    onSecondaryContainer = Color(0xFF00363C),
    background = LightBg,
    onBackground = LightTxtPri,
    surface = LightSurf,
    onSurface = LightTxtPri,
    surfaceVariant = LightSurfVar,
    onSurfaceVariant = LightTxtSec,
    outline = Color(0xFFD9D9E0),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun PushToFinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PtfTypography,
        content = content
    )
}