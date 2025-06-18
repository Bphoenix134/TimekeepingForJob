package com.example.timemanagerforjob.presentation.work

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanagerforjob.data.local.dao.TimeReportDao
import com.example.timemanagerforjob.data.local.entity.TimeReportEntity
import com.example.timemanagerforjob.domain.model.Result
import com.example.timemanagerforjob.domain.model.TimeReport
import com.example.timemanagerforjob.domain.repository.CalendarRepository
import com.example.timemanagerforjob.domain.repository.TimeReportRepository
import com.example.timemanagerforjob.utils.ErrorHandler
import com.example.timemanagerforjob.utils.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val timeReportRepository: TimeReportRepository,
    private val timeReportDao: TimeReportDao,
    private val calendarRepository: CalendarRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadTodayReport()
        insertAprilReportsForTesting()
    }

    fun loadTodayReport() {
        viewModelScope.launch {
            val userEmail = appPreferences.getUserEmail()
            Log.d("ReportViewModel", "Loading report for today, userEmail: $userEmail")
            if (userEmail == null) {
                Log.e("ReportViewModel", "No user logged in, skipping report load")
                _uiState.update { it.copy(reportState = null, errorMessage = "Необходимо авторизоваться") }
                return@launch
            }
            when (val result = timeReportRepository.getReportByDate(LocalDate.now())) {
                is Result.Success -> {
                    Log.d("ReportViewModel", "Report loaded: ${result.value}")
                    _uiState.update {
                        it.copy(
                            reportState = result.value,
                            errorMessage = null
                        )
                    }
                }
                is Result.Failure -> {
                    Log.e("ReportViewModel", "Failed to load report: ${result.exception.message}")
                    if (result.exception.message == "No report found for date") {
                        _uiState.update { it.copy(reportState = null, errorMessage = null) }
                    } else {
                        ErrorHandler.emitError("Не удалось загрузить отчёт за сегодня: ${result.exception.message}")
                        _uiState.update { it.copy(reportState = null) }
                    }
                }
            }
        }
    }

    fun insertAprilReportsForTesting() {
        viewModelScope.launch {
            insertAprilReports(timeReportDao, calendarRepository, appPreferences)
        }
    }

    private suspend fun insertAprilReports(
        timeReportDao: TimeReportDao,
        calendarRepository: CalendarRepository,
        appPreferences: AppPreferences
    ) {
        val year = 2025
        val month = 4
        val yearMonth = YearMonth.of(year, month)
        val totalWorkHours = 160
        val millisecondsPerHour = 1000L * 60 * 60

        val currentDate = LocalDate.of(2025, 4, 30)

        val selectedDays = calendarRepository.getSelectedDays(year, month)
        val workingDays = (1..yearMonth.lengthOfMonth())
            .filter { !selectedDays.contains(it) }
            .filter { day -> LocalDate.of(year, month, day) != currentDate }

        if (workingDays.isEmpty()) {
            return
        }

        val hoursPerDay = totalWorkHours.toDouble() / workingDays.size
        val workTimeMillisPerDay = (hoursPerDay * millisecondsPerHour).toLong()
        val userEmail = appPreferences.getUserEmail()

        workingDays.forEach { day ->
            val date = LocalDate.of(year, month, day)
            val startTime = date.atTime(9, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
            val endTime = startTime + workTimeMillisPerDay

            val reportEntity = TimeReportEntity(
                date = date,
                startTime = startTime,
                endTime = endTime,
                workTime = workTimeMillisPerDay,
                pauses = Json.encodeToString(emptyList<Pair<Long, Long?>>()),
                userEmail = userEmail.toString()
            )

            try {
                timeReportDao.insertTimeReport(reportEntity)
            } catch (e: Exception) {
                ErrorHandler.emitError("Не удалось вставить отчёт за $date: ${e.message}")
            }
        }
    }
}

data class ReportUiState(
    val reportState: TimeReport? = null,
    val errorMessage: String? = null
)