package com.example.timemanagerforjob.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.timemanagerforjob.data.local.dao.SelectedDayDao
import com.example.timemanagerforjob.data.local.entity.SelectedDayEntity

@Database(entities = [SelectedDayEntity::class], version = 1)
abstract class CalendarDatabase : RoomDatabase() {
    abstract fun selectedDayDao(): SelectedDayDao
}