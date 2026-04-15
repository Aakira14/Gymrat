package com.gymrat.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

private val Context.gameDataStore by preferencesDataStore(name = "gymrat_game")

data class GameStats(
    val xpTotal: Int,
    val streakCount: Int,
    val bestWeightKg: Map<String, Int>
)

class GameRepository(private val context: Context) {
    val stats: Flow<GameStats> = context.gameDataStore.data.map { prefs ->
        GameStats(
            xpTotal = prefs[GamePrefs.xpTotal] ?: 0,
            streakCount = prefs[GamePrefs.streakCount] ?: 0,
            bestWeightKg = decodeWeightMap(prefs[GamePrefs.bestWeightByWorkout])
        )
    }

    suspend fun awardCompletion(
        xp: Int,
        workoutId: String,
        weightKg: Int?
    ) {
        val today = LocalDate.now(ZoneId.systemDefault())
        val todayIso = today.toString()

        context.gameDataStore.edit { prefs ->
            val currentXp = prefs[GamePrefs.xpTotal] ?: 0
            prefs[GamePrefs.xpTotal] = currentXp + xp.coerceAtLeast(0)

            val lastIso = prefs[GamePrefs.lastCompletionDateIso]
            val currentStreak = prefs[GamePrefs.streakCount] ?: 0

            val newStreak = when {
                lastIso == null -> 1
                lastIso == todayIso -> currentStreak
                runCatching { LocalDate.parse(lastIso) }.getOrNull() == today.minusDays(1) -> currentStreak + 1
                else -> 1
            }

            prefs[GamePrefs.streakCount] = newStreak
            prefs[GamePrefs.lastCompletionDateIso] = todayIso

            if (weightKg != null && weightKg > 0) {
                val current = decodeWeightMap(prefs[GamePrefs.bestWeightByWorkout])
                val best = current[workoutId] ?: 0
                if (weightKg > best) {
                    val updated = current.toMutableMap()
                    updated[workoutId] = weightKg
                    prefs[GamePrefs.bestWeightByWorkout] = encodeWeightMap(updated)
                }
            }
        }
    }

    private fun decodeWeightMap(raw: String?): Map<String, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(';')
            .mapNotNull { part ->
                val idx = part.indexOf('=')
                if (idx <= 0 || idx == part.lastIndex) return@mapNotNull null
                val key = part.substring(0, idx)
                val value = part.substring(idx + 1).toIntOrNull() ?: return@mapNotNull null
                key to value
            }
            .toMap()
    }

    private fun encodeWeightMap(map: Map<String, Int>): String {
        return map.entries
            .sortedBy { it.key }
            .joinToString(";") { "${it.key}=${it.value}" }
    }
}

