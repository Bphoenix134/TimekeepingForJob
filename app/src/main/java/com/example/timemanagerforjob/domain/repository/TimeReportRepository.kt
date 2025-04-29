package com.example.timemanagerforjob.domain.repository

import com.example.timemanagerforjob.domain.model.TimeReport
import com.example.timemanagerforjob.domain.model.Result
import java.time.LocalDate
import java.time.YearMonth

interface TimeReportRepository {
    suspend fun saveReport(report: TimeReport): Unit
    suspend fun getReportByDate(date: LocalDate): Result<TimeReport>
    suspend fun getReportsByMonth(month: YearMonth): Result<List<TimeReport>>
}