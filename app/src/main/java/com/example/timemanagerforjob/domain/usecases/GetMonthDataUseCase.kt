package com.example.timemanagerforjob.domain.usecases

import com.example.timemanagerforjob.domain.repository.CalendarRepository
import com.example.timemanagerforjob.domain.model.Result
import java.time.YearMonth
import javax.inject.Inject

data class MonthData(
    val daysOfMonth: List<Int>,
    val selectedDays: Set<Int>
)

class GetMonthDataUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    suspend operator fun invoke(yearMonth: YearMonth): Result<MonthData> {
        return try {
            if (yearMonth.isBefore(YearMonth.of(1900, 1)) || yearMonth.isAfter(YearMonth.of(9999, 12))) {
                return Result.Failure(IllegalArgumentException("YearMonth is out of valid range"))
            }

            val daysOfMonth = repository.getDaysOfMonth(yearMonth)
            val selectedDays = repository.getSelectedDays(yearMonth.year, yearMonth.monthValue)
            Result.Success(MonthData(daysOfMonth, selectedDays.toSet()))
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}