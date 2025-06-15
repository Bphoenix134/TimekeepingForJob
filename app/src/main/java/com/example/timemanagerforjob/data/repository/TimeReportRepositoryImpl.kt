package com.example.timemanagerforjob.data.repository

import com.example.timemanagerforjob.data.local.dao.TimeReportDao
import com.example.timemanagerforjob.data.local.mapper.toDomain
import com.example.timemanagerforjob.data.local.mapper.toEntity
import com.example.timemanagerforjob.domain.model.TimeReport
import com.example.timemanagerforjob.domain.repository.TimeReportRepository
import com.example.timemanagerforjob.domain.model.Result
import com.example.timemanagerforjob.utils.preferences.AppPreferences
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TimeReportRepositoryImpl @Inject constructor(
    private val timeReportDao: TimeReportDao,
    private val appPreferences: AppPreferences
) : TimeReportRepository {

    override suspend fun saveReport(report: TimeReport) {
        val userEmail = appPreferences.getUserEmail() ?: return
        val entity = report.toEntity().copy(userEmail = userEmail)
        val existingReport = timeReportDao.getTimeReportByDate(report.date, userEmail)
        if (existingReport != null) {
            timeReportDao.updateTimeReport(
                date = report.date,
                startTime = report.startTime,
                endTime = report.endTime,
                workTime = report.workTime,
                pauses = Json.encodeToString(report.pauses),
                userEmail = userEmail
            )
        } else {
            timeReportDao.insertTimeReport(entity)
        }
    }

    override suspend fun getReportByDate(date: LocalDate): Result<TimeReport> {
        val userEmail = appPreferences.getUserEmail() ?: return Result.Failure(Exception("No user logged in"))
        return try {
            val report = timeReportDao.getTimeReportByDate(date, userEmail)?.toDomain()
            Result.Success(report ?: return Result.Failure(Exception("No report found for date")))
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun getReportsByMonth(month: YearMonth): Result<List<TimeReport>> {
        val userEmail = appPreferences.getUserEmail() ?: return Result.Failure(Exception("No user logged in"))
        return try {
            val reports = timeReportDao.getTimeReportsForMonth(
                month.year,
                month.monthValue,
                userEmail
            ).map { it.toDomain() }
            Result.Success(reports)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}