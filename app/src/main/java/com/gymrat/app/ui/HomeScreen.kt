package com.gymrat.app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.gymrat.app.MainViewModel
import com.gymrat.app.R
import com.gymrat.app.auth.GymRatUser
import com.gymrat.app.auth.ProfileGender
import com.gymrat.app.challenges.BuiltInWorkouts
import com.gymrat.app.challenges.WorkoutChallenge
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    user: GymRatUser?,
    authLoading: Boolean,
    onSignOut: () -> Unit,
    onUpdateProfile: (ProfileGender, String) -> Unit,
    onUpdateRoutineByDay: (Map<String, String>) -> Unit,
    vm: MainViewModel
) {
    val ui by vm.uiState.collectAsState()
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf(false) }
    var profileOpen by remember { mutableStateOf(false) }
    var leaderboardOpen by remember { mutableStateOf(false) }
    var historyOpen by remember { mutableStateOf(false) }
    var historyInitialDateIso by remember { mutableStateOf<String?>(null) }
    var challengeWorkout by remember { mutableStateOf<WorkoutChallenge?>(null) }
    var coinsBurst by remember { mutableIntStateOf(0) }
    var editProfileOpen by remember { mutableStateOf(false) }
    var exerciseQuery by remember { mutableStateOf("") }
    val todayDayName = remember {
        LocalDate.now(ZoneId.systemDefault()).dayOfWeek.name
            .lowercase(Locale.getDefault())
            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }
    val routineCategories = remember {
        listOf(
            "Chest",
            "Back",
            "Shoulders",
            "Biceps",
            "Triceps",
            "Forearms",
            "Legs",
            "Calves",
            "Core (Abs + Obliques + Lower Back)",
            "Mixed"
        )
    }
    var todayCategory by remember(user?.username, user?.routineByDay) {
        mutableStateOf(user?.routineByDay?.get(todayDayName))
    }
    var todayCategoryMenuOpen by remember { mutableStateOf(false) }

    var lastXp by remember { mutableIntStateOf(ui.xpTotal) }
    LaunchedEffect(ui.xpTotal) {
        if (ui.xpTotal > lastXp) {
            coinsBurst = (ui.xpTotal - lastXp).coerceIn(1, 60)
            lastXp = ui.xpTotal
            delay(900)
            coinsBurst = 0
        } else {
            lastXp = ui.xpTotal
        }
    }

    val animatedXp by animateIntAsState(
        targetValue = ui.xpTotal,
        animationSpec = tween(durationMillis = 650),
        label = "xp"
    )

    val historySessions by vm.historySessions.collectAsState()
    val recentHistoryDays = remember(historySessions) {
        historySessions
            .groupBy { it.dateIso }
            .map { (dateIso, list) ->
                val titles = list
                    .sortedByDescending { it.startedAtEpochMs }
                    .map { it.workoutTitle }
                    .distinct()
                HistoryDayPreview(
                    dateIso = dateIso,
                    date = runCatching { LocalDate.parse(dateIso) }.getOrNull(),
                    workoutTitles = titles,
                    totalXp = list.sumOf { it.xpGained }
                )
            }
            .sortedByDescending { day ->
                day.date ?: LocalDate.MIN
            }
            .take(3)
    }

    val baseWorkouts = remember(todayCategory) {
        val cat = todayCategory
        if (cat.isNullOrBlank()) BuiltInWorkouts.all
        else BuiltInWorkouts.all.filter { workoutMatchesCategory(it, cat) }
    }

    val q = exerciseQuery.trim().lowercase(Locale.getDefault())
    val matches = remember(q, baseWorkouts) {
        if (q.isBlank()) emptyList()
        else baseWorkouts
            .filter { w ->
                w.title.lowercase(Locale.getDefault()).contains(q) || w.id.contains(q)
            }
            .sortedBy { it.title.lowercase(Locale.getDefault()) }
    }

    val filteredGroups = remember(todayCategory) {
        val cat = todayCategory
        if (cat.isNullOrBlank()) BuiltInWorkouts.groups
        else BuiltInWorkouts.groups.mapNotNull { g ->
            val sections = g.sections.mapNotNull { s ->
                val ws = s.workouts.filter { workoutMatchesCategory(it, cat) }
                if (ws.isEmpty()) null else s.copy(workouts = ws)
            }
            if (sections.isEmpty()) null else g.copy(sections = sections)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasPermission = granted
            vm.setActivityPermissionGranted(granted)
        }
    )

    LaunchedEffect(Unit) {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        hasPermission = granted
        vm.setActivityPermissionGranted(granted)
    }

    VoidPatternBackdrop {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("GymRat") },
                    actions = {
                        IconButton(onClick = { leaderboardOpen = true }) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = "Leaderboard"
                            )
                        }
                        IconButton(onClick = { profileOpen = true }) {
                            val avatarResName = user?.avatarResName
                            val resId = remember(avatarResName) {
                                if (avatarResName.isNullOrBlank()) 0
                                else context.resources.getIdentifier(avatarResName, "drawable", context.packageName)
                            }
                            if (resId != 0) {
                                Image(
                                    painter = painterResource(resId),
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.AccountCircle,
                                    contentDescription = "Profile"
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item {
                val username = user?.username
                Text(
                    text = if (username.isNullOrBlank()) "Today" else "Today: $username",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.MonetizationOn,
                                contentDescription = "XP",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text("XP: $animatedXp", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }

                        if (coinsBurst > 0) {
                            CoinBurst(
                                count = coinsBurst.coerceIn(4, 14),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text("Streak: ${ui.streakCount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            item {
                Text("Steps:", style = MaterialTheme.typography.labelLarge)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ui.stepsToday.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black
                        )
                        if (!hasPermission) {
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                                    }
                                },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Enable")
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Calories burned due to steps", style = MaterialTheme.typography.labelLarge)
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = ui.caloriesBurned.toString(),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        Text("History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            historyInitialDateIso = null
                            historyOpen = true
                        },
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("View") }
                }
            }

            if (recentHistoryDays.isEmpty()) {
                item {
                    Text(
                        text = "No workouts logged yet. Start a challenge to build your timeline.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            } else {
                recentHistoryDays.forEach { day ->
                    item {
                        HistoryDayCard(
                            day = day,
                            onClick = {
                                historyInitialDateIso = day.dateIso
                                historyOpen = true
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Challenges",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Today • $todayDayName",
                            fontWeight = FontWeight.Bold
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = todayCategory.orEmpty(),
                                onValueChange = { },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Category (optional)") },
                                placeholder = { Text("All exercises") },
                                trailingIcon = {
                                    IconButton(onClick = { todayCategoryMenuOpen = true }) {
                                        Icon(Icons.Outlined.ExpandMore, contentDescription = "Pick category")
                                    }
                                }
                            )

                            DropdownMenu(
                                expanded = todayCategoryMenuOpen,
                                onDismissRequest = { todayCategoryMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All exercises") },
                                    onClick = {
                                        todayCategoryMenuOpen = false
                                        todayCategory = null
                                        val u = user ?: return@DropdownMenuItem
                                        val map = u.routineByDay.toMutableMap()
                                        map.remove(todayDayName)
                                        onUpdateRoutineByDay(map)
                                    }
                                )
                                routineCategories.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c) },
                                        onClick = {
                                            todayCategoryMenuOpen = false
                                            todayCategory = c
                                            val u = user ?: return@DropdownMenuItem
                                            val map = u.routineByDay.toMutableMap()
                                            map[todayDayName] = c
                                            onUpdateRoutineByDay(map)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    OutlinedTextField(
                        value = exerciseQuery,
                        onValueChange = { exerciseQuery = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        trailingIcon = {
                            if (exerciseQuery.isNotBlank()) {
                                IconButton(onClick = { exerciseQuery = "" }) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        placeholder = { Text("Search exercises…") }
                    )
                }
            }

            if (q.isNotBlank()) {
                if (matches.isEmpty()) {
                    item {
                        Text(
                            "No matching exercises found.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    item {
                        Text(
                            "Results (${matches.size})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    item {
                        WorkoutGrid(
                            workouts = matches,
                            selectedId = ui.activeWorkout?.id,
                            onSelect = { id ->
                                vm.setActiveWorkout(id)
                                challengeWorkout = BuiltInWorkouts.byId(id)
                            }
                        )
                    }
                }
            } else {
                filteredGroups.forEach { group ->
                    item {
                        WorkoutGroupHeader(title = group.title)
                    }
                    group.sections.forEach { section ->
                        item {
                            Text(section.title, style = MaterialTheme.typography.labelLarge)
                        }
                        item {
                            WorkoutGrid(
                                workouts = section.workouts,
                                selectedId = ui.activeWorkout?.id,
                                onSelect = { id ->
                                    vm.setActiveWorkout(id)
                                    challengeWorkout = BuiltInWorkouts.byId(id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
        if (historyOpen) {
            HistoryScreen(
                sessions = historySessions,
                initialDateIso = historyInitialDateIso,
                onBack = { historyOpen = false }
            )
        }
    }

    if (profileOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { profileOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ProfileSheetContent(
                user = user,
                xp = ui.xpTotal,
                streak = ui.streakCount,
                activeWorkoutTitle = ui.activeWorkout?.title,
                logoutEnabled = !authLoading,
                onEditProfile = {
                    profileOpen = false
                    editProfileOpen = true
                },
                onLogout = {
                    profileOpen = false
                    onSignOut()
                }
            )
            Spacer(Modifier.height(18.dp))
        }
    }

    if (editProfileOpen) {
        EditProfileDialog(
            currentGender = user?.gender,
            currentAvatarResName = user?.avatarResName,
            onDismiss = { editProfileOpen = false },
            onSave = { gender, avatar ->
                onUpdateProfile(gender, avatar)
                editProfileOpen = false
            }
        )
    }

    if (challengeWorkout != null) {
        ChallengeDialog(
            workout = challengeWorkout!!,
            bestWeightKg = if (challengeWorkout?.id == ui.activeWorkout?.id) ui.bestWeightKgForActiveWorkout else null,
            onDismiss = { challengeWorkout = null },
            onComplete = { completion ->
                vm.recordChallengeCompletion(
                    username = user?.username,
                    workout = challengeWorkout!!,
                    completion = completion
                )
            }
        )
    }

    if (leaderboardOpen) {
        LeaderboardDialog(
            currentUsername = user?.username,
            onDismiss = { leaderboardOpen = false }
        )
    }

    // History is now a full-screen page overlay (see `HistoryScreen`).
}

@Composable
private fun CoinBurst(
    count: Int,
    tint: Color
) {
    // Tiny "gamey" burst: a few coins float up + fade.
    // Deterministic offsets so recompositions don’t reshuffle.
    val items = remember(count) { (0 until count).toList() }
    val transition = rememberInfiniteTransition(label = "coins")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900)
        ),
        label = "t"
    )

    items.forEach { i ->
        val x = ((i % 5) - 2) * 10f
        val y = -40f - (i / 5) * 16f
        Icon(
            imageVector = Icons.Outlined.MonetizationOn,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .graphicsLayer {
                    translationX = x
                    translationY = y * t
                    scaleX = 0.55f + (0.15f * (1f - t))
                    scaleY = 0.55f + (0.15f * (1f - t))
                    rotationZ = (i * 19f) * t
                }
                .alpha(1f - t)
        )
    }
}

private data class HistoryDayPreview(
    val dateIso: String,
    val date: LocalDate?,
    val workoutTitles: List<String>,
    val totalXp: Int
)

@Composable
private fun HistoryDayCard(
    day: HistoryDayPreview,
    onClick: () -> Unit
) {
    val today = remember { LocalDate.now(ZoneId.systemDefault()) }
    val label = remember(day.dateIso) {
        day.date?.let { humanDayLabel(it, today) } ?: day.dateIso
    }

    val titles = remember(day.workoutTitles) {
        val shown = day.workoutTitles.take(3)
        val more = (day.workoutTitles.size - shown.size).coerceAtLeast(0)
        buildString {
            append(shown.joinToString(" • "))
            if (more > 0) append(" • +$more more")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontWeight = FontWeight.Bold)
                Text(
                    "+${day.totalXp} XP",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (titles.isNotBlank()) {
                Text(
                    text = titles,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
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

private fun workoutMatchesCategory(workout: WorkoutChallenge, category: String): Boolean {
    if (category == "Mixed") return true
    val t = workout.title.lowercase(Locale.getDefault())
    val id = workout.id.lowercase(Locale.getDefault())
    fun has(vararg keys: String): Boolean = keys.any { t.contains(it) || id.contains(it) }

    return when (category) {
        "Chest" -> has("bench", "incline", "fly", "pec", "chest", "crossover", "push-up", "push ups", "pushups")
        "Back" -> has("row", "pulldown", "pull-up", "pull ups", "chin-up", "chin ups", "renegade", "back")
        "Shoulders" -> has("shoulder", "arnold", "lateral", "front raise", "rear delt", "overhead press")
        "Biceps" -> has("bicep", "curl", "hammer", "concentration")
        "Triceps" -> has("tricep", "skull", "pushdown", "dip", "dips")
        "Forearms" -> has("forearm", "wrist")
        "Legs" -> has("squat", "lunge", "leg press", "leg extension", "leg curl", "deadlift", "romanian", "step-up", "step ups", "hip thrust")
        "Calves" -> has("calf")
        "Core (Abs + Obliques + Lower Back)" -> has("plank", "crunch", "sit-up", "sit ups", "situps", "russian", "core", "twist", "side bend", "mountain climber")
        else -> true
    }
}

@Composable
private fun WorkoutGroupHeader(title: String) {
    val icon = when (title) {
        "Dumbbell Exercises" -> GroupHeaderIcon(resId = R.drawable.ic_dumbbell_exercises, wide = false)
        "Barbell Exercises" -> GroupHeaderIcon(resId = R.drawable.ic_barbell_exercises, wide = true)
        else -> null
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (icon != null) {
            Image(
                painter = painterResource(icon.resId),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .then(
                        if (icon.wide) Modifier.height(28.dp).width(56.dp) else Modifier.size(28.dp)
                    )
                    .clip(RoundedCornerShape(10.dp))
                    .alpha(0.72f)
            )
        }
    }
}

private data class GroupHeaderIcon(
    val resId: Int,
    val wide: Boolean
)

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutGrid(
    workouts: List<WorkoutChallenge>,
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    val spacing = 12.dp
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cellWidth = (maxWidth - spacing * 2) / 3
        FlowRow(
            maxItemsInEachRow = 3,
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
            modifier = Modifier.fillMaxWidth()
        ) {
            workouts.forEach { workout ->
                val selected = selectedId == workout.id
                val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.dp, borderColor),
                    modifier = Modifier
                        .width(cellWidth)
                        .height(86.dp)
                        .padding(0.dp),
                    onClick = { onSelect(workout.id) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (
                            workout.id == "dumbbell_bench_press" ||
                            workout.id == "dumbbell_bicep_curl" ||
                            workout.id == "hammer_curl" ||
                            workout.id == "concentration_curl" ||
                            workout.id == "incline_dumbbell_press" ||
                            workout.id == "dumbbell_fly" ||
                            workout.id == "dumbbell_shoulder_press" ||
                            workout.id == "arnold_press" ||
                            workout.id == "lateral_raise"
                        ) {
                            val title = when (workout.id) {
                                "dumbbell_bench_press" -> "Dumbbell\nBench Press"
                                "dumbbell_bicep_curl" -> "Dumbbell\nBicep Curl"
                                "hammer_curl" -> "Hammer\nCurl"
                                "concentration_curl" -> "Concentration\nCurl"
                                "incline_dumbbell_press" -> "Incline\nDumbbell Press"
                                "dumbbell_fly" -> "Dumbbell\nFly"
                                "dumbbell_shoulder_press" -> "Dumbbell\nShoulder Press"
                                "arnold_press" -> "Arnold\nPress"
                                else -> "Lateral\nRaise"
                            }
                            val imageRes = when (workout.id) {
                                "dumbbell_bench_press" -> R.drawable.exercise_dumbbell_bench_press
                                "dumbbell_bicep_curl" -> R.drawable.exercise_dumbbell_bicep_curl
                                "hammer_curl" -> R.drawable.exercise_hammer_curl
                                "concentration_curl" -> R.drawable.exercise_concentration_curl
                                "incline_dumbbell_press" -> R.drawable.exercise_incline_dumbbell_press
                                "dumbbell_fly" -> R.drawable.exercise_dumbbell_fly
                                "dumbbell_shoulder_press" -> R.drawable.exercise_dumbbell_shoulder_press
                                "arnold_press" -> R.drawable.exercise_arnold_press
                                else -> R.drawable.exercise_lateral_raise
                            }
                            val leftWeight = if (workout.id == "arnold_press") 0.95f else 1.05f
                            val rightWeight = if (workout.id == "arnold_press") 1.05f else 0.95f
                            val imageStartPad = if (workout.id == "arnold_press") 2.dp else 6.dp
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(leftWeight)
                                        .padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 16.sp,
                                        textAlign = TextAlign.Start
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(rightWeight)
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Image(
                                        painter = painterResource(imageRes),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        alpha = 0.62f,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(start = imageStartPad)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = workout.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
