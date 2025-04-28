package com.example.timemanagerforjob.data.mapper

import com.example.timemanagerforjob.data.local.entity.TimeReportEntity
import com.example.timemanagerforjob.domain.model.TimeReport

fun TimeReportEntity.toDomain(): TimeReport {
    return TimeReport(
        date = date,
        startTime = startTime,
        endTime = endTime,
        workTime = workTime
    )
}

fun TimeReport.toEntity(): TimeReportEntity {
    return TimeReportEntity(
        date = date,
        startTime = startTime,
        endTime = endTime,
        workTime = workTime
    )
}