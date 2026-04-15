package com.gymrat.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymrat.app.ui.theme.GymRatColors
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    onReady: () -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            GymRatColors.VoidBlack,
            GymRatColors.VoidPurpleDeep,
            GymRatColors.VoidBlack
        )
    )

    val fullText = "WELCOME TO GYMMORA"
    var shownChars by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        shownChars = 0
        while (shownChars < fullText.length) {
            delay(55)
            shownChars += 1
        }
    }

    val t = rememberInfiniteTransition(label = "readyPulse")
    val scale by t.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by t.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val density = LocalDensity.current
    val bounce by t.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(22.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.65f))
        Text(
            text = fullText.take(shownChars.coerceIn(0, fullText.length)),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = GymRatColors.OnDark,
            letterSpacing = 1.2.sp
        )

        Spacer(Modifier.weight(1f))

        val shape = RoundedCornerShape(20.dp)
        Button(
            onClick = onReady,
            shape = shape,
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .widthIn(max = 320.dp)
                .height(58.dp)
                .scale(scale)
                .graphicsLayer {
                    translationY = with(density) { (bounce * 6.dp.toPx()) }
                }
                .clip(shape)
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GymRatColors.VoidPurple.copy(alpha = glowAlpha),
                            GymRatColors.NeonMint.copy(alpha = glowAlpha * 0.75f),
                            GymRatColors.VoidPurple.copy(alpha = glowAlpha)
                        )
                    ),
                    shape = shape
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = GymRatColors.VoidPurpleDeep
            )
        ) {
            Text(
                "I'm Ready!! --->",
                fontWeight = FontWeight.Black,
                color = GymRatColors.OnDark
            )
        }

        Spacer(Modifier.weight(0.25f))
    }
}
