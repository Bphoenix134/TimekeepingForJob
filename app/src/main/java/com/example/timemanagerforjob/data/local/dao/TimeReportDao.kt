package com.example.timemanagerforjob.data.local.dao

import androidx.room.*
import com.example.timemanagerforjob.data.local.entity.TimeReportEntity
import java.time.LocalDate

@Dao
interface TimeReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeReport(report: TimeReportEntity)

    @Query("UPDATE time_reports SET startTime = :startTime, endTime = :endTime, workTime = :workTime, pauses = :pauses WHERE date = :date AND userEmail = :userEmail")
    suspend fun updateTimeReport(date: LocalDate, startTime: Long, endTime: Long?, workTime: Long, pauses: String, userEmail: String)

    @Query("SELECT * FROM time_reports WHERE date = :date AND userEmail = :userEmail LIMIT 1")
    suspend fun getTimeReportByDate(date: LocalDate, userEmail: String): TimeReportEntity?

    @Query("SELECT * FROM time_reports WHERE strftime('%Y-%m', date) = :year || '-' || printf('%02d', :month) AND userEmail = :userEmail")
    suspend fun getTimeReportsForMonth(year: Int, month: Int, userEmail: String): List<TimeReportEntity>
}