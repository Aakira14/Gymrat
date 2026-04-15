package com.gymrat.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymrat.app.data.WorkoutSession
import com.gymrat.app.ui.theme.GymRatColors
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

private enum class TrackMetric { Weight, Sets }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TrackMeScreen(
    sessions: List<WorkoutSession>,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val gradient = Brush.verticalGradient(
        colors = listOf(
            GymRatColors.VoidBlack,
            GymRatColors.VoidPurpleDeep,
            GymRatColors.VoidBlack
        )
    )

    val today = remember { LocalDate.now(ZoneId.systemDefault()) }
    val days = remember(today) { (13 downTo 0).map { today.minusDays(it.toLong()) } }

    var metric by remember { mutableStateOf(TrackMetric.Weight) }
    var metricMenuOpen by remember { mutableStateOf(false) }

    val series = remember(sessions, days, metric) {
        buildSeries(
            sessions = sessions,
            days = days,
            metric = metric
        )
    }

    VoidPatternBackdrop(background = gradient) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(gradient)
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                        }
                        Column {
                            Text(
                                text = today.year.toString(),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Track Me",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard),
                        onClick = { metricMenuOpen = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (metric == TrackMetric.Weight) "Weight" else "Sets",
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Outlined.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            DropdownMenu(
                                expanded = metricMenuOpen,
                                onDismissRequest = { metricMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Weight") },
                                    onClick = {
                                        metric = TrackMetric.Weight
                                        metricMenuOpen = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sets") },
                                    onClick = {
                                        metric = TrackMetric.Sets
                                        metricMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Average by body part (last 14 days)",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard)
                ) {
                    if (series.all { it.points.all { p -> p == null } }) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("No data yet", fontWeight = FontWeight.Bold)
                            Text(
                                "Complete a few challenges (and enter weight) to unlock graphs.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            )
                        }
                    } else {
                        LineChart(
                            days = days,
                            series = series,
                            ySuffix = if (metric == TrackMetric.Weight) "kg" else "sets",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(14.dp)
                        )
                    }
                }

                LegendRow(series = series)

                val summary = remember(series, metric) { buildSummary(series, metric) }
                if (summary.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard)
                    ) {
                        Text(
                            text = summary,
                            modifier = Modifier.padding(14.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

private data class MetricSeries(
    val label: String,
    val color: Color,
    val points: List<Float?>
)

private fun buildSummary(series: List<MetricSeries>, metric: TrackMetric): String {
    val lastValues = series.mapNotNull { s -> s.points.lastOrNull()?.let { v -> s.label to v } }
    if (lastValues.isEmpty()) return ""
    val (bestLabel, bestValue) = lastValues.maxBy { it.second }
    val unit = if (metric == TrackMetric.Weight) "kg" else "sets"
    val valueText = if (metric == TrackMetric.Weight) "%.1f".format(bestValue) else bestValue.toInt().toString()
    return "Top today: $bestLabel • $valueText $unit"
}

private fun buildSeries(
    sessions: List<WorkoutSession>,
    days: List<LocalDate>,
    metric: TrackMetric
): List<MetricSeries> {
    val palette = listOf(
        GymRatColors.VoidPurple,
        GymRatColors.NeonMint,
        Color(0xFFFF6BD6),
        Color(0xFF7CFFEE),
        Color(0xFFFFC857)
    )

    val categories = listOf(
        "Upper Body",
        "Back",
        "Legs",
        "Core",
        "Cardio"
    )

    val pointsByCategory: Map<String, List<Float?>> = categories.associateWith { category ->
        days.map { d ->
            val dayIso = d.toString()
            val items = sessions.filter { it.dateIso == dayIso && categorizeBodyPart(it) == category }
            when (metric) {
                TrackMetric.Weight -> {
                    val weights = items.mapNotNull { it.weightKg?.toFloat() }.filter { it > 0f }
                    if (weights.isEmpty()) null else weights.average().toFloat()
                }
                TrackMetric.Sets -> {
                    val sets = items.mapNotNull { it.setsDone?.toFloat() }.filter { it > 0f }
                    if (sets.isEmpty()) null else sets.average().toFloat()
                }
            }
        }
    }

    return categories.mapIndexed { idx, label ->
        MetricSeries(
            label = label,
            color = palette[idx % palette.size],
            points = pointsByCategory[label] ?: days.map { null }
        )
    }
}

private fun categorizeBodyPart(session: WorkoutSession): String {
    val t = (session.workoutTitle.ifBlank { session.workoutId }).lowercase(Locale.getDefault())

    fun has(vararg parts: String): Boolean = parts.any { t.contains(it) }

    return when {
        has("squat", "lunge", "leg ", "leg-", "calf", "hip thrust", "deadlift", "romanian", "step-up") -> "Legs"
        has("row", "pulldown", "pull-up", "pull ups", "chin-up", "chin ups", "back", "renegade") -> "Back"
        has("plank", "crunch", "sit-up", "sit ups", "situps", "russian", "core", "twist", "side bend") -> "Core"
        has("running", "cycling", "jump rope", "rower", "rowing", "stair", "elliptical", "cardio", "burpee", "mountain climber") -> "Cardio"
        else -> "Upper Body"
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LegendRow(series: List<MetricSeries>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        series.forEach { s ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Canvas(Modifier.size(10.dp)) { drawCircle(s.color) }
                Text(
                    text = s.label,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun LineChart(
    days: List<LocalDate>,
    series: List<MetricSeries>,
    ySuffix: String,
    modifier: Modifier = Modifier
) {
    val allValues = series.flatMap { it.points }.filterNotNull()
    val rawMin = allValues.minOrNull() ?: 0f
    val rawMax = allValues.maxOrNull() ?: 1f
    val yMin = if (rawMin == rawMax) (rawMin - 1f) else rawMin
    val yMax = if (rawMin == rawMax) (rawMax + 1f) else rawMax
    val pad = (yMax - yMin) * 0.12f
    val minY = yMin - pad
    val maxY = yMax + pad

    val fmt = remember { DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()) }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val leftPad = 34f
        val bottomPad = 26f
        val topPad = 10f

        val chartW = max(1f, w - leftPad)
        val chartH = max(1f, h - bottomPad - topPad)

        // grid
        val gridColor = Color.White.copy(alpha = 0.08f)
        val axisColor = Color.White.copy(alpha = 0.14f)
        val ticks = 4
        for (i in 0..ticks) {
            val y = topPad + (chartH * i / ticks.toFloat())
            drawLine(gridColor, Offset(leftPad, y), Offset(w, y), strokeWidth = 1f)
        }
        drawLine(axisColor, Offset(leftPad, topPad), Offset(leftPad, topPad + chartH), strokeWidth = 1.5f)
        drawLine(axisColor, Offset(leftPad, topPad + chartH), Offset(w, topPad + chartH), strokeWidth = 1.5f)

        fun xFor(index: Int): Float {
            val denom = max(1, days.size - 1)
            return leftPad + (chartW * index.toFloat() / denom.toFloat())
        }

        fun yFor(value: Float): Float {
            val pct = ((value - minY) / (maxY - minY)).coerceIn(0f, 1f)
            return topPad + chartH * (1f - pct)
        }

        // subtle fill for depth
        series.take(3).forEach { s ->
            val fillPath = Path()
            var started = false
            var lastX = 0f
            s.points.forEachIndexed { idx, v ->
                if (v == null) {
                    started = false
                    return@forEachIndexed
                }
                val x = xFor(idx)
                val y = yFor(v)
                if (!started) {
                    fillPath.moveTo(x, topPad + chartH)
                    fillPath.lineTo(x, y)
                    started = true
                } else {
                    fillPath.lineTo(x, y)
                }
                lastX = x
            }
            if (started) {
                fillPath.lineTo(lastX, topPad + chartH)
                fillPath.close()
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(s.color.copy(alpha = 0.18f), Color.Transparent),
                        startY = topPad,
                        endY = topPad + chartH
                    )
                )
            }
        }

        // series lines + dots
        series.forEach { s ->
            val path = Path()
            var started = false

            s.points.forEachIndexed { idx, v ->
                if (v == null) {
                    started = false
                    return@forEachIndexed
                }

                val p = Offset(xFor(idx), yFor(v))
                if (!started) {
                    path.moveTo(p.x, p.y)
                    started = true
                } else {
                    path.lineTo(p.x, p.y)
                }
            }

            drawPath(
                path = path,
                color = s.color.copy(alpha = 0.9f),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            // dots
            s.points.forEachIndexed { idx, v ->
                if (v == null) return@forEachIndexed
                val p = Offset(xFor(idx), yFor(v))
                drawCircle(color = s.color, radius = 5f, center = p)
            }
        }

        // x labels: first / middle / last
        val labelColor = Color.White.copy(alpha = 0.7f)
        val indices = listOf(0, (days.size - 1) / 2, days.size - 1).distinct()
        indices.forEach { idx ->
            val txt = fmt.format(days[idx])
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(180, 255, 255, 255)
                    textSize = 24f
                    isAntiAlias = true
                }
                drawText(
                    txt,
                    xFor(idx) - (paint.measureText(txt) / 2f),
                    topPad + chartH + 22f,
                    paint
                )
            }
        }

        // y labels: top and bottom
        val topText = if (maxY < 10f) "%.1f $ySuffix".format(maxY) else "${maxY.toInt()} $ySuffix"
        val bottomText = if (minY < 10f) "%.1f $ySuffix".format(minY) else "${minY.toInt()} $ySuffix"
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(150, 255, 255, 255)
                textSize = 22f
                isAntiAlias = true
            }
            drawText(topText, 0f, topPad + 22f, paint)
            drawText(bottomText, 0f, topPad + chartH, paint)
        }
    }
}
