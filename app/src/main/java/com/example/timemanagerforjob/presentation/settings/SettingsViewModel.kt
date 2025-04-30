package com.example.timemanagerforjob.presentation.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanagerforjob.utils.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadRates()
    }

    private fun loadRates() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    weekdayRate = appPreferences.getWeekdayHourlyRate().toString(),
                    weekendRate = appPreferences.getWeekendHourlyRate().toString()
                )
            }
        }
    }

    fun updateWeekdayRate(value: String) {
        _uiState.update { it.copy(weekdayRate = value) }
    }

    fun updateWeekendRate(value: String) {
        _uiState.update { it.copy(weekendRate = value) }
    }

    fun saveRates() {
        viewModelScope.launch {
            try {
                val weekdayRate = _uiState.value.weekdayRate.toFloatOrNull()
                val weekendRate = _uiState.value.weekendRate.toFloatOrNull()

                if (weekdayRate == null || weekdayRate <= 0 || weekendRate == null || weekendRate <= 0) {
                    _uiState.update {
                        it.copy(errorMessage = "Введите корректные положительные ставки")
                    }
                    return@launch
                }

                appPreferences.setWeekdayHourlyRate(weekdayRate)
                appPreferences.setWeekendHourlyRate(weekendRate)
                _uiState.update {
                    it.copy(
                        successMessage = "Ставки успешно сохранены",
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Ошибка сохранения ставок: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

data class SettingsUiState(
    val weekdayRate: String = "",
    val weekendRate: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

