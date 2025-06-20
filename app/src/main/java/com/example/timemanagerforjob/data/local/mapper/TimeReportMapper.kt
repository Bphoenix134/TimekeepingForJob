package com.example.timemanagerforjob.data.local.mapper

import com.example.timemanagerforjob.data.local.entity.TimeReportEntity
import com.example.timemanagerforjob.domain.model.TimeReport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun TimeReport.toEntity(): TimeReportEntity {
    return TimeReportEntity(
        date = date,
        startTime = startTime,
        endTime = endTime,
        workTime = workTime,
        pauses = Json.encodeToString(pauses),
        userEmail = ""
    )
}

fun TimeReportEntity.toDomain(): TimeReport {
    return TimeReport(
        date = date,
        startTime = startTime,
        endTime = endTime,
        workTime = workTime,
        pauses = Json.decodeFromString(pauses)
    )
}