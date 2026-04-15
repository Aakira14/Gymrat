package com.gymrat.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymrat.app.MainViewModel
import com.gymrat.app.auth.AuthScreen
import com.gymrat.app.auth.AuthViewModel
import com.gymrat.app.auth.ProfileGender
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
fun GymRatApp(
    authVm: AuthViewModel = viewModel(),
    mainVm: MainViewModel = viewModel()
) {
    val auth by authVm.uiState.collectAsState()
    var showWelcome by remember { mutableStateOf(true) }

    if (!auth.hydrated) {
        SplashScreen()
        return
    }

    if (showWelcome) {
        WelcomeScreen(onReady = { showWelcome = false })
        return
    }

    if (auth.sessionUser == null) {
        when (auth.screen) {
            AuthScreen.Login -> LoginScreen(
                loading = auth.loading,
                errorMessage = auth.errorMessage,
                onLogin = { u, p -> authVm.login(u, p) },
                onGoToSignUp = { authVm.goToSignUp() }
            )
            AuthScreen.SignUp -> SignUpScreen(
                loading = auth.loading,
                errorMessage = auth.errorMessage,
                onCreateAccount = { u, p -> authVm.register(u, p) },
                onGoToLogin = { authVm.goToLogin() }
            )
            AuthScreen.Loading -> LoadingScreen()
        }
    } else {
        val u = auth.sessionUser!!
        val doOnboarding = u.onboardingPending && !u.onboardingCompleted

        if (doOnboarding && u.birthdateIso.isNullOrBlank()) {
            BirthdayScreen(
                username = u.username,
                initialBirthdateIso = u.birthdateIso,
                onSave = { iso ->
                    authVm.updateBirthdateIso(iso)
                }
            )
            return
        }

        if (doOnboarding && (u.weightKg == null || u.heightCm == null)) {
            BodyMetricsScreen(
                username = u.username,
                initialWeightKg = u.weightKg,
                initialHeightCm = u.heightCm,
                onNext = { w, h -> authVm.updateBodyMetrics(weightKg = w, heightCm = h) }
            )
            return
        }

        if (doOnboarding && !u.onboardingOptionalDone) {
            OptionalProfileScreen(
                initialGenderChoice = u.genderChoice,
                initialBodyType = u.bodyType,
                onSkip = { authVm.skipOptionalProfile() },
                onReady = { g, bt -> authVm.updateOptionalProfile(genderChoice = g, bodyType = bt) }
            )
            return
        }

        if (doOnboarding && u.gymLevel.isNullOrBlank()) {
            GymLevelScreen(
                initialLevel = u.gymLevel,
                onNext = { level -> authVm.updateGymLevel(level) }
            )
            return
        }

        if (doOnboarding && !u.onboardingRoutineDone) {
            RoutineScreen(
                initial = u.routineByDay,
                onSkip = { authVm.skipRoutine() },
                onDone = { map -> authVm.updateRoutineByDay(map) }
            )
            return
        }

        if (doOnboarding) {
            OnboardingLoadingScreen()
            LaunchedEffect(u.username) {
                delay(1400)
                authVm.setOnboardingCompleted(true)
            }
            return
        }

        HomeScreen(
            user = u,
            authLoading = auth.loading,
            onSignOut = { authVm.logout() },
            onUpdateProfile = { gender: ProfileGender, avatar: String ->
                authVm.updateProfile(gender, avatar)
            },
            onUpdateRoutineByDay = { map -> authVm.updateRoutineByDay(map) },
            vm = mainVm
        )
    }
}
