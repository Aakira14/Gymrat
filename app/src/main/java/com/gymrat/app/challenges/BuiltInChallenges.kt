package com.gymrat.app.challenges

object BuiltInChallenges {
    val all: List<Challenge> = listOf(
        Challenge(id = "steps_3k", title = "Warm-up Walk", goalSteps = 3_000),
        Challenge(id = "steps_5k", title = "Daily 5K", goalSteps = 5_000),
        Challenge(id = "steps_10k", title = "Classic 10K", goalSteps = 10_000),
        Challenge(id = "steps_15k", title = "Beast Mode 15K", goalSteps = 15_000)
    )

    fun byId(id: String?): Challenge? = all.firstOrNull { it.id == id }
}

