package com.example.timemanagerforjob.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.timemanagerforjob.data.local.dao.TimeReportDao
import com.example.timemanagerforjob.data.mapper.toDomain
import com.example.timemanagerforjob.data.mapper.toEntity
import com.example.timemanagerforjob.domain.model.TimeReport
import com.example.timemanagerforjob.domain.repository.TimeReportRepository
import com.example.timemanagerforjob.domain.model.Result
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class TimeReportRepositoryImpl @Inject constructor(
    private val dao: TimeReportDao
) : TimeReportRepository {

    override suspend fun saveReport(report: TimeReport): Unit {
        dao.insertTimeReport(report.toEntity())
    }

    override suspend fun getReportByDate(date: LocalDate): Result<TimeReport> {
        return try {
            val report = dao.getTimeReportByDate(date)?.toDomain()
            Result.Success(report ?: return Result.Failure(Exception("No report found for date")))
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getReportsByMonth(month: YearMonth): Result<List<TimeReport>> {
        return try {
            val reports = dao.getTimeReportsForMonth(
                month.year,
                month.monthValue
            ).map { it.toDomain() }
            Result.Success(reports)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}