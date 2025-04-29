package com.example.timemanagerforjob.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.timemanagerforjob.data.local.Converters
import com.example.timemanagerforjob.data.local.dao.SelectedDayDao
import com.example.timemanagerforjob.data.local.dao.TimeReportDao
import com.example.timemanagerforjob.data.local.entity.SelectedDayEntity
import com.example.timemanagerforjob.data.local.entity.TimeReportEntity

@Database(
    entities = [SelectedDayEntity::class, TimeReportEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun selectedDayDao(): SelectedDayDao
    abstract fun timeReportDao(): TimeReportDao
}