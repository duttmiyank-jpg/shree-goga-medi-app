package com.example.miyank.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LuxuryColorScheme = darkColorScheme(
    primary = AccentGolden,
    onPrimary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    secondary = AccentGolden,
    onSecondary = BackgroundDark,
    tertiary = TextMuted
)

@Composable
fun MiyankTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We use the luxury dark theme for both cases to maintain the branded look
    val colorScheme = LuxuryColorScheme

    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
}
