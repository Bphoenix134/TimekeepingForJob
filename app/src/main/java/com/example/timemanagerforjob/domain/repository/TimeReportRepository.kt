package com.example.timemanagerforjob.domain.repository

import com.example.timemanagerforjob.domain.model.TimeReport
import java.time.LocalDate

interface TimeReportRepository {
    suspend fun saveReport(report: TimeReport)
    suspend fun getReportByDate(date: LocalDate): TimeReport?
}