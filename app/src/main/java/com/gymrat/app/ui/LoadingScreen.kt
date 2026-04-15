package com.gymrat.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gymrat.app.ui.theme.GymRatColors

@Composable
fun LoadingScreen() {
    val gradient = Brush.radialGradient(
        colors = listOf(
            GymRatColors.VoidPurple,
            GymRatColors.VoidBlack
        ),
        radius = 900f
    )

    VoidPatternBackdrop(background = gradient, patternAlpha = 0.14f) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VoidPreloader()
            Spacer(Modifier.height(18.dp))
            Text("Logging in...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun VoidPreloader() {
    val shape = GenericShape { size, _ ->
        val pts = listOf(
            0.50f to 0.00f,
            0.61f to 0.35f,
            0.98f to 0.35f,
            0.68f to 0.57f,
            0.79f to 0.91f,
            0.50f to 0.70f,
            0.21f to 0.91f,
            0.32f to 0.57f,
            0.02f to 0.35f,
            0.39f to 0.35f
        )
        moveTo(size.width * pts[0].first, size.height * pts[0].second)
        for (i in 1 until pts.size) {
            lineTo(size.width * pts[i].first, size.height * pts[i].second)
        }
        close()
    }

    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Crack(shape = shape, size = 46.dp, durationMs = 6000, delayMs = 0, tint = GymRatColors.Crack)
        Crack(shape = shape, size = 56.dp, durationMs = 6000, delayMs = 1000, tint = GymRatColors.Crack.copy(alpha = 0.85f))
        Crack(shape = shape, size = 66.dp, durationMs = 6000, delayMs = 1500, tint = GymRatColors.Crack.copy(alpha = 0.70f))
        Crack(shape = shape, size = 76.dp, durationMs = 6000, delayMs = 2000, tint = GymRatColors.Crack.copy(alpha = 0.55f))
        Crack(shape = shape, size = 86.dp, durationMs = 6000, delayMs = 2500, tint = GymRatColors.Crack.copy(alpha = 0.40f))
    }
}

@Composable
private fun Crack(
    shape: GenericShape,
    size: Dp,
    durationMs: Int,
    delayMs: Int,
    tint: Color
) {
    val transition = rememberInfiniteTransition(label = "crack")
    val rotation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, delayMillis = delayMs, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                rotationZ = rotation.value
                shadowElevation = 8f
            }
            .clip(shape)
            .background(tint)
    )
}
