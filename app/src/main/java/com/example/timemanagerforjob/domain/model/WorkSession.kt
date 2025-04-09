package com.example.timemanagerforjob.domain.model

import java.time.LocalDateTime

data class WorkSession(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val isWeekend: Boolean
)