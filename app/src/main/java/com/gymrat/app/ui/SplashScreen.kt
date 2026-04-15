package com.gymrat.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.gymrat.app.ui.theme.GymRatColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen() {
    val gradient = Brush.radialGradient(
        colors = listOf(GymRatColors.VoidPurpleDeep, GymRatColors.VoidBlack),
        radius = 900f
    )

    val full = "GymRat"
    var shown by remember { mutableIntStateOf(0) }
    var cursorOn by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        shown = 0
        delay(150)
        while (shown < full.length) {
            shown += 1
            delay(110)
        }
        while (true) {
            cursorOn = !cursorOn
            delay(420)
        }
    }

    val glowAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 700, easing = LinearEasing),
        label = "glow"
    )

    VoidPatternBackdrop(background = gradient, patternAlpha = 0.16f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val display = buildString {
                append(full.take(shown))
                if (shown < full.length || cursorOn) append("▍")
            }

            Text(
                text = display,
                style = TextStyle(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = GymRatColors.OnDark,
                    shadow = Shadow(
                        color = Color(0xFFB35CFF).copy(alpha = 0.7f * glowAlpha),
                        blurRadius = 28f
                    )
                )
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Loading…",
                color = GymRatColors.OnDark.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}
