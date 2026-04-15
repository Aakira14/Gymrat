package com.gymrat.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymrat.app.ui.theme.GymRatColors
import kotlin.math.abs

@Composable
fun OnboardingLoadingScreen() {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            GymRatColors.VoidBlack,
            GymRatColors.VoidPurpleDeep,
            GymRatColors.VoidBlack
        )
    )

    VoidPatternBackdrop(background = gradient, patternAlpha = 0.16f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoaderWord()
        }
    }
}

@Composable
private fun LoaderWord() {
    val slices = 9
    val base = "Loading"

    val t = rememberInfiniteTransition(label = "scrolling")
    val x by t.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "x"
    )

    val shineAlpha = (0.35f + 0.65f * (1f - abs(x))).coerceIn(0.35f, 1f)

    Box(
        modifier = Modifier
            .width(260.dp)
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        for (i in 0 until slices) {
            val frac = (i + 1).toFloat() / slices.toFloat()
            val fontSize = (14f + (frac * 26f))
            val offset = ((i - (slices / 2f)) * 6f)
            val left = i / slices.toFloat()
            val right = (i + 1) / slices.toFloat()

            Text(
                text = base,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Black,
                color = GymRatColors.OnDark.copy(alpha = 0.04f),
                modifier = Modifier
                    .offset(x = offset.dp)
                    .drawWithContent {
                        val w = size.width
                        val h = size.height
                        clipRect(left = w * left, right = w * right, top = 0f, bottom = h) {
                            this@drawWithContent.drawContent()
                        }
                    }
            )

            Text(
                text = base,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Black,
                color = GymRatColors.OnDark.copy(alpha = 0.45f * shineAlpha),
                modifier = Modifier
                    .offset(x = offset.dp)
                    .drawWithContent {
                        val w = size.width
                        val h = size.height
                        clipRect(
                            left = w * left,
                            right = w * right,
                            top = 0f,
                            bottom = h
                        ) {
                            this@drawWithContent.drawContent()
                        }
                    }
            )
        }

        Box(
            modifier = Modifier
                .width(80.dp)
                .height(4.dp)
                .align(Alignment.BottomCenter)
                .drawWithContent {
                    val p = ((x + 1f) / 2f).coerceIn(0f, 1f)
                    drawRect(GymRatColors.OnDark.copy(alpha = 0.22f))
                    val w = size.width
                    val start = (w * (p - 0.45f)).coerceIn(0f, w)
                    val end = (w * (p + 0.45f)).coerceIn(0f, w)
                    clipRect(left = start, right = end) {
                        drawRect(GymRatColors.OnDark.copy(alpha = 0.75f))
                    }
                }
        )
    }
}
