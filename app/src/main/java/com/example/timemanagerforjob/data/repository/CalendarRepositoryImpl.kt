package com.example.timemanagerforjob.data.repository

import com.example.timemanagerforjob.domain.repository.CalendarRepository
import com.example.timemanagerforjob.data.local.dao.SelectedDayDao
import com.example.timemanagerforjob.data.local.entity.SelectedDayEntity
import com.example.timemanagerforjob.utils.preferences.AppPreferences
import java.time.YearMonth
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    private val dao: SelectedDayDao,
    private val appPreferences: AppPreferences
) : CalendarRepository {
    override fun getDaysOfMonth(yearMonth: YearMonth): List<Int> {
        return (1..yearMonth.lengthOfMonth()).toList()
    }

    override suspend fun getSelectedDays(year: Int, month: Int): List<Int> {
        val userEmail = appPreferences.getUserEmail() ?: return emptyList()
        return try {
            dao.getSelectedDaysForMonth(month, year, userEmail).map { it.day }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun saveSelectedDay(day: Int, month: Int, year: Int) {
        val userEmail = appPreferences.getUserEmail() ?: return
        dao.insert(SelectedDayEntity(day = day, month = month, year = year, userEmail = userEmail))
    }

    override suspend fun removeSelectedDay(day: Int, month: Int, year: Int) {
        val userEmail = appPreferences.getUserEmail() ?: return
        val selectedDay = dao.getSelectedDay(day, month, year, userEmail)
        if (selectedDay != null) {
            dao.delete(selectedDay)
        }
    }

    override suspend fun initializeWeekendDays(year: Int, month: Int) {
        val userEmail = appPreferences.getUserEmail() ?: return
        val yearMonth = YearMonth.of(year, month)
        val existingDays = dao.getSelectedDaysForMonth(month, year, userEmail).map { it.day }.toSet()

        val daysInMonth = yearMonth.lengthOfMonth()
        val weekendDays = (1..daysInMonth).filter { day ->
            yearMonth.atDay(day).dayOfWeek.value >= 6
        }

        for (day in weekendDays) {
            if (!existingDays.contains(day)) {
                dao.insert(SelectedDayEntity(day = day, month = month, year = year, userEmail = userEmail))
            }
        }
    }
}
