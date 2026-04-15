package com.gymrat.app.challenges

object ChallengeTemplates {
    fun templatesFor(workoutId: String): List<ChallengeTemplate> {
        val workout = BuiltInWorkouts.byId(workoutId) ?: return emptyList()
        val groupTitle = findGroupTitle(workoutId)

        val base = workout.title

        return when {
            groupTitle.contains("Cardio", ignoreCase = true) -> listOf(
                ChallengeTemplate(
                    id = "${workout.id}_time_10",
                    challengeName = "$base Sprint",
                    type = ChallengeType.TimeBased,
                    sets = 0,
                    reps = 0,
                    restTimeSec = 0,
                    timeLimitSec = 10 * 60,
                    xpReward = 80,
                    weightRequired = false
                ),
                ChallengeTemplate(
                    id = "${workout.id}_time_20",
                    challengeName = "$base Endurance",
                    type = ChallengeType.TimeBased,
                    sets = 0,
                    reps = 0,
                    restTimeSec = 0,
                    timeLimitSec = 20 * 60,
                    xpReward = 140,
                    weightRequired = false
                )
            )

            groupTitle.contains("Bodyweight", ignoreCase = true) -> listOf(
                ChallengeTemplate(
                    id = "${workout.id}_sets_3x12",
                    challengeName = "$base Classic",
                    type = ChallengeType.SetBased,
                    sets = 3,
                    reps = 12,
                    restTimeSec = 30,
                    timeLimitSec = 8 * 60,
                    xpReward = 60,
                    weightRequired = false
                ),
                ChallengeTemplate(
                    id = "${workout.id}_time_5",
                    challengeName = "$base Time Attack",
                    type = ChallengeType.TimeBased,
                    sets = 0,
                    reps = 0,
                    restTimeSec = 0,
                    timeLimitSec = 5 * 60,
                    xpReward = 55,
                    weightRequired = false
                )
            )

            else -> listOf(
                ChallengeTemplate(
                    id = "${workout.id}_sets_4x12",
                    challengeName = "$base Volume",
                    type = ChallengeType.SetBased,
                    sets = 4,
                    reps = 12,
                    restTimeSec = 45,
                    timeLimitSec = 12 * 60,
                    xpReward = 85,
                    weightRequired = false
                ),
                ChallengeTemplate(
                    id = "${workout.id}_weight_3x8",
                    challengeName = "$base Strength",
                    type = ChallengeType.WeightBased,
                    sets = 3,
                    reps = 8,
                    restTimeSec = 60,
                    timeLimitSec = 10 * 60,
                    xpReward = 110,
                    weightRequired = true
                ),
                ChallengeTemplate(
                    id = "${workout.id}_time_6",
                    challengeName = "$base Speed Run",
                    type = ChallengeType.TimeBased,
                    sets = 0,
                    reps = 0,
                    restTimeSec = 0,
                    timeLimitSec = 6 * 60,
                    xpReward = 70,
                    weightRequired = false
                )
            )
        }
    }

    private fun findGroupTitle(workoutId: String): String {
        for (group in BuiltInWorkouts.groups) {
            for (section in group.sections) {
                if (section.workouts.any { it.id == workoutId }) return group.title
            }
        }
        return ""
    }
}

