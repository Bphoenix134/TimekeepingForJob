package com.example.timemanagerforjob.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "time_reports")
data class TimeReportEntity(
    @PrimaryKey
    val date: LocalDate,
    val startTime: Long,
    val endTime: Long?,
    val workTime: Long,
    val pauses: String,
    val userEmail: String
)