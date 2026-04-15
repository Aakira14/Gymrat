package com.gymrat.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gymrat.app.challenges.ChallengeTemplate
import com.gymrat.app.challenges.ChallengeTemplates
import com.gymrat.app.challenges.ChallengeCompletion
import com.gymrat.app.challenges.ChallengeType
import com.gymrat.app.challenges.WorkoutChallenge
import com.gymrat.app.ui.theme.GymRatColors
import kotlinx.coroutines.delay

@Composable
fun ChallengeDialog(
    workout: WorkoutChallenge,
    bestWeightKg: Int?,
    onDismiss: () -> Unit,
    onComplete: (ChallengeCompletion) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ChallengeScreen(
            workout = workout,
            bestWeightKg = bestWeightKg,
            onDismiss = onDismiss,
            onComplete = onComplete
        )
    }
}

private enum class Phase { Weight, Pick, Countdown, Work, Rest, Done, TimeUp }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChallengeScreen(
    workout: WorkoutChallenge,
    bestWeightKg: Int?,
    onDismiss: () -> Unit,
    onComplete: (ChallengeCompletion) -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            GymRatColors.VoidBlack,
            GymRatColors.VoidPurpleDeep,
            GymRatColors.VoidBlack
        )
    )

    val haptics = LocalHapticFeedback.current

    var phase by remember { mutableStateOf(Phase.Weight) }
    val templates = remember(workout.id) { ChallengeTemplates.templatesFor(workout.id) }
    var chosen by remember { mutableStateOf<ChallengeTemplate?>(templates.firstOrNull()) }

    var totalLeftSec by remember { mutableIntStateOf(0) }
    var restLeftSec by remember { mutableIntStateOf(0) }
    var currentSet by remember { mutableIntStateOf(1) }
    var repsDone by remember { mutableIntStateOf(0) }
    var weightText by remember { mutableStateOf(bestWeightKg?.toString().orEmpty()) }
    var countdownLeft by remember { mutableIntStateOf(3) }
    var fullyCompleted by remember { mutableStateOf(false) }
    var earnedXp by remember { mutableIntStateOf(0) }
    var durationSec by remember { mutableIntStateOf(0) }

    fun computeProgress(template: ChallengeTemplate): Float {
        return when (template.type) {
            ChallengeType.TimeBased -> {
                val total = template.timeLimitSec.coerceAtLeast(1)
                val spent = (total - totalLeftSec).coerceIn(0, total)
                (spent.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            }
            ChallengeType.SetBased, ChallengeType.WeightBased -> {
                val totalSets = template.sets.coerceAtLeast(1)
                val repsTarget = template.reps.coerceAtLeast(1)
                val completedSets = (currentSet - 1).coerceIn(0, totalSets)
                val repsProgress = (repsDone.coerceIn(0, repsTarget).toFloat() / repsTarget.toFloat())
                ((completedSets.toFloat() + repsProgress) / totalSets.toFloat()).coerceIn(0f, 1f)
            }
        }
    }

    fun finalize(template: ChallengeTemplate, completed: Boolean) {
        fullyCompleted = completed
        earnedXp = if (completed) {
            template.xpReward
        } else {
            val pct = computeProgress(template)
            (template.xpReward * pct).toInt().coerceIn(0, template.xpReward)
        }
        durationSec = (template.timeLimitSec - totalLeftSec).coerceIn(0, template.timeLimitSec)
        phase = Phase.Done
    }

    LaunchedEffect(phase) {
        if (phase == Phase.Countdown) {
            countdownLeft = 3
            while (phase == Phase.Countdown && countdownLeft > 0) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                delay(1000)
                countdownLeft -= 1
            }
            if (phase == Phase.Countdown) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                phase = Phase.Work
            }
        }

        if (phase == Phase.Work || phase == Phase.Rest) {
            while ((phase == Phase.Work || phase == Phase.Rest) && totalLeftSec > 0) {
                delay(1000)
                totalLeftSec -= 1
                if (phase == Phase.Rest && restLeftSec > 0) {
                    restLeftSec -= 1
                    if (restLeftSec == 0) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        phase = Phase.Work
                    }
                }
            }
            if ((phase == Phase.Work || phase == Phase.Rest) && totalLeftSec <= 0) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                phase = Phase.TimeUp
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Challenge", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(workout.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
            Button(onClick = onDismiss, shape = RoundedCornerShape(14.dp)) {
                Text("Close")
            }
        }

        when (phase) {
            Phase.Weight -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Set your weight", fontWeight = FontWeight.Black, fontSize = 22.sp)
                        Text(
                            text = "Optional, but it will be saved in your history.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Weight (kg)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (bestWeightKg != null) {
                            Text("PR: $bestWeightKg kg", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    weightText = ""
                                    phase = Phase.Pick
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp)
                            ) { Text("Skip") }

                            Button(
                                onClick = { phase = Phase.Pick },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp)
                            ) { Text("Continue") }
                        }
                    }
                }
            }

            Phase.Pick -> {
                Text("Pick your mode", fontWeight = FontWeight.SemiBold)
                templates.forEach { t ->
                    val selected = chosen?.id == t.id
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
                        ),
                        onClick = { chosen = t }
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(t.challengeName, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = templateSubtitle(t),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text("+${t.xpReward} XP", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = {
                        val t = chosen ?: return@Button
                        totalLeftSec = t.timeLimitSec
                        restLeftSec = 0
                        currentSet = 1
                        repsDone = 0
                        phase = Phase.Countdown
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Start")
                }
            }

            Phase.Countdown -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Get ready", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = countdownLeft.coerceAtLeast(0).toString(),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Starting…", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Phase.Work, Phase.Rest -> {
                val t = chosen ?: return@Column
                val total = t.timeLimitSec.coerceAtLeast(1)
                val progress = (totalLeftSec.toFloat() / total.toFloat()).coerceIn(0f, 1f)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (t.type) {
                                ChallengeType.TimeBased -> "Time-based"
                                ChallengeType.SetBased -> "Set-based"
                                ChallengeType.WeightBased -> "Weight-based"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("+${t.xpReward} XP", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(if (phase == Phase.Rest) "Rest" else "Timer", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = formatTime(if (phase == Phase.Rest) restLeftSec else totalLeftSec),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        MiniBar(progress = progress)
                    }
                }

                if (t.type != ChallengeType.TimeBased) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Sets / Reps", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Set $currentSet / ${t.sets}  •  Target ${t.reps} reps",
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Reps done: $repsDone")
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { repsDone = (repsDone - 1).coerceAtLeast(0) },
                                        shape = RoundedCornerShape(14.dp)
                                    ) { Text("-") }
                                    Button(
                                        onClick = { repsDone += 1 },
                                        shape = RoundedCornerShape(14.dp)
                                    ) { Text("+") }
                                }
                            }

                            if (t.weightRequired) {
                                OutlinedTextField(
                                    value = weightText,
                                    onValueChange = { weightText = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Weight (kg)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (bestWeightKg != null) {
                                    Text("PR: $bestWeightKg kg", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        // Finish session early -> partial XP
                                        finalize(t, completed = false)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) { Text("Finish") }

                                Button(
                                    onClick = {
                                        if (currentSet >= t.sets) {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            finalize(t, completed = true)
                                        } else {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            currentSet += 1
                                            repsDone = 0
                                            restLeftSec = t.restTimeSec
                                            phase = Phase.Rest
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) { Text(if (currentSet >= t.sets) "Complete" else "Next set") }
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { finalize(t, completed = true) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) { Text("Complete") }
                }
            }

            Phase.Done -> {
                val t = chosen ?: return@Column
                val weightKg = weightText.toIntOrNull()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (fullyCompleted) "Completed!" else "Session saved",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text("+$earnedXp XP", fontWeight = FontWeight.Bold)
                        if (!fullyCompleted) {
                            val pct = (computeProgress(t) * 100).toInt()
                            Text("$pct% progress", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = {
                                val setsDone = when (t.type) {
                                    ChallengeType.SetBased, ChallengeType.WeightBased -> {
                                        val done = (currentSet - 1).coerceAtLeast(0)
                                        if (fullyCompleted) t.sets else done.coerceAtMost(t.sets)
                                    }
                                    else -> null
                                }

                                val repsDone = when (t.type) {
                                    ChallengeType.SetBased, ChallengeType.WeightBased -> {
                                        if (fullyCompleted) (t.sets * t.reps) else {
                                            val completedSets = (currentSet - 1).coerceAtLeast(0)
                                            (completedSets * t.reps) + repsDone.coerceAtLeast(0)
                                        }
                                    }
                                    else -> null
                                }

                                onComplete(
                                    ChallengeCompletion(
                                        templateId = t.id,
                                        type = t.type,
                                        timeLimitSec = t.timeLimitSec,
                                        durationSec = durationSec,
                                        setsTarget = t.sets,
                                        repsTarget = t.reps,
                                        setsDone = setsDone,
                                        repsDone = repsDone,
                                        xpEarned = earnedXp,
                                        fullyCompleted = fullyCompleted,
                                        weightKg = weightKg
                                    )
                                )
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) { Text("Claim XP") }
                    }
                }
            }

            Phase.TimeUp -> {
                val t = chosen ?: return@Column
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = GymRatColors.VoidCard)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Time’s up!", fontWeight = FontWeight.Black, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Claim partial XP or try again.")
                        val partial = (t.xpReward * computeProgress(t)).toInt().coerceIn(0, t.xpReward)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    earnedXp = partial
                                    fullyCompleted = false
                                    durationSec = t.timeLimitSec
                                    phase = Phase.Done
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp)
                            ) { Text("Claim $partial") }

                            Button(
                                onClick = { phase = Phase.Pick },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp)
                            ) { Text("Retry") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniBar(progress: Float) {
    val p by animateFloatAsState(targetValue = progress, label = "mini", animationSpec = androidx.compose.animation.core.tween(250, easing = LinearEasing))
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(p.coerceIn(0f, 1f))
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary, shape)
        )
    }
}

private fun formatTime(totalSec: Int): String {
    val s = totalSec.coerceAtLeast(0)
    val m = s / 60
    val r = s % 60
    return "%d:%02d".format(m, r)
}

private fun templateSubtitle(t: ChallengeTemplate): String {
    return when (t.type) {
        ChallengeType.TimeBased -> "Time-based • ${t.timeLimitSec / 60} min"
        ChallengeType.SetBased -> "Set-based • ${t.sets}×${t.reps} • rest ${t.restTimeSec}s • ${t.timeLimitSec / 60} min"
        ChallengeType.WeightBased -> "Weight-based • ${t.sets}×${t.reps} • rest ${t.restTimeSec}s • ${t.timeLimitSec / 60} min"
    }
}
