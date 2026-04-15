package com.gymrat.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class LeaderboardEntry(
    val username: String,
    val xp: Int
)

private val Context.leaderboardDataStore by preferencesDataStore(name = "gymrat_leaderboard")
private val leaderboardRawKey = stringPreferencesKey("leaderboard_raw") // "u=10;u2=25"

class LeaderboardRepository(private val context: Context) {
    val entries: Flow<List<LeaderboardEntry>> = context.leaderboardDataStore.data.map { prefs ->
        decode(prefs[leaderboardRawKey])
            .entries
            .map { LeaderboardEntry(username = it.key, xp = it.value) }
            .sortedByDescending { it.xp }
    }

    suspend fun addXp(username: String, deltaXp: Int) {
        if (username.isBlank() || deltaXp <= 0) return
        context.leaderboardDataStore.edit { prefs ->
            val map = decode(prefs[leaderboardRawKey]).toMutableMap()
            val current = map[username] ?: 0
            map[username] = current + deltaXp
            prefs[leaderboardRawKey] = encode(map)
        }
    }

    private fun decode(raw: String?): Map<String, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(';')
            .mapNotNull { part ->
                val idx = part.indexOf('=')
                if (idx <= 0 || idx >= part.length - 1) return@mapNotNull null
                val key = part.substring(0, idx)
                val value = part.substring(idx + 1).toIntOrNull() ?: return@mapNotNull null
                key to value
            }
            .toMap()
    }

    private fun encode(map: Map<String, Int>): String {
        return map.entries
            .sortedByDescending { it.value }
            .joinToString(";") { "${it.key}=${it.value}" }
    }
}

