package com.example.timemanagerforjob.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "selected_days",
    primaryKeys = ["day", "month", "year"]
)
data class SelectedDayEntity(
    val day: Int,
    val month: Int,
    val year: Int
)