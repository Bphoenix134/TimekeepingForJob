package com.example.timemanagerforjob.presentation.work

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanagerforjob.domain.model.Result
import com.example.timemanagerforjob.domain.repository.CalendarRepository
import com.example.timemanagerforjob.domain.usecases.GetMonthDataUseCase
import com.example.timemanagerforjob.utils.preferences.AppPreferences
import com.example.timemanagerforjob.utils.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val getMonthDataUseCase: GetMonthDataUseCase,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        initializeFirstLaunch()
        loadMonthData()
    }

    private fun initializeFirstLaunch() {
        if (appPreferences.isFirstLaunch()) {
            viewModelScope.launch {
                Log.d("CalendarViewModel", "FirstLaunch")
                val currentYear = YearMonth.now().year
                for (month in 1..12) {
                    calendarRepository.initializeWeekendDays(currentYear, month)
                }
                appPreferences.setFirstLaunchCompleted()
            }
        }
    }

    private fun loadMonthData() {
        viewModelScope.launch {
            Log.d("CalendarViewModel", "loadMonthData")
            _uiState.update { it.copy(isLoading = true) }
            val month = _uiState.value.currentMonth
            if (!appPreferences.isMonthInitialized(month)) {
                calendarRepository.initializeWeekendDays(month.year, month.monthValue)
                appPreferences.setMonthInitialized(month)
            }
            when (val result = getMonthDataUseCase(month)) {
                is Result.Success -> {
                    _uiState.update { it.copy(selectedDays = result.value.selectedDays.toSet(), isLoading = false) }
                }
                is Result.Failure -> {
                    ErrorHandler.emitError("Не удалось загрузить данные месяца: ${result.exception.message}")
                }
            }
        }
    }

    fun toggleDaySelection(day: Int) {
        val month = _uiState.value.currentMonth
        val currentDays = _uiState.value.selectedDays.toMutableSet()

        viewModelScope.launch {
            try {
                if (currentDays.contains(day)) {
                    calendarRepository.removeSelectedDay(day, month.monthValue, month.year)
                    currentDays.remove(day)
                } else {
                    calendarRepository.saveSelectedDay(day, month.monthValue, month.year)
                    currentDays.add(day)
                }
                _uiState.update { it.copy(selectedDays = currentDays.toSet(), errorMessage = null) }
            } catch (e: Exception) {
                ErrorHandler.emitError("Не удалось обновить день: ${e.message}")
            }
        }
    }

    fun goToPreviousMonth() {
        _uiState.update { it.copy(currentMonth = it.currentMonth.minusMonths(1)) }
        loadMonthData()
    }

    fun goToNextMonth() {
        _uiState.update { it.copy(currentMonth = it.currentMonth.plusMonths(1)) }
        loadMonthData()
    }
}
