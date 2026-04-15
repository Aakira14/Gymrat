package com.gymrat.app.data

data class WorkoutSession(
    val id: String,
    val dateIso: String, // yyyy-mm-dd
    val startedAtEpochMs: Long,
    val workoutId: String,
    val workoutTitle: String,
    val durationSec: Int,
    val setsDone: Int?,
    val repsDone: Int?,
    val weightKg: Int?,
    val caloriesBurned: Int,
    val xpGained: Int
)
