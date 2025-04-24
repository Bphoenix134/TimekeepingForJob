package com.example.timemanagerforjob.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.timemanagerforjob.domain.repository.CalendarRepository
import java.time.YearMonth
import com.example.timemanagerforjob.data.local.dao.SelectedDayDao
import com.example.timemanagerforjob.data.local.entity.SelectedDayEntity
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    private val dao: SelectedDayDao
) : CalendarRepository {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getDaysOfMonth(yearMonth: YearMonth): List<Int> {
        return (1..yearMonth.lengthOfMonth()).toList()
    }

    override suspend fun getSelectedDays(year: Int, month: Int): List<Int> {
        return dao.getSelectedDaysForMonth(month, year).map { it.day }
    }

    override suspend fun saveSelectedDay(day: Int, month: Int, year: Int) {
        dao.insert(SelectedDayEntity(day = day, month = month, year = year))
    }

    override suspend fun removeSelectedDay(day: Int, month: Int, year: Int) {
        dao.delete(SelectedDayEntity(day = day, month = month, year = year))
    }
}
