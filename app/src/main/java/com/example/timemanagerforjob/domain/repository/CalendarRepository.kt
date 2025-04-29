package com.example.timemanagerforjob.domain.repository

import java.time.YearMonth

interface CalendarRepository {
    fun getDaysOfMonth(yearMonth: YearMonth): List<Int>
    suspend fun getSelectedDays(year: Int, month: Int): List<Int>
    suspend fun saveSelectedDay(day: Int, month: Int, year: Int)
    suspend fun removeSelectedDay(day: Int, month: Int, year: Int)
    suspend fun initializeWeekendDays(year: Int, month: Int)
}