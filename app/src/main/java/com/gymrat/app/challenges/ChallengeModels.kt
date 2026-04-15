package com.gymrat.app.challenges

enum class ChallengeType {
    TimeBased,
    SetBased,
    WeightBased
}

data class ChallengeTemplate(
    val id: String,
    val challengeName: String,
    val type: ChallengeType,
    val sets: Int,
    val reps: Int,
    val restTimeSec: Int,
    val timeLimitSec: Int,
    val xpReward: Int,
    val weightRequired: Boolean
)

data class ChallengeCompletion(
    val templateId: String,
    val type: ChallengeType,
    val timeLimitSec: Int,
    val durationSec: Int,
    val setsTarget: Int,
    val repsTarget: Int,
    val setsDone: Int?,
    val repsDone: Int?,
    val xpEarned: Int,
    val fullyCompleted: Boolean,
    val weightKg: Int?
)
