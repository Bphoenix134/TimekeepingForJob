package com.example.timemanagerforjob.presentation.statistics

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timemanagerforjob.domain.model.Result
import com.example.timemanagerforjob.domain.repository.CalendarRepository
import com.example.timemanagerforjob.domain.repository.TimeReportRepository
import com.example.timemanagerforjob.utils.ExcelExportUtil
import com.example.timemanagerforjob.utils.formatters.TimeFormatter
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
class StatisticsViewModel @Inject constructor(
    private val timeReportRepository: TimeReportRepository,
    private val calendarRepository: CalendarRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun setMode(mode: StatisticsMode) {
        _uiState.update { it.copy(mode = mode) }
        loadStatistics()
    }

    fun navigatePrevious() {
        _uiState.update {
            when (it.mode) {
                StatisticsMode.DAY -> it.copy(currentDate = it.currentDate.minusDays(1))
                StatisticsMode.WEEK -> it.copy(
                    currentWeek = Pair(
                        it.currentWeek.first.minusWeeks(1),
                        it.currentWeek.second.minusWeeks(1)
                    )
                )
                StatisticsMode.MONTH -> it.copy(currentMonth = it.currentMonth.minusMonths(1))
            }
        }
        loadStatistics()
    }

    fun navigateNext() {
        _uiState.update {
            when (it.mode) {
                StatisticsMode.DAY -> it.copy(currentDate = it.currentDate.plusDays(1))
                StatisticsMode.WEEK -> it.copy(
                    currentWeek = Pair(
                        it.currentWeek.first.plusWeeks(1),
                        it.currentWeek.second.plusWeeks(1)
                    )
                )
                StatisticsMode.MONTH -> it.copy(currentMonth = it.currentMonth.plusMonths(1))
            }
        }
        loadStatistics()
    }

    fun exportToExcel(context: android.content.Context) {
        viewModelScope.launch {
            val month = _uiState.value.currentMonth
            val result = timeReportRepository.getReportsByMonth(month)
            val selectedDays = calendarRepository.getSelectedDays(month.year, month.monthValue)
            when (result) {
                is Result.Success -> {
                    val exportResult = ExcelExportUtil.exportMonthlyStatistics(
                        context,
                        result.value,
                        month,
                        selectedDays
                    )
                    _uiState.update {
                        it.copy(
                            exportResult = exportResult.getOrNull(),
                            exportError = if (exportResult is Result.Failure) exportResult.exception.message else null
                        )
                    }
                }
                is Result.Failure -> {
                    _uiState.update {
                        it.copy(
                            exportResult = null,
                            exportError = result.exception.message ?: "Failed to load data for export"
                        )
                    }
                }
            }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val mode = _uiState.value.mode) {
                StatisticsMode.DAY -> loadDayStatistics()
                StatisticsMode.WEEK -> loadWeekStatistics()
                StatisticsMode.MONTH -> loadMonthStatistics()
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadDayStatistics() {
        val date = _uiState.value.currentDate
        when (val result = timeReportRepository.getReportByDate(date)) {
            is Result.Success -> {
                val report = result.value
                Log.d("StatisticsViewModel", "Loaded report for $date: $report")
                val data = report?.let {
                    DayStatisticsData(
                        report = it,
                        startTime = it.startTime.formatTimeForStartAndEnd(),
                        endTime = it.endTime?.formatTimeForStartAndEnd(),
                        totalPauseTime = it.pauses.sumOf { pause ->
                            val end = pause.second ?: System.currentTimeMillis()
                            end - pause.first
                        }
                    )
                }
                _uiState.update { it.copy(statisticsData = data, errorMessage = null) }
            }
            is Result.Failure -> {
                Log.e("StatisticsViewModel", "Failed to load report: ${result.exception}")
                _uiState.update {
                    it.copy(
                        statisticsData = null,
                        errorMessage = result.exception.message ?: "Failed to load day statistics"
                    )
                }
            }
        }
    }

    private suspend fun loadWeekStatistics() {
        val (start, end) = _uiState.value.currentWeek
        val reports = mutableListOf<com.example.timemanagerforjob.domain.model.TimeReport>()
        var currentDate = start
        while (!currentDate.isAfter(end)) {
            when (val result = timeReportRepository.getReportByDate(currentDate)) {
                is Result.Success -> result.value?.let { reports.add(it) }
                is Result.Failure -> {}
            }
            currentDate = currentDate.plusDays(1)
        }
        val selectedDays = calendarRepository.getSelectedDays(start.year, start.monthValue)
        var weekendDaysInWeek = 0
        var tempDate = start
        while (!tempDate.isAfter(end)) {
            if (selectedDays.contains(tempDate.dayOfMonth)) {
                weekendDaysInWeek++
            }
            tempDate = tempDate.plusDays(1)
        }
        val data = if (reports.isNotEmpty()) {
            WeekStatisticsData(
                reports = reports,
                totalWorkTime = reports.sumOf { it.workTime },
                averageWorkTime = if (reports.isNotEmpty()) reports.sumOf { it.workTime } / reports.size else 0L,
                totalPauseTime = reports.sumOf { report ->
                    report.pauses.sumOf { pause ->
                        val end = pause.second ?: System.currentTimeMillis()
                        end - pause.first
                    }
                },
                weekendsInWeek = weekendDaysInWeek
            )
        } else {
            WeekStatisticsData(
                reports = emptyList(),
                totalWorkTime = 0L,
                averageWorkTime = 0L,
                totalPauseTime = 0L,
                weekendsInWeek = weekendDaysInWeek
            )
        }
        _uiState.update { it.copy(statisticsData = data, errorMessage = null) }
    }

    private suspend fun loadMonthStatistics() {
        val month = _uiState.value.currentMonth
        when (val result = timeReportRepository.getReportsByMonth(month)) {
            is Result.Success -> {
                val reports = result.value
                val selectedDays = calendarRepository.getSelectedDays(month.year, month.monthValue)
                val weekendsInMonth = selectedDays.size
                val earnings = calculateMonthlyEarnings(reports, selectedDays)
                val data = if (reports.isNotEmpty()) {
                    MonthStatisticsData(
                        reports = reports,
                        totalWorkTime = reports.sumOf { it.workTime },
                        averageWorkTime = if (reports.isNotEmpty()) reports.sumOf { it.workTime } / reports.size else 0L,
                        totalPauseTime = reports.sumOf { report ->
                            report.pauses.sumOf { pause ->
                                val end = pause.second ?: System.currentTimeMillis()
                                end - pause.first
                            }
                        },
                        longestDay = reports.maxByOrNull { it.workTime },
                        shortestDay = reports.minByOrNull { it.workTime },
                        weekendsInMonth = weekendsInMonth,
                        totalEarnings = earnings
                    )
                } else {
                    MonthStatisticsData(
                        reports = emptyList(),
                        totalWorkTime = 0L,
                        averageWorkTime = 0L,
                        totalPauseTime = 0L,
                        longestDay = null,
                        shortestDay = null,
                        weekendsInMonth = weekendsInMonth,
                        totalEarnings = 0.0
                    )
                }
                _uiState.update { it.copy(statisticsData = data, errorMessage = null) }
            }
            is Result.Failure -> {
                _uiState.update {
                    it.copy(
                        statisticsData = null,
                        errorMessage = result.exception.message ?: "Failed to load month statistics"
                    )
                }
            }
        }
    }

    private fun calculateMonthlyEarnings(reports: List<com.example.timemanagerforjob.domain.model.TimeReport>, selectedDays: List<Int>): Double {
        val weekdayRate = appPreferences.getWeekdayHourlyRate().toDouble()
        val weekendRate = appPreferences.getWeekendHourlyRate().toDouble()
        return reports.sumOf { report ->
            val hours = report.workTime / (1000.0 * 60 * 60) // Convert milliseconds to hours
            val isWeekend = selectedDays.contains(report.date.dayOfMonth)
            hours * if (isWeekend) weekendRate else weekdayRate
        }
    }

    private fun Long.formatTime(): String {
        return TimeFormatter.formatTime(this)
    }

    private fun Long.formatTimeForStatistics(): String {
        return TimeFormatter.formatTimeForStatistics(this)
    }

    private fun Long.formatTimeForStartAndEnd(): String {
        return TimeFormatter.formatTimeForStartAndEnd(this)
    }
}