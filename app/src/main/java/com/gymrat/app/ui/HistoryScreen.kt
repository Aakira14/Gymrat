package com.gymrat.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymrat.app.data.WorkoutSession
import com.gymrat.app.ui.theme.GymRatColors
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    sessions: List<WorkoutSession>,
    initialDateIso: String? = null,
    onBack: () -> Unit
) {
    var trackMeOpen by remember { mutableStateOf(false) }
    BackHandler(onBack = {
        if (trackMeOpen) trackMeOpen = false else onBack()
    })

    val today = LocalDate.now(ZoneId.systemDefault())
    val latestDate = sessions.firstOrNull()?.dateIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val initialDate = initialDateIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    var selectedDate by remember(initialDateIso, sessions) { mutableStateOf(initialDate ?: latestDate ?: today) }

    val sessionsForDate = remember(sessions, selectedDate) {
        sessions.filter { it.dateIso == selectedDate.toString() }
    }

    val dayLabel = remember(selectedDate, today) { humanDayLabel(selectedDate, today) }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            GymRatColors.VoidBlack,
            GymRatColors.VoidPurpleDeep,
            GymRatColors.VoidBlack
        )
    )

    if (trackMeOpen) {
        TrackMeScreen(
            sessions = sessions,
            onBack = { trackMeOpen = false }
        )
        return
    }

    VoidPatternBackdrop(background = gradient) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
                                text = selectedDate.year.toString(),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "History",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Button(onClick = onBack, shape = RoundedCornerShape(14.dp)) { Text("Close") }
                }
            },
            content = { padding ->
                HistoryBody(
                    sessionsForDate = sessionsForDate,
                    selectedDate = selectedDate,
                    selectedDateLabel = dayLabel,
                    today = today,
                    onPrevDay = { selectedDate = selectedDate.minusDays(1) },
                    onNextDay = { selectedDate = selectedDate.plusDays(1) },
                    onOpenTrackMe = { trackMeOpen = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryBody(
    sessionsForDate: List<WorkoutSession>,
    selectedDate: LocalDate,
    selectedDateLabel: String,
    today: LocalDate,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onOpenTrackMe: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard),
                onClick = onOpenTrackMe
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Track me with graph…", fontWeight = FontWeight.Black)
                        Text(
                            "Weight + sets progress by body part",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevDay) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "Previous day")
                    }

                    Text(
                        text = selectedDateLabel,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onNextDay,
                        enabled = selectedDate.isBefore(today)
                    ) {
                        Icon(Icons.Outlined.ChevronRight, contentDescription = "Next day")
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (sessionsForDate.isEmpty()) {
                    item {
                        Text(
                            "No workouts logged for this date.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    items(sessionsForDate) { s ->
                        HistorySessionCard(session = s)
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun HistorySessionCard(session: WorkoutSession) {
    val minutes = (session.durationSec / 60f).coerceAtLeast(0f)
    val minLabel = if (minutes < 1f) "<1" else minutes.toInt().toString()
    val setsLabel = session.setsDone?.let { " • $it sets" }.orEmpty()
    val repsLabel = session.repsDone?.let { " • $it reps" }.orEmpty()
    val weightLabel = session.weightKg?.let { " • ${it}kg" }.orEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(session.workoutTitle, fontWeight = FontWeight.SemiBold)
            Text(
                text = "$minLabel min$setsLabel$repsLabel$weightLabel",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${session.caloriesBurned} kcal", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                Text("+${session.xpGained} XP", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
            }
        }
    }
}

private fun humanDayLabel(date: LocalDate, today: LocalDate): String {
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        today.plusDays(1) -> "Tomorrow"
        else -> DateTimeFormatter.ofPattern("d- MMMM", Locale.getDefault()).format(date)
    }
}
