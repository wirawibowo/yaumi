package com.yaumi.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColors: ColorScheme = lightColorScheme(
    primary = RoyalPurple,
    onPrimary = Color.White,
    primaryContainer = Lavender100,
    onPrimaryContainer = Ink900,
    secondary = SoftViolet,
    onSecondary = Color.White,
    secondaryContainer = Lavender200,
    onSecondaryContainer = Ink700,
    tertiary = GoldDeep,
    onTertiary = Color.White,
    tertiaryContainer = CreamManuscript,
    onTertiaryContainer = Ink900,
    background = Lavender50,
    onBackground = Ink900,
    surface = SurfaceWhite,
    onSurface = Ink900,
    surfaceVariant = Lavender100,
    onSurfaceVariant = Ink500,
    outline = Lavender200,
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Lavender200,
    onPrimary = IndigoNight,
    primaryContainer = RoyalPurple,
    onPrimaryContainer = Lavender50,
    secondary = SoftViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4D356A),
    onSecondaryContainer = Lavender50,
    tertiary = GoldCrescent,
    onTertiary = IndigoNight,
    tertiaryContainer = Color(0xFF5D466C),
    onTertiaryContainer = CreamManuscript,
    background = Color(0xFF120A2E),
    onBackground = Color(0xFFECE3EF),
    surface = Color(0xFF1B1145),
    onSurface = Color(0xFFECE3EF),
    surfaceVariant = Color(0xFF2D1F60),
    onSurfaceVariant = Lavender200,
    outline = Color(0xFF6B5FA3),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun YaumiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = YaumiTypography,
            shapes = YaumiShapes,
            content = content
        )
    }
}
