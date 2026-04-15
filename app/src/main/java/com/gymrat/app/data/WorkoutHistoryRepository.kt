package com.gymrat.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.historyDataStore by preferencesDataStore(name = "gymrat_history")
private val historyJsonKey = stringPreferencesKey("history_json")

class WorkoutHistoryRepository(private val context: Context) {
    val sessions: Flow<List<WorkoutSession>> = context.historyDataStore.data.map { prefs ->
        decode(prefs[historyJsonKey])
            .sortedByDescending { it.startedAtEpochMs }
    }

    suspend fun addSession(session: WorkoutSession) {
        context.historyDataStore.edit { prefs ->
            val current = decode(prefs[historyJsonKey]).toMutableList()
            current.add(session)
            // keep last 365 sessions to limit storage
            val trimmed = current
                .sortedByDescending { it.startedAtEpochMs }
                .take(365)
            prefs[historyJsonKey] = encode(trimmed)
        }
    }

    private fun decode(raw: String?): List<WorkoutSession> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { idx ->
                val o = arr.optJSONObject(idx) ?: return@mapNotNull null
                WorkoutSession(
                    id = o.optString("id"),
                    dateIso = o.optString("dateIso"),
                    startedAtEpochMs = o.optLong("startedAtEpochMs"),
                    workoutId = o.optString("workoutId"),
                    workoutTitle = o.optString("workoutTitle"),
                    durationSec = o.optInt("durationSec"),
                    setsDone = o.optInt("setsDone").takeIf { o.has("setsDone") },
                    repsDone = o.optInt("repsDone").takeIf { o.has("repsDone") },
                    weightKg = o.optInt("weightKg").takeIf { o.has("weightKg") },
                    caloriesBurned = o.optInt("caloriesBurned"),
                    xpGained = o.optInt("xpGained")
                ).takeIf { it.id.isNotBlank() && it.workoutId.isNotBlank() && it.dateIso.isNotBlank() }
            }
        }.getOrElse { emptyList() }
    }

    private fun encode(list: List<WorkoutSession>): String {
        val arr = JSONArray()
        list.forEach { s ->
            val o = JSONObject()
            o.put("id", s.id)
            o.put("dateIso", s.dateIso)
            o.put("startedAtEpochMs", s.startedAtEpochMs)
            o.put("workoutId", s.workoutId)
            o.put("workoutTitle", s.workoutTitle)
            o.put("durationSec", s.durationSec)
            if (s.setsDone != null) o.put("setsDone", s.setsDone)
            if (s.repsDone != null) o.put("repsDone", s.repsDone)
            if (s.weightKg != null) o.put("weightKg", s.weightKg)
            o.put("caloriesBurned", s.caloriesBurned)
            o.put("xpGained", s.xpGained)
            arr.put(o)
        }
        return arr.toString()
    }
}
