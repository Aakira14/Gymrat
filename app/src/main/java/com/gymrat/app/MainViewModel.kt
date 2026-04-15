package com.gymrat.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gymrat.app.challenges.BuiltInChallenges
import com.gymrat.app.challenges.BuiltInWorkouts
import com.gymrat.app.challenges.Challenge
import com.gymrat.app.challenges.ChallengeCompletion
import com.gymrat.app.challenges.ChallengeType
import com.gymrat.app.challenges.WorkoutChallenge
import com.gymrat.app.data.GameRepository
import com.gymrat.app.data.LeaderboardRepository
import com.gymrat.app.data.FirebaseLeaderboardRepository
import com.gymrat.app.data.StepPrefsRepository
import com.gymrat.app.data.WorkoutHistoryRepository
import com.gymrat.app.data.WorkoutSession
import com.gymrat.app.steps.StepCounter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

data class UiState(
    val stepsToday: Int = 0,
    val caloriesBurned: Int = 0,
    val stepSensorAvailable: Boolean? = null,
    val activeChallenge: Challenge? = null,
    val challengeProgressPercent: Int = 0,
    val demoModeEnabled: Boolean = false,
    val activeWorkout: WorkoutChallenge? = null,
    val xpTotal: Int = 0,
    val streakCount: Int = 0,
    val bestWeightKgForActiveWorkout: Int? = null
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = StepPrefsRepository(app)
    private val stepCounter = StepCounter(app)
    private val gameRepo = GameRepository(app)
    private val leaderboardRepo = LeaderboardRepository(app)
    private val firebaseLeaderboardRepo = FirebaseLeaderboardRepository(app)
    private val historyRepo = WorkoutHistoryRepository(app)
    private var bestWeightMap: Map<String, Int> = emptyMap()

    val historySessions: StateFlow<List<WorkoutSession>> = historyRepo.sessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var activityPermissionGranted: Boolean = false

    init {
        viewModelScope.launch {
            repo.activeChallengeId.collect { id ->
                val active = BuiltInChallenges.byId(id)
                _uiState.update { it.copy(activeChallenge = active) }
                recomputeChallengeProgress()
            }
        }

        viewModelScope.launch {
            repo.activeWorkoutId.collect { id ->
                val active = BuiltInWorkouts.byId(id)
                _uiState.update {
                    it.copy(
                        activeWorkout = active,
                        bestWeightKgForActiveWorkout = if (active == null) null else bestWeightMap[active.id]
                    )
                }
            }
        }

        viewModelScope.launch {
            gameRepo.stats.collect { stats ->
                val activeId = _uiState.value.activeWorkout?.id
                bestWeightMap = stats.bestWeightKg
                _uiState.update {
                    it.copy(
                        xpTotal = stats.xpTotal,
                        streakCount = stats.streakCount,
                        bestWeightKgForActiveWorkout = if (activeId == null) null else stats.bestWeightKg[activeId]
                    )
                }
            }
        }
    }

    fun setActivityPermissionGranted(granted: Boolean) {
        activityPermissionGranted = granted
        startOrStopSensor()
    }

    fun setActiveChallenge(id: String) {
        viewModelScope.launch {
            repo.setActiveChallengeId(id)
        }
    }

    fun setActiveWorkout(id: String) {
        viewModelScope.launch {
            repo.setActiveWorkoutId(id)
        }
    }

    fun recordChallengeCompletion(
        username: String?,
        workout: WorkoutChallenge,
        completion: ChallengeCompletion
    ) {
        viewModelScope.launch {
            gameRepo.awardCompletion(
                xp = completion.xpEarned,
                workoutId = workout.id,
                weightKg = if (completion.fullyCompleted) completion.weightKg else null
            )
            if (!username.isNullOrBlank()) {
                leaderboardRepo.addXp(username = username, deltaXp = completion.xpEarned)
                if (firebaseLeaderboardRepo.enabled) {
                    firebaseLeaderboardRepo.addXp(username = username, deltaXp = completion.xpEarned)
                }
            }

            val calories = estimateExerciseCalories(completion)
            val todayIso = LocalDate.now(ZoneId.systemDefault()).toString()
            val startedAt = System.currentTimeMillis()

            historyRepo.addSession(
                WorkoutSession(
                    id = UUID.randomUUID().toString(),
                    dateIso = todayIso,
                    startedAtEpochMs = startedAt,
                    workoutId = workout.id,
                    workoutTitle = workout.title,
                    durationSec = completion.durationSec,
                    setsDone = completion.setsDone,
                    repsDone = completion.repsDone,
                    weightKg = completion.weightKg,
                    caloriesBurned = calories,
                    xpGained = completion.xpEarned
                )
            )
        }
    }

    fun enableDemoMode(enabled: Boolean) {
        _uiState.update { it.copy(demoModeEnabled = enabled) }
        if (enabled) {
            stepCounter.stop()
            _uiState.update { it.copy(stepSensorAvailable = false) }
        } else {
            startOrStopSensor()
        }
    }

    fun addDemoSteps(delta: Int) {
        _uiState.update {
            val newSteps = (it.stepsToday + delta).coerceAtLeast(0)
            it.copy(
                stepsToday = newSteps,
                caloriesBurned = estimateCaloriesFromSteps(newSteps)
            )
        }
        recomputeChallengeProgress()
    }

    private fun startOrStopSensor() {
        if (!activityPermissionGranted) return
        if (_uiState.value.demoModeEnabled) return

        stepCounter.stop()
        val started = stepCounter.start(
            onSensorAvailability = { available ->
                _uiState.update { it.copy(stepSensorAvailable = available) }
            },
            onStepsSinceBoot = { stepsSinceBoot ->
                viewModelScope.launch {
                    val stepsToday = repo.stepsTodayFromStepsSinceBoot(stepsSinceBoot)
                    _uiState.update {
                        it.copy(
                            stepsToday = stepsToday,
                            caloriesBurned = estimateCaloriesFromSteps(stepsToday)
                        )
                    }
                    recomputeChallengeProgress()
                }
            }
        )

        if (!started) {
            _uiState.update { it.copy(stepSensorAvailable = false) }
        }
    }

    private fun recomputeChallengeProgress() {
        val state = _uiState.value
        val active = state.activeChallenge ?: run {
            _uiState.update { it.copy(challengeProgressPercent = 0) }
            return
        }

        val pct = if (active.goalSteps <= 0) 0 else min(100, (state.stepsToday * 100) / active.goalSteps)
        _uiState.update { it.copy(challengeProgressPercent = pct) }
    }

    private fun estimateCaloriesFromSteps(steps: Int): Int {
        // Rough default: ~0.04 kcal / step (varies by weight/pace).
        return (steps * 0.04f).toInt()
    }

    private fun estimateExerciseCalories(completion: ChallengeCompletion): Int {
        val minutes = (completion.durationSec.coerceAtLeast(0) / 60f).coerceAtLeast(0.1f)
        val kcalPerMin = when (completion.type) {
            ChallengeType.TimeBased -> 9.0f
            ChallengeType.SetBased -> 7.0f
            ChallengeType.WeightBased -> 8.0f
        }
        return (minutes * kcalPerMin).toInt().coerceAtLeast(1)
    }

    override fun onCleared() {
        stepCounter.stop()
        super.onCleared()
    }
}
