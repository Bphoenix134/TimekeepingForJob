package com.example.timemanagerforjob.domain.usecases

import android.util.Log
import com.example.timemanagerforjob.domain.model.TimeReport
import com.example.timemanagerforjob.domain.model.WorkSession
import com.example.timemanagerforjob.domain.repository.TimeReportRepository
import com.example.timemanagerforjob.domain.model.Result
import java.time.LocalDate
import javax.inject.Inject


class ManageTimeReportUseCase @Inject constructor(
    private val repository: TimeReportRepository
) {
    suspend fun startSession(date: LocalDate): Result<WorkSession> {
        val existingReport = repository.getReportByDate(date).getOrNull()
        if (existingReport != null) {
            return if (existingReport.endTime == null) {
                Result.Failure(IllegalStateException("Active session already exists for this date"))
            } else {
                Result.Failure(IllegalStateException("Session already completed for this date"))
            }
        }
        val session = WorkSession.create(date, System.currentTimeMillis())
        val report = session.toTimeReport()
        repository.saveReport(report)
        return Result.Success(session)
    }

    suspend fun stopSession(session: WorkSession): Result<TimeReport> {
        if (session.endTime != null) {
            return Result.Failure(IllegalStateException("Session already stopped"))
        }
        val endTime = System.currentTimeMillis()
        val report = session.toTimeReport(endTime)
        repository.saveReport(report)
        return Result.Success(report)
    }

    suspend fun pauseSession(session: WorkSession): Result<WorkSession> {
        if (session.endTime != null) {
            return Result.Failure(IllegalStateException("Cannot pause a stopped session"))
        }
        if (session.pauses.any { it.second == null }) {
            return Result.Failure(IllegalStateException("Session is already paused"))
        }
        val updatedSession = session.copy(
            pauses = session.pauses + (System.currentTimeMillis() to null)
        )
        repository.saveReport(updatedSession.toTimeReport())
        return Result.Success(updatedSession)
    }

    suspend fun resumeSession(session: WorkSession): Result<WorkSession> {
        if (session.endTime != null) {
            return Result.Failure(IllegalStateException("Cannot resume a stopped session"))
        }
        val lastPause = session.pauses.lastOrNull()
        if (lastPause == null || lastPause.second != null) {
            return Result.Failure(IllegalStateException("Session is not paused"))
        }
        val updatedPauses = session.pauses.dropLast(1) + (lastPause.first to System.currentTimeMillis())
        val updatedSession = session.copy(pauses = updatedPauses)
        repository.saveReport(updatedSession.toTimeReport())
        return Result.Success(updatedSession)
    }

    fun calculateCurrentWorkTime(session: WorkSession): Long {
        return session.calculateWorkTime()
    }

    suspend fun getReportByDate(date: LocalDate): Result<TimeReport> {
        return repository.getReportByDate(date)
    }
}

private fun WorkSession.toTimeReport(endTime: Long? = this.endTime): TimeReport {
    val calculatedWorkTime = calculateWorkTime(endTime ?: System.currentTimeMillis())
    Log.d("WorkSessionDebug", "base=${(endTime ?: System.currentTimeMillis()) - startTime}, pauseTime=${pauses.sumOf { (it.second ?: System.currentTimeMillis()) - it.first }}, result=$calculatedWorkTime")
    return TimeReport(
        date = date,
        startTime = startTime,
        endTime = endTime,
        workTime = calculatedWorkTime,
        pauses = pauses
    )
}