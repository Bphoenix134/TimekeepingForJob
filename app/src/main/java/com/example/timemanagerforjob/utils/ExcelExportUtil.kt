package com.example.timemanagerforjob.utils

import android.content.Context
import android.os.Environment
import com.example.timemanagerforjob.domain.model.Result
import com.example.timemanagerforjob.domain.model.TimeReport
import com.example.timemanagerforjob.utils.formatters.TimeFormatter
import jxl.Workbook
import jxl.write.WritableSheet
import jxl.write.WritableWorkbook
import jxl.write.Label
import java.io.File
import java.time.format.DateTimeFormatter

object ExcelExportUtil {

    fun exportMonthlyStatistics(
        context: Context,
        reports: List<TimeReport>,
        month: java.time.YearMonth,
        selectedDays: List<Int>
    ): Result<String> {
        return try {
            val fileName = "Work_Statistics_${month.format(DateTimeFormatter.ofPattern("MMM_yyyy"))}.xls"
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

            val workbook: WritableWorkbook = Workbook.createWorkbook(file)
            val sheet: WritableSheet = workbook.createSheet("Monthly Statistics", 0)

            // Create header row
            listOf("Date", "Work Time", "Pause Time", "Is Weekend").forEachIndexed { index, header ->
                sheet.addCell(Label(index, 0, header))
            }

            // Populate data rows
            reports.forEachIndexed { index, report ->
                val row = index + 1
                val isWeekend = selectedDays.contains(report.date.dayOfMonth)

                sheet.addCell(
                    Label(
                        0,
                        row,
                        report.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    )
                )
                sheet.addCell(
                    Label(
                        1,
                        row,
                        TimeFormatter.formatTimeForStatistics(report.workTime)
                    )
                )
                sheet.addCell(
                    Label(
                        2,
                        row,
                        TimeFormatter.formatTimeForStatistics(
                            report.pauses.sumOf { pause ->
                                val end = pause.second ?: System.currentTimeMillis()
                                end - pause.first
                            }
                        )
                    )
                )
                sheet.addCell(Label(3, row, if (isWeekend) "Yes" else "No"))
            }

            workbook.write()
            workbook.close()

            Result.Success(file.absolutePath)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}