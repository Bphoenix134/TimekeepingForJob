package com.example.timemanagerforjob.domain.repository

import java.time.YearMonth

interface CalendarRepository {
    fun getDaysOfMonth(yearMonth: YearMonth): List<Int>
}