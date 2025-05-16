package com.example.timemanagerforjob.presentation.work

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.serialization.encodeToString
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanagerforjob.data.local.dao.TimeReportDao
import com.example.timemanagerforjob.data.local.entity.TimeReportEntity
import com.example.timemanagerforjob.domain.repository.CalendarRepository
import com.example.timemanagerforjob.domain.repository.TimeReportRepository
import com.example.timemanagerforjob.domain.usecases.GetMonthDataUseCase
import com.example.timemanagerforjob.domain.usecases.ManageTimeReportUseCase
import com.example.timemanagerforjob.utils.preferences.AppPreferences
import com.example.timemanagerforjob.domain.model.Result
import com.example.timemanagerforjob.services.WorkSessionService
import com.example.timemanagerforjob.utils.events.SessionEvent
import com.example.timemanagerforjob.utils.events.SessionEventBus
import com.example.timemanagerforjob.utils.formatters.TimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.serialization.json.Json

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
@SuppressLint("StaticFieldLeak")
class WorkViewModel @Inject constructor(
    private val timeReportRepository: TimeReportRepository,
    private val timeReportDao: TimeReportDao,
    private val calendarRepository: CalendarRepository,
    private val getMonthDataUseCase: GetMonthDataUseCase,
    private val manageTimeReportUseCase: ManageTimeReportUseCase,
    private val appPreferences: AppPreferences,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkUiState())
    val uiState: StateFlow<WorkUiState> = _uiState.asStateFlow()

    init {
        insertAprilReportsForTesting()
        initializeFirstLaunch()
        loadMonthData()

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

    //Просто добавление насильно данных для отчёта
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertAprilReportsForTesting() {
        viewModelScope.launch {
            insertAprilReports(timeReportDao, calendarRepository)
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
            Log.d("WorkViewModel", "Attempting to start session for date: ${LocalDate.now()}")
            when (val result = manageTimeReportUseCase.startSession(LocalDate.now())) {
                is Result.Success -> {
                    Log.d("WorkViewModel", "Session started: ${result.value}")
                    _uiState.update {
                        it.copy(
                            sessionState = result.value,
                            reportState = null,
                            isPaused = result.value.pauses.any { it.second == null },
                            workedTime = result.value.calculateWorkTime(),
                            errorMessage = null
                        )
                    }
                    WorkSessionService.startService(context, result.value)
                }
                is Result.Failure -> {
                    Log.e("WorkViewModel", "Failed to start session: ${result.exception}")
                    val errorMessage = when (result.exception.message) {
                        "Active session already exists for this date" -> "Сеанс уже активен. Завершите текущий сеанс."
                        "Session already completed for this date" -> "Сеанс за ${LocalDate.now()} уже завершён. Вы можете начать новый сеанс завтра."
                        else -> "Не удалось начать сеанс: ${result.exception.message}"
                    }
                    _uiState.update {
                        it.copy(
                            errorMessage = errorMessage
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

    //Добавление насильно данные
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insertAprilReports(
        timeReportDao: TimeReportDao,
        calendarRepository: CalendarRepository
    ) {
        val year = 2025
        val month = 4
        val yearMonth = YearMonth.of(year, month)
        val totalWorkHours = 160
        val millisecondsPerHour = 1000L * 60 * 60

        // Определяем текущий день (30 апреля 2025 года)
        val currentDate = LocalDate.of(2025, 4, 30) // Можно заменить на LocalDate.now() в реальном коде

        // Получаем выходные дни из базы данных
        val selectedDays = calendarRepository.getSelectedDays(year, month)
        // Фильтруем рабочие дни, исключая текущий день
        val workingDays = (1..yearMonth.lengthOfMonth())
            .filter { !selectedDays.contains(it) } // Исключаем выходные
            .filter { day -> LocalDate.of(year, month, day) != currentDate } // Исключаем текущий день

        if (workingDays.isEmpty()) {
            Log.e("InsertAprilReports", "No working days found for April 2025")
            return
        }

        // Рассчитываем рабочее время на день
        val hoursPerDay = totalWorkHours.toDouble() / workingDays.size
        val workTimeMillisPerDay = (hoursPerDay * millisecondsPerHour).toLong()

        // Для каждого рабочего дня создаём запись
        workingDays.forEach { day ->
            val date = LocalDate.of(year, month, day)
            val startTime = date.atTime(9, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
            val endTime = startTime + workTimeMillisPerDay // Предполагаем, что endTime = startTime + workTime

            val reportEntity = TimeReportEntity(
                date = date,
                startTime = startTime,
                endTime = endTime,
                workTime = workTimeMillisPerDay,
                pauses = Json.encodeToString(emptyList<Pair<Long, Long?>>())
            )

            try {
                timeReportDao.insertTimeReport(reportEntity)
                Log.d("InsertAprilReports", "Inserted report for $date: ${TimeFormatter.formatTime(workTimeMillisPerDay)}")
            } catch (e: Exception) {
                Log.e("InsertAprilReports", "Failed to insert report for $date: ${e.message}", e)
            }
        }
    }
}