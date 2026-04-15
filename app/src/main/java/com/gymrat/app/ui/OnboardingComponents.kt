package com.gymrat.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.gymrat.app.ui.theme.GymRatColors

@Composable
fun GlowNextButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val t = rememberInfiniteTransition(label = "nextGlow")
    val a by t.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "a"
    )

    val shape = RoundedCornerShape(18.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        GymRatColors.VoidPurple.copy(alpha = a),
                        GymRatColors.NeonMint.copy(alpha = a * 0.7f),
                        GymRatColors.VoidPurple.copy(alpha = a)
                    )
                ),
                shape = shape
            ),
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = GymRatColors.VoidCard)
    ) {
        Text(text, color = MaterialTheme.colorScheme.primary, fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OptionPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(999.dp)
    val border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f))
    Card(
        onClick = onClick,
        shape = shape,
        border = border,
        colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard),
        modifier = Modifier.padding(end = 10.dp, bottom = 10.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        }
    }
}

@Composable
fun DualActionRow(
    leftText: String,
    rightText: String,
    rightEnabled: Boolean = true,
    onLeft: () -> Unit,
    onRight: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onLeft,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GymRatColors.VoidCard)
        ) {
            Text(leftText)
        }
        Spacer(Modifier.width(12.dp))
        GlowNextButton(
            text = rightText,
            modifier = Modifier.weight(1f),
            enabled = rightEnabled,
            onClick = onRight
        )
    }
}
