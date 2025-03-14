package com.example.timemanagerforjob.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.timemanagerforjob.domain.repository.CalendarRepository
import java.time.YearMonth

class CalendarRepositoryImpl : CalendarRepository {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getDaysOfMonth(yearMonth: YearMonth): List<Int> {
        return (1..yearMonth.lengthOfMonth()).toList()
    }
}
