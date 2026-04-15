package com.gymrat.app.auth

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object AuthPrefs {
    val registeredUsername = stringPreferencesKey("registered_username")
    val passwordSaltB64 = stringPreferencesKey("password_salt_b64")
    val passwordHashB64 = stringPreferencesKey("password_hash_b64")
    val sessionUsername = stringPreferencesKey("session_username")

    // Profile
    val profileGender = stringPreferencesKey("profile_gender") // "male" | "female"
    val profileAvatarResName = stringPreferencesKey("profile_avatar_res_name") // ex: "avatar_m1"
    val profileBirthdateIso = stringPreferencesKey("profile_birthdate_iso") // "yyyy-mm-dd"

    // Onboarding
    val onboardingPending = booleanPreferencesKey("onboarding_pending")
    val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    val onboardingOptionalDone = booleanPreferencesKey("onboarding_optional_done")
    val onboardingRoutineDone = booleanPreferencesKey("onboarding_routine_done")
    val profileWeightKg = stringPreferencesKey("profile_weight_kg") // float string
    val profileHeightCm = stringPreferencesKey("profile_height_cm") // float string
    val profileGenderChoice = stringPreferencesKey("profile_gender_choice") // "male" | "female" | "other"
    val profileBodyType = stringPreferencesKey("profile_body_type") // "lean" | "average" | "muscular"
    val profileGymLevel = stringPreferencesKey("profile_gym_level") // "beginner" | "intermediate" | "pro"
    val profileRoutineJson = stringPreferencesKey("profile_routine_json") // json object mapping day->category
}
