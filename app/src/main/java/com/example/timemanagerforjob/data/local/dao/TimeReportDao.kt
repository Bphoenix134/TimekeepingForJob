package com.example.timemanagerforjob.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.timemanagerforjob.data.local.entity.TimeReportEntity
import java.time.LocalDate

@Dao
interface TimeReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeReport(report: TimeReportEntity)

    @Query("SELECT * FROM time_reports WHERE date = :date LIMIT 1")
    suspend fun getTimeReportByDate(date: LocalDate): TimeReportEntity?
}