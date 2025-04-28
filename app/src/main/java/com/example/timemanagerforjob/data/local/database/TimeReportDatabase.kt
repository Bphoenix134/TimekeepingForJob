package com.example.timemanagerforjob.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.timemanagerforjob.data.local.Converters
import com.example.timemanagerforjob.data.local.dao.TimeReportDao
import com.example.timemanagerforjob.data.local.entity.TimeReportEntity

@Database(
    entities = [TimeReportEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TimeReportDatabase : RoomDatabase() {
    abstract fun timeReportDao(): TimeReportDao
}