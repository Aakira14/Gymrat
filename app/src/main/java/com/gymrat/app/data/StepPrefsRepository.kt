package com.gymrat.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max

private val Context.dataStore by preferencesDataStore(name = "gymrat_prefs")

class StepPrefsRepository(private val context: Context) {
    val activeChallengeId: Flow<String?> = context.dataStore.data.map { it[Prefs.activeChallengeId] }
    val activeWorkoutId: Flow<String?> = context.dataStore.data.map { it[Prefs.activeWorkoutId] }

    suspend fun setActiveChallengeId(id: String) {
        context.dataStore.edit { it[Prefs.activeChallengeId] = id }
    }

    suspend fun setActiveWorkoutId(id: String) {
        context.dataStore.edit { it[Prefs.activeWorkoutId] = id }
    }

    suspend fun stepsTodayFromStepsSinceBoot(stepsSinceBoot: Long): Int {
        val today = LocalDate.now(ZoneId.systemDefault())
        val todayIso = today.toString()

        // Reading DataStore synchronously isn't supported; do a single edit transaction instead.
        // We keep logic in one place: if date changes or baseline is invalid, reset baseline.
        var stepsToday = 0

        context.dataStore.edit { prefs ->
            val storedBaseline = prefs[Prefs.baselineStepsSinceBoot]
            val storedDate = prefs[Prefs.baselineDateIso]

            val shouldReset = storedBaseline == null ||
                storedDate == null ||
                storedDate != todayIso ||
                stepsSinceBoot < storedBaseline

            val effectiveBaseline = if (shouldReset) {
                prefs[Prefs.baselineStepsSinceBoot] = stepsSinceBoot
                prefs[Prefs.baselineDateIso] = todayIso
                stepsSinceBoot
            } else {
                storedBaseline
            }

            stepsToday = max(0, (stepsSinceBoot - (effectiveBaseline ?: stepsSinceBoot))).toInt()
        }

        return stepsToday
    }
}
