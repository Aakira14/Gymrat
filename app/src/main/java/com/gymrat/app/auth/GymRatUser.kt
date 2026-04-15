package com.gymrat.app.auth

enum class ProfileGender { Male, Female }

data class GymRatUser(
    val username: String,
    val gender: ProfileGender? = null,
    val avatarResName: String? = null,
    val birthdateIso: String? = null,
    val weightKg: Float? = null,
    val heightCm: Float? = null,
    val genderChoice: String? = null,
    val bodyType: String? = null,
    val gymLevel: String? = null,
    val routineByDay: Map<String, String> = emptyMap(),
    val onboardingOptionalDone: Boolean = false,
    val onboardingRoutineDone: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val onboardingPending: Boolean = false
)
