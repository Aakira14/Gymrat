package com.gymrat.app.data

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object GamePrefs {
    val xpTotal = intPreferencesKey("xp_total")
    val streakCount = intPreferencesKey("streak_count")
    val lastCompletionDateIso = stringPreferencesKey("last_completion_date_iso")
    val bestWeightByWorkout = stringPreferencesKey("best_weight_by_workout") // "id=10;id2=25"
}

