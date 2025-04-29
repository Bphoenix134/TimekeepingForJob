package com.example.timemanagerforjob.presentation.work

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanagerforjob.domain.repository.CalendarRepository
import com.example.timemanagerforjob.domain.repository.TimeReportRepository
import com.example.timemanagerforjob.domain.usecases.GetMonthDataUseCase
import com.example.timemanagerforjob.domain.usecases.ManageTimeReportUseCase
import com.example.timemanagerforjob.utils.preferences.AppPreferences
import com.example.timemanagerforjob.domain.model.Result
import com.example.timemanagerforjob.services.WorkSessionService
import com.example.timemanagerforjob.utils.events.SessionEvent
import com.example.timemanagerforjob.utils.events.SessionEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
@SuppressLint("StaticFieldLeak")
class WorkViewModel @Inject constructor(
    private val timeReportRepository: TimeReportRepository,
    private val calendarRepository: CalendarRepository,
    private val getMonthDataUseCase: GetMonthDataUseCase,
    private val manageTimeReportUseCase: ManageTimeReportUseCase,
    private val appPreferences: AppPreferences,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkUiState())
    val uiState: StateFlow<WorkUiState> = _uiState.asStateFlow()

    init {
        initializeFirstLaunch()
        loadMonthData()

        // Подписываемся на события через SharedFlow
        viewModelScope.launch {
            SessionEventBus.sessionEvents.collect { event ->
                when (event) {
                    is SessionEvent.SessionStopped -> {
                        Log.d("WorkViewModel", "Received stop event: ${event.timeReport}")
                        _uiState.update {
                            it.copy(
                                sessionState = null,
                                reportState = event.timeReport,
                                workedTime = event.timeReport.workTime,
                                isPaused = false,
                                errorMessage = null
                            )
                        }
                    }
                    is SessionEvent.SessionPausedResumed -> {
                        Log.d("WorkViewModel", "Received pause/resume event: ${event.session}, isPaused=${event.isPaused}")
                        _uiState.update {
                            it.copy(
                                sessionState = event.session,
                                isPaused = event.isPaused,
                                workedTime = event.session.calculateWorkTime(),
                                errorMessage = null
                            )
                        }
                    }
                    is SessionEvent.SessionUpdated -> {
                        Log.d("WorkViewModel", "Received update event: workTime=${event.workTime}, isPaused=${event.isPaused}")
                        _uiState.update {
                            it.copy(
                                sessionState = event.session,
                                workedTime = event.workTime,
                                isPaused = event.isPaused,
                                errorMessage = null
                            )
                        }
                    }
                }
            }
        }
    }

    private fun initializeFirstLaunch() {
        if (appPreferences.isFirstLaunch()) {
            viewModelScope.launch {
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
            _uiState.update { it.copy(isLoading = true) }
            val month = _uiState.value.currentMonth

            val yearInitialized = appPreferences.isYearInitialized(month.year)
            if (!yearInitialized) {
                for (monthValue in 1..12) {
                    calendarRepository.initializeWeekendDays(month.year, monthValue)
                }
                appPreferences.setYearInitialized(month.year)
            }

            when (val result = getMonthDataUseCase(month)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            selectedDays = result.value.selectedDays.toSet(),
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is Result.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Failed to load month data"
                        )
                    }
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
                _uiState.update {
                    it.copy(errorMessage = "Failed to update day: ${e.message}")
                }
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

    @SuppressLint("NewApi")
    fun startTimeReport() {
        viewModelScope.launch {
            when (val result = manageTimeReportUseCase.startSession(LocalDate.now())) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            sessionState = result.value,
                            reportState = null,
                            isPaused = false,
                            workedTime = result.value.calculateWorkTime(),
                            errorMessage = null
                        )
                    }
                    WorkSessionService.startService(context, result.value)
                }
                is Result.Failure -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = result.exception.message ?: "Failed to start session"
                        )
                    }
                }
            }
        }
    }

    fun stopTimeReport() {
        WorkSessionService.stopService(context)
    }

    fun pauseTimeReport() {
        val pauseResumeIntent = Intent(context, WorkSessionService::class.java).apply {
            action = WorkSessionService.ACTION_PAUSE_RESUME
            putExtra("isPaused", false)
        }
        ContextCompat.startForegroundService(context, pauseResumeIntent)
    }

    fun resumeTimeReport() {
        val pauseResumeIntent = Intent(context, WorkSessionService::class.java).apply {
            action = WorkSessionService.ACTION_PAUSE_RESUME
            putExtra("isPaused", true)
        }
        ContextCompat.startForegroundService(context, pauseResumeIntent)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
    }
}