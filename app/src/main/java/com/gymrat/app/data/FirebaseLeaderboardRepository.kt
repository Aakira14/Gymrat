package com.gymrat.app.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Global leaderboard (Firestore).
 *
 * Requires Firebase config via `google-services.json`. If config is missing, [enabled] will be false
 * and callers should fall back to local-only storage.
 */
class FirebaseLeaderboardRepository(context: Context) {
    private val app: FirebaseApp? = runCatching { FirebaseApp.initializeApp(context) }.getOrNull()
    val enabled: Boolean = app != null

    private val db: FirebaseFirestore? = if (enabled) FirebaseFirestore.getInstance(app!!) else null

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun topEntries(limit: Int = 50): Flow<List<LeaderboardEntry>> = callbackFlow {
        val firestore = db
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val reg = firestore.collection("leaderboard")
            .orderBy("xp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    _lastError.value = err.message ?: err.javaClass.simpleName
                    Log.w("GymRat", "Firestore leaderboard listen failed", err)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                _lastError.value = null
                val list = snap?.documents?.mapNotNull { doc ->
                    val username = doc.getString("username") ?: doc.id
                    val xp = (doc.getLong("xp") ?: 0L).toInt()
                    LeaderboardEntry(username = username, xp = xp)
                } ?: emptyList()
                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    fun addXp(username: String, deltaXp: Int) {
        val firestore = db ?: return
        if (username.isBlank() || deltaXp <= 0) return

        val safeId = safeDocId(username)
        val doc = firestore.collection("leaderboard").document(safeId)
        doc.set(
            mapOf(
                "username" to username,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).addOnFailureListener { e ->
            _lastError.value = e.message ?: e.javaClass.simpleName
            Log.w("GymRat", "Firestore leaderboard set failed", e)
        }
        doc.update(
            mapOf(
                "xp" to FieldValue.increment(deltaXp.toLong()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).addOnFailureListener { e ->
            _lastError.value = e.message ?: e.javaClass.simpleName
            Log.w("GymRat", "Firestore leaderboard update failed", e)
        }
    }

    private fun safeDocId(username: String): String {
        // Firestore doc IDs cannot contain '/', and extremely long IDs are not helpful.
        val trimmed = username.trim()
        val replaced = trimmed.replace('/', '_')
        return replaced.take(128).ifBlank { "user" }
    }
}
