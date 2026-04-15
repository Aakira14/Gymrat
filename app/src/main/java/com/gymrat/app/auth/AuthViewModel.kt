package com.gymrat.app.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthScreen {
    Login,
    SignUp,
    Loading
}

data class AuthUiState(
    val sessionUser: GymRatUser? = null,
    val screen: AuthScreen = AuthScreen.Login,
    val errorMessage: String? = null,
    val loading: Boolean = false,
    val hydrated: Boolean = false
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.sessionUser.collect { user ->
                _uiState.update { it.copy(sessionUser = user, hydrated = true) }
            }
        }
    }

    fun goToLogin() {
        _uiState.update { it.copy(screen = AuthScreen.Login, errorMessage = null, loading = false) }
    }

    fun goToSignUp() {
        _uiState.update { it.copy(screen = AuthScreen.SignUp, errorMessage = null, loading = false) }
    }

    fun login(username: String, password: String) {
        val u = username.trim()
        if (u.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter username and password.") }
            return
        }

        _uiState.update { it.copy(screen = AuthScreen.Loading, loading = true, errorMessage = null) }
        viewModelScope.launch {
            delay(900)
            val ok = repo.login(u, password)
            if (!ok) {
                _uiState.update {
                    it.copy(
                        screen = AuthScreen.Login,
                        loading = false,
                        errorMessage = "Wrong username or password."
                    )
                }
            } else {
                _uiState.update { it.copy(loading = false, errorMessage = null) }
            }
        }
    }

    fun register(username: String, password: String) {
        val u = username.trim()
        if (u.length < 3) {
            _uiState.update { it.copy(errorMessage = "Username must be at least 3 characters.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.") }
            return
        }

        _uiState.update { it.copy(screen = AuthScreen.Loading, loading = true, errorMessage = null) }
        viewModelScope.launch {
            delay(1200)
            repo.register(u, password)
            _uiState.update { it.copy(loading = false, errorMessage = null) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
            _uiState.update { it.copy(screen = AuthScreen.Login, errorMessage = null, loading = false) }
        }
    }

    fun updateProfile(gender: ProfileGender, avatarResName: String) {
        viewModelScope.launch {
            repo.updateProfile(gender = gender, avatarResName = avatarResName)
        }
    }

    fun updateBirthdateIso(birthdateIso: String) {
        viewModelScope.launch {
            repo.updateBirthdateIso(birthdateIso)
        }
    }

    fun updateBodyMetrics(weightKg: Float, heightCm: Float) {
        viewModelScope.launch {
            repo.updateBodyMetrics(weightKg = weightKg, heightCm = heightCm)
        }
    }

    fun updateOptionalProfile(genderChoice: String?, bodyType: String?) {
        viewModelScope.launch {
            repo.updateOptionalProfile(genderChoice = genderChoice, bodyType = bodyType)
        }
    }

    fun skipOptionalProfile() {
        viewModelScope.launch {
            repo.skipOptionalProfile()
        }
    }

    fun updateGymLevel(level: String) {
        viewModelScope.launch {
            repo.updateGymLevel(level)
        }
    }

    fun updateRoutineByDay(map: Map<String, String>) {
        viewModelScope.launch {
            repo.updateRoutineByDay(map)
        }
    }

    fun skipRoutine() {
        viewModelScope.launch {
            repo.skipRoutine()
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            repo.setOnboardingCompleted(completed)
        }
    }
}
