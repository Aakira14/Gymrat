package com.gymrat.app.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.authDataStore by preferencesDataStore(name = "gymrat_auth")

class AuthRepository(private val context: Context) {
    val sessionUser: Flow<GymRatUser?> = context.authDataStore.data.map { prefs ->
        val username = prefs[AuthPrefs.sessionUsername] ?: return@map null
        val gender = when (prefs[AuthPrefs.profileGender]) {
            "male" -> ProfileGender.Male
            "female" -> ProfileGender.Female
            else -> null
        }
        GymRatUser(
            username = username,
            gender = gender,
            avatarResName = prefs[AuthPrefs.profileAvatarResName],
            birthdateIso = prefs[AuthPrefs.profileBirthdateIso],
            weightKg = prefs[AuthPrefs.profileWeightKg]?.toFloatOrNull(),
            heightCm = prefs[AuthPrefs.profileHeightCm]?.toFloatOrNull(),
            genderChoice = prefs[AuthPrefs.profileGenderChoice],
            bodyType = prefs[AuthPrefs.profileBodyType],
            gymLevel = prefs[AuthPrefs.profileGymLevel],
            routineByDay = decodeRoutine(prefs[AuthPrefs.profileRoutineJson]),
            onboardingOptionalDone = prefs[AuthPrefs.onboardingOptionalDone] ?: false,
            onboardingRoutineDone = prefs[AuthPrefs.onboardingRoutineDone] ?: false,
            onboardingCompleted = prefs[AuthPrefs.onboardingCompleted] ?: false,
            onboardingPending = prefs[AuthPrefs.onboardingPending] ?: false
        )
    }

    val hasRegisteredAccount: Flow<Boolean> = context.authDataStore.data.map { prefs ->
        prefs[AuthPrefs.registeredUsername] != null &&
            prefs[AuthPrefs.passwordSaltB64] != null &&
            prefs[AuthPrefs.passwordHashB64] != null
    }

    suspend fun register(username: String, password: String) {
        val hash = PasswordHasher.hash(password)
        context.authDataStore.edit { prefs ->
            prefs[AuthPrefs.registeredUsername] = username
            prefs[AuthPrefs.passwordSaltB64] = hash.saltB64
            prefs[AuthPrefs.passwordHashB64] = hash.hashB64
            prefs[AuthPrefs.sessionUsername] = username

            // Start onboarding for brand-new accounts.
            prefs[AuthPrefs.onboardingPending] = true
            prefs[AuthPrefs.onboardingCompleted] = false
            prefs[AuthPrefs.onboardingOptionalDone] = false
            prefs[AuthPrefs.onboardingRoutineDone] = false
        }
    }

    suspend fun login(username: String, password: String): Boolean {
        var ok = false
        context.authDataStore.edit { prefs ->
            val regUser = prefs[AuthPrefs.registeredUsername]
            val salt = prefs[AuthPrefs.passwordSaltB64]
            val hash = prefs[AuthPrefs.passwordHashB64]
            ok = regUser != null &&
                salt != null &&
                hash != null &&
                regUser == username &&
                PasswordHasher.verify(password, salt, hash)

            if (ok) {
                prefs[AuthPrefs.sessionUsername] = username
            }
        }
        return ok
    }

    suspend fun logout() {
        context.authDataStore.edit { prefs ->
            prefs.remove(AuthPrefs.sessionUsername)
        }
    }

    suspend fun updateProfile(gender: ProfileGender, avatarResName: String) {
        context.authDataStore.edit { prefs ->
            prefs[AuthPrefs.profileGender] = if (gender == ProfileGender.Male) "male" else "female"
            prefs[AuthPrefs.profileAvatarResName] = avatarResName
        }
    }

    suspend fun updateBirthdateIso(birthdateIso: String) {
        val trimmed = birthdateIso.trim()
        context.authDataStore.edit { prefs ->
            if (trimmed.isBlank()) prefs.remove(AuthPrefs.profileBirthdateIso)
            else prefs[AuthPrefs.profileBirthdateIso] = trimmed
        }
    }

    suspend fun updateBodyMetrics(weightKg: Float, heightCm: Float) {
        context.authDataStore.edit { prefs ->
            prefs[AuthPrefs.profileWeightKg] = weightKg.toString()
            prefs[AuthPrefs.profileHeightCm] = heightCm.toString()
        }
    }

    suspend fun updateOptionalProfile(genderChoice: String?, bodyType: String?) {
        context.authDataStore.edit { prefs ->
            if (genderChoice.isNullOrBlank()) prefs.remove(AuthPrefs.profileGenderChoice)
            else prefs[AuthPrefs.profileGenderChoice] = genderChoice

            if (bodyType.isNullOrBlank()) prefs.remove(AuthPrefs.profileBodyType)
            else prefs[AuthPrefs.profileBodyType] = bodyType

            prefs[AuthPrefs.onboardingOptionalDone] = true
        }
    }

    suspend fun updateGymLevel(level: String) {
        context.authDataStore.edit { prefs ->
            prefs[AuthPrefs.profileGymLevel] = level
        }
    }

    suspend fun updateRoutineByDay(map: Map<String, String>) {
        context.authDataStore.edit { prefs ->
            prefs[AuthPrefs.profileRoutineJson] = encodeRoutine(map)
            prefs[AuthPrefs.onboardingRoutineDone] = true
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.authDataStore.edit { prefs ->
            prefs[AuthPrefs.onboardingCompleted] = completed
            if (completed) prefs[AuthPrefs.onboardingPending] = false
        }
    }

    suspend fun skipOptionalProfile() {
        context.authDataStore.edit { prefs ->
            prefs[AuthPrefs.onboardingOptionalDone] = true
        }
    }

    suspend fun skipRoutine() {
        context.authDataStore.edit { prefs ->
            prefs[AuthPrefs.onboardingRoutineDone] = true
        }
    }

    private fun decodeRoutine(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val o = JSONObject(raw)
            o.keys().asSequence().mapNotNull { k ->
                val v = o.optString(k)
                if (k.isBlank() || v.isBlank()) null else k to v
            }.toMap()
        }.getOrElse { emptyMap() }
    }

    private fun encodeRoutine(map: Map<String, String>): String {
        val o = JSONObject()
        map.forEach { (k, v) ->
            if (k.isNotBlank() && v.isNotBlank()) o.put(k, v)
        }
        return o.toString()
    }
}
