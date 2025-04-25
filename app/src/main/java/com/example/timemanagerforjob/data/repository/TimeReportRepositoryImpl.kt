package com.example.timemanagerforjob.data.repository

import com.example.timemanagerforjob.data.local.dao.TimeReportDao
import com.example.timemanagerforjob.data.mapper.toDomain
import com.example.timemanagerforjob.data.mapper.toEntity
import com.example.timemanagerforjob.domain.model.TimeReport
import com.example.timemanagerforjob.domain.repository.TimeReportRepository
import java.time.LocalDate

class TimeReportRepositoryImpl(
    private val dao: TimeReportDao
) : TimeReportRepository {

    override suspend fun saveReport(report: TimeReport) {
        dao.insertTimeReport(report.toEntity())
    }

    override suspend fun getReportByDate(date: LocalDate): TimeReport? {
        return dao.getTimeReportByDate(date)?.toDomain()
    }
}