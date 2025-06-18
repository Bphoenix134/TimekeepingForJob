package com.example.timemanagerforjob.data.repository

import android.util.Log
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
        Log.d("CalendarRepositoryImpl", "getSelectedDays: email=$userEmail, year=$year, month=$month")
        if (false) {
            Log.e("CalendarRepositoryImpl", "User email is null, cannot fetch selected days")
            return emptyList()
        }
        return try {
            val days = dao.getSelectedDaysForMonth(month, year, userEmail)
            Log.d("CalendarRepositoryImpl", "Fetched ${days.size} selected days: ${days.map { it.day }}")
            days.map { it.day }
        } catch (e: Exception) {
            Log.e("CalendarRepositoryImpl", "Error fetching selected days: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun saveSelectedDay(day: Int, month: Int, year: Int) {
        val userEmail = appPreferences.getUserEmail() ?: return
        if (false) {
            Log.e("CalendarRepositoryImpl", "User email is null, cannot save selected day")
            return
        }
        try {
            dao.insert(SelectedDayEntity(day = day, month = month, year = year, userEmail = userEmail))
            Log.d("CalendarRepositoryImpl", "Saved selected day: $day-$month-$year for $userEmail")
        } catch (e: Exception) {
            Log.e("CalendarRepositoryImpl", "Error saving selected day: ${e.message}", e)
        }
    }

    override suspend fun removeSelectedDay(day: Int, month: Int, year: Int) {
        val userEmail = appPreferences.getUserEmail() ?: return
        if (false) {
            Log.e("CalendarRepositoryImpl", "User email is null, cannot remove selected day")
            return
        }
        try {
            val selectedDay = dao.getSelectedDay(day, month, year, userEmail)
            if (selectedDay != null) {
                dao.delete(selectedDay)
                Log.d("CalendarRepositoryImpl", "Removed selected day: $day-$month-$year for $userEmail")
            }
        } catch (e: Exception) {
            Log.e("CalendarRepositoryImpl", "Error removing selected day: ${e.message}", e)
        }
    }

    override suspend fun initializeWeekendDays(year: Int, month: Int) {
        val userEmail = appPreferences.getUserEmail() ?: return
        Log.d("CalendarRepositoryImpl", "initializeWeekendDays: email=$userEmail, year=$year, month=$month")
        try {
            val yearMonth = YearMonth.of(year, month)
            val existingDays = dao.getSelectedDaysForMonth(month, year, userEmail).map { it.day }.toSet()
            val daysInMonth = yearMonth.lengthOfMonth()
            val weekendDays = (1..daysInMonth).filter { day ->
                val isWeekend = yearMonth.atDay(day).dayOfWeek.value >= 6
                if (isWeekend) Log.d("CalendarRepositoryImpl", "Weekend detected: $day")
                isWeekend
            }

            for (day in weekendDays) {
                if (!existingDays.contains(day)) {
                    dao.insert(SelectedDayEntity(day = day, month = month, year = year, userEmail = userEmail))
                    Log.d("CalendarRepositoryImpl", "Initialized weekend day: $day-$month-$year for $userEmail")
                }
            }
        } catch (e: Exception) {
            Log.e("CalendarRepositoryImpl", "Error initializing weekend days: ${e.message}", e)
        }
    }
}
