package com.example.timemanagerforjob.presentation.statistics

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.timemanagerforjob.domain.model.TimeReport
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.YearMonth

enum class StatisticsMode {
    DAY, WEEK, MONTH
}

@RequiresApi(Build.VERSION_CODES.O)
data class StatisticsUiState(
    val mode: StatisticsMode = StatisticsMode.DAY,
    val currentDate: LocalDate = now(),
    val currentWeek: Pair<LocalDate, LocalDate> = now().run {
        val start = this.minusDays(this.dayOfWeek.value.toLong() - 1)
        val end = start.plusDays(6)
        Pair(start, end)
    },
    val currentMonth: YearMonth = YearMonth.now(),
    val statisticsData: StatisticsData? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val exportResult: String? = null,
    val exportError: String? = null
)

sealed interface StatisticsData

data class DayStatisticsData(
    val report: TimeReport,
    val startTime: String,
    val endTime: String?,
    val totalPauseTime: Long
) : StatisticsData

data class WeekStatisticsData(
    val reports: List<TimeReport>,
    val totalWorkTime: Long,
    val averageWorkTime: Long,
    val totalPauseTime: Long,
    val weekendsInWeek: Int
) : StatisticsData

data class MonthStatisticsData(
    val reports: List<TimeReport>,
    val totalWorkTime: Long,
    val averageWorkTime: Long,
    val totalPauseTime: Long,
    val longestDay: TimeReport?,
    val shortestDay: TimeReport?,
    val weekendsInMonth: Int,
    val totalEarnings: Double
) : StatisticsData