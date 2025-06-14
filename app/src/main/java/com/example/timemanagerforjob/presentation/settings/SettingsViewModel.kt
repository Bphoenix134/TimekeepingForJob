package com.example.timemanagerforjob.presentation.settings


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanagerforjob.auth.AuthRepository
import com.example.timemanagerforjob.utils.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject constructor(
    private val appPreferences: AppPreferences,
    private val authRepository: AuthRepository ) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val userEmail = appPreferences.getUserEmail() ?: authRepository.getCurrentUser()?.email
        _uiState.value = SettingsUiState(
            weekdayRate = appPreferences.getWeekdayHourlyRate(),
            weekendRate = appPreferences.getWeekendHourlyRate(),
            userEmail = userEmail
        )
    }

    fun updateWeekdayRate(rate: Float) {
        appPreferences.setWeekdayHourlyRate(rate)
        _uiState.value = _uiState.value.copy(weekdayRate = rate)
    }

    fun updateWeekendRate(rate: Float) {
        appPreferences.setWeekendHourlyRate(rate)
        _uiState.value = _uiState.value.copy(weekendRate = rate)
    }

    fun switchAccount() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = _uiState.value.copy(userEmail = null)
            appPreferences.saveUserEmail(null)
        }
    }

}

data class SettingsUiState(
    val weekdayRate: Float = 500f,
    val weekendRate: Float = 750f,
    val userEmail: String? = null,
    val errorMessage: String? = null )
