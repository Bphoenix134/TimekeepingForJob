package com.example.timemanagerforjob.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "time_reports")
data class TimeReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val startTime: Long,
    val endTime: Long?,
    val workTime: Long
)