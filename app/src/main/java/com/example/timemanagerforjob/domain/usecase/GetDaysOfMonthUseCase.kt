package com.example.timemanagerforjob.domain.usecase

import com.example.timemanagerforjob.domain.repository.CalendarRepository
import java.time.YearMonth

class GetDaysOfMonthUseCase(private val repository: CalendarRepository) {
    operator fun invoke(yearMonth: YearMonth): List<Int> {
        return repository.getDaysOfMonth(yearMonth)
    }
}