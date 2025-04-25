package com.example.timemanagerforjob.domain.model

import java.time.LocalDate

data class TimeReport(
    val date: LocalDate,
    val startTime: Long,
    val endTime: Long?
) {
    val durationMillis: Long?
        get() = endTime?.let { it - startTime }
}