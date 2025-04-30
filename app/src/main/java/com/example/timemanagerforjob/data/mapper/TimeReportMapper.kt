package com.example.timemanagerforjob.data.mapper

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.timemanagerforjob.data.local.entity.TimeReportEntity
import com.example.timemanagerforjob.domain.model.TimeReport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@RequiresApi(Build.VERSION_CODES.O)
fun TimeReportEntity.toDomain(): TimeReport {
    return TimeReport(
        date = date,
        startTime = startTime,
        endTime = endTime,
        workTime = workTime,
        pauses = Json.decodeFromString(pauses)
    )
}

fun TimeReport.toEntity(): TimeReportEntity {
    Log.d("TimeReportMapper", "Converting TimeReport to Entity: $this")
    val entity = TimeReportEntity(
        date = date,
        startTime = startTime,
        endTime = endTime,
        workTime = workTime,
        pauses = Json.encodeToString(pauses)
    )
    Log.d("TimeReportMapper", "Created TimeReportEntity: $entity")
    return entity
}