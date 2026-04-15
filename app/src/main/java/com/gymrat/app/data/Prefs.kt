package com.gymrat.app.data

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object Prefs {
    val baselineStepsSinceBoot = longPreferencesKey("baseline_steps_since_boot")
    val baselineDateIso = stringPreferencesKey("baseline_date_iso")
    val activeChallengeId = stringPreferencesKey("active_challenge_id")
    val activeWorkoutId = stringPreferencesKey("active_workout_id")
}
