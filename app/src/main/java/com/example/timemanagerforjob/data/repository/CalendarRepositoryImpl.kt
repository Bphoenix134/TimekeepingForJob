package com.example.timemanagerforjob.data.repository

import com.example.timemanagerforjob.domain.repository.CalendarRepository
import java.time.YearMonth
import com.example.timemanagerforjob.data.local.dao.SelectedDayDao
import com.example.timemanagerforjob.data.local.entity.SelectedDayEntity
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    private val dao: SelectedDayDao
) : CalendarRepository {
    override fun getDaysOfMonth(yearMonth: YearMonth): List<Int> {
        return (1..yearMonth.lengthOfMonth()).toList()
    }

    override suspend fun getSelectedDays(year: Int, month: Int): List<Int> {
        return try {
            dao.getSelectedDaysForMonth(month, year).map { it.day }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun saveSelectedDay(day: Int, month: Int, year: Int) {
        dao.insert(SelectedDayEntity(day = day, month = month, year = year))
    }

    override suspend fun removeSelectedDay(day: Int, month: Int, year: Int) {
        val selectedDay = dao.getSelectedDay(day, month, year)
        if (selectedDay != null) {
            dao.delete(selectedDay)
        }
    }

    override suspend fun initializeWeekendDays(year: Int, month: Int) {
        val yearMonth = YearMonth.of(year, month)
        val existingDays = dao.getSelectedDaysForMonth(month, year).map { it.day }.toSet()

        val daysInMonth = yearMonth.lengthOfMonth()
        val weekendDays = (1..daysInMonth).filter { day ->
            yearMonth.atDay(day).dayOfWeek.value >= 6 // Saturday or Sunday
        }

        for (day in weekendDays) {
            if (!existingDays.contains(day)) {
                dao.insert(SelectedDayEntity(day = day, month = month, year = year))
            }
        }
    }
}
