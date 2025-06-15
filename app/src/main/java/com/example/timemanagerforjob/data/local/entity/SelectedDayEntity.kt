package com.example.timemanagerforjob.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "selected_days")
data class SelectedDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val day: Int,
    val month: Int,
    val year: Int,
    val userEmail: String
)