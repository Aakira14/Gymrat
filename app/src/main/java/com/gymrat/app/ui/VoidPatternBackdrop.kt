package com.gymrat.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun VoidPatternBackdrop(
    modifier: Modifier = Modifier,
    background: Brush? = null,
    patternAlpha: Float = 0.22f,
    content: @Composable BoxScope.() -> Unit
) {
    val bg = background ?: Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background,
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
    ) {
        VoidPatternOverlay(
            modifier = Modifier.fillMaxSize(),
            alpha = patternAlpha.coerceIn(0f, 1f)
        )
        content()
    }
}

@Composable
private fun VoidPatternOverlay(
    modifier: Modifier,
    alpha: Float
) {
    val primary = MaterialTheme.colorScheme.primary.copy(alpha = 0.70f * alpha)
    val secondary = MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f * alpha)
    val stars = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.22f * alpha)

    Canvas(modifier = modifier) {
        if (size.minDimension <= 1f) return@Canvas

        val seed = (size.width.toInt() * 31 + size.height.toInt()).xor(0x0BADC0DE)
        val rng = Random(seed)

        drawStars(rng = rng, tint = stars)
        drawPatternGlyphs(rng = rng, a = primary, b = secondary)
    }
}

private fun DrawScope.drawStars(rng: Random, tint: Color) {
    val count = ((min(size.width, size.height) / 8f).toInt()).coerceIn(80, 200)
    repeat(count) {
        val x = rng.nextFloat() * size.width
        val y = rng.nextFloat() * size.height
        val r = rng.nextFloat().let { 0.6f + it * 1.8f }
        drawCircle(
            color = tint.copy(alpha = tint.alpha * (0.35f + rng.nextFloat() * 0.65f)),
            radius = r,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawPatternGlyphs(rng: Random, a: Color, b: Color) {
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    val spacing = max(size.minDimension * 0.22f, 140.dp.toPx())
    val cols = (size.width / spacing).toInt().coerceIn(2, 6)
    val rows = (size.height / spacing).toInt().coerceIn(3, 10)

    val startX = -spacing * 0.2f
    val startY = -spacing * 0.2f

    for (r in 0..rows) {
        for (c in 0..cols) {
            val base = Offset(
                x = startX + c * spacing + rng.nextFloat() * spacing * 0.35f,
                y = startY + r * spacing + rng.nextFloat() * spacing * 0.35f
            )

            val glyphSize = (spacing * (0.40f + rng.nextFloat() * 0.35f))
            val rot = (rng.nextFloat() - 0.5f) * 0.45f
            val tint = if ((r + c) % 2 == 0) a else b

            rotate(degrees = rot * 180f / Math.PI.toFloat(), pivot = base) {
                if (rng.nextBoolean()) {
                    drawBarbell(center = base, sizePx = glyphSize, color = tint, stroke = stroke)
                } else {
                    drawDumbbell(center = base, sizePx = glyphSize, color = tint, stroke = stroke)
                }
            }
        }
    }
}

private fun DrawScope.drawBarbell(
    center: Offset,
    sizePx: Float,
    color: Color,
    stroke: Stroke
) {
    val half = sizePx * 0.55f
    val rodHalf = sizePx * 0.38f
    val plateOuter = sizePx * 0.18f
    val plateInner = sizePx * 0.12f

    val left = Offset(center.x - rodHalf, center.y)
    val right = Offset(center.x + rodHalf, center.y)

    drawLine(color = color, start = left, end = right, strokeWidth = stroke.width, cap = stroke.cap)

    fun plates(x: Float) {
        drawCircle(color = color, radius = plateOuter, center = Offset(x - half * 0.10f, center.y), style = stroke)
        drawCircle(color = color, radius = plateInner, center = Offset(x + half * 0.05f, center.y), style = stroke)
        drawLine(
            color = color,
            start = Offset(x, center.y - plateOuter * 0.95f),
            end = Offset(x, center.y + plateOuter * 0.95f),
            strokeWidth = stroke.width,
            cap = stroke.cap
        )
    }

    plates(left.x - plateOuter * 0.65f)
    plates(right.x + plateOuter * 0.65f)
}

private fun DrawScope.drawDumbbell(
    center: Offset,
    sizePx: Float,
    color: Color,
    stroke: Stroke
) {
    val handleHalf = sizePx * 0.26f
    val hexRadius = sizePx * 0.18f

    val left = Offset(center.x - handleHalf, center.y)
    val right = Offset(center.x + handleHalf, center.y)

    drawLine(color = color, start = left, end = right, strokeWidth = stroke.width, cap = stroke.cap)
    drawPath(path = hexPath(center = Offset(left.x - hexRadius * 1.05f, left.y), radius = hexRadius), color = color, style = stroke)
    drawPath(path = hexPath(center = Offset(right.x + hexRadius * 1.05f, right.y), radius = hexRadius), color = color, style = stroke)
}

private fun hexPath(center: Offset, radius: Float): Path {
    val path = Path()
    val points = (0 until 6).map { i ->
        val a = (Math.PI.toFloat() * 2f) * (i / 6f) + (Math.PI.toFloat() / 6f)
        Offset(
            x = center.x + cos(a) * radius,
            y = center.y + sin(a) * radius
        )
    }
    path.moveTo(points[0].x, points[0].y)
    for (i in 1 until points.size) path.lineTo(points[i].x, points[i].y)
    path.close()
    return path
}
