package com.gymrat.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = GymRatColors.VoidPurple,
    secondary = GymRatColors.NeonMint,
    background = GymRatColors.VoidBlack,
    surface = GymRatColors.VoidCard,
    surfaceVariant = GymRatColors.VoidCard.copy(alpha = 0.7f),
    onPrimary = GymRatColors.OnDark,
    onSecondary = GymRatColors.OnDark,
    onBackground = GymRatColors.OnDark,
    onSurface = GymRatColors.OnDark
)

@Composable
fun GymRatTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography,
        content = content
    )
}
