package com.example.timemanagerforjob.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.timemanagerforjob.R
import com.example.timemanagerforjob.domain.model.Result
import com.example.timemanagerforjob.domain.model.TimeReport
import com.example.timemanagerforjob.domain.model.WorkSession
import com.example.timemanagerforjob.domain.usecases.ManageTimeReportUseCase
import com.example.timemanagerforjob.services.WorkSessionService
import com.example.timemanagerforjob.utils.formatters.TimeFormatter
import com.example.timemanagerforjob.utils.events.SessionEvent
import com.example.timemanagerforjob.utils.events.SessionEventBus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class WorkSessionWidget : AppWidgetProvider() {

    @Inject
    lateinit var manageTimeReportUseCase: ManageTimeReportUseCase

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var eventJob: Job? = null

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("WorkSessionWidget", "onUpdate called with ${appWidgetIds.size} IDs")
        for (appWidgetId in appWidgetIds) {
            try {
                // Синхронное начальное обновление
                val views = RemoteViews(context.packageName, R.layout.work_session_widget)
                views.setTextViewText(R.id.widget_session_status, "Сеанс не активен")
                views.setTextViewText(R.id.widget_worked_time, "00:00:00")
                views.setViewVisibility(R.id.widget_session_status, View.GONE)
                views.setViewVisibility(R.id.widget_worked_time, View.GONE)
                views.setViewVisibility(R.id.widget_pause_resume_button, View.GONE)
                views.setViewVisibility(R.id.widget_total_time, View.GONE)
                views.setTextViewText(R.id.widget_start_stop_button, "Начать работу")
                views.setInt(R.id.widget_start_stop_button, "setBackgroundTint", android.graphics.Color.parseColor("#4CAF50"))

                // Настройка PendingIntent
                val startStopIntent = Intent(context, WorkSessionWidget::class.java).apply {
                    action = WorkSessionService.ACTION_START
                }
                val startStopPendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    startStopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_start_stop_button, startStopPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
                Log.d("WorkSessionWidget", "Initial update completed for ID $appWidgetId")

                // Асинхронное обновление с данными
                updateAppWidget(context, appWidgetManager, appWidgetId)
            } catch (e: Exception) {
                Log.e("WorkSessionWidget", "Error in onUpdate for ID $appWidgetId: ${e.message}", e)
                val views = RemoteViews(context.packageName, R.layout.work_session_widget)
                views.setTextViewText(R.id.widget_session_status, "Ошибка загрузки")
                views.setViewVisibility(R.id.widget_session_status, View.VISIBLE)
                views.setViewVisibility(R.id.widget_worked_time, View.GONE)
                views.setViewVisibility(R.id.widget_start_stop_button, View.GONE)
                views.setViewVisibility(R.id.widget_pause_resume_button, View.GONE)
                views.setViewVisibility(R.id.widget_total_time, View.GONE)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d("WorkSessionWidget", "onEnabled called")
        eventJob = scope.launch {
            try {
                SessionEventBus.sessionEvents.collect { event ->
                    Log.d("WorkSessionWidget", "Received SessionEvent: $event")
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(
                        ComponentName(context, WorkSessionWidget::class.java)
                    )
                    for (appWidgetId in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, appWidgetId)
                    }
                }
            } catch (e: Exception) {
                Log.e("WorkSessionWidget", "Error in SessionEventBus collect: ${e.message}", e)
            }
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d("WorkSessionWidget", "onDisabled called")
        eventJob?.cancel()
        scope.cancel()
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d("WorkSessionWidget", "onReceive called with action: ${intent.action}")
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, WorkSessionWidget::class.java)
        )

        when (intent.action) {
            WorkSessionService.ACTION_START -> {
                scope.launch(Dispatchers.IO) {
                    try {
                        val currentReport = manageTimeReportUseCase.getReportByDate(LocalDate.now()).getOrNull()
                        Log.d("WorkSessionWidget", "Current report: $currentReport")
                        if (currentReport?.endTime == null) {
                            Log.d("WorkSessionWidget", "Stopping active session")
                            WorkSessionService.stopService(context)
                        } else {
                            Log.d("WorkSessionWidget", "Starting new session")
                            val result = manageTimeReportUseCase.startSession(LocalDate.now())
                            if (result is Result.Success) {
                                WorkSessionService.startService(context, result.value)
                            } else {
                                Log.e("WorkSessionWidget", "Failed to start session: $result")
                            }
                        }
                        for (appWidgetId in appWidgetIds) {
                            updateAppWidget(context, appWidgetManager, appWidgetId)
                        }
                    } catch (e: Exception) {
                        Log.e("WorkSessionWidget", "Error in ACTION_START: ${e.message}", e)
                    }
                }
            }
            WorkSessionService.ACTION_PAUSE_RESUME -> {
                scope.launch(Dispatchers.IO) {
                    try {
                        val currentReport = manageTimeReportUseCase.getReportByDate(LocalDate.now()).getOrNull()
                        if (currentReport != null && currentReport.endTime == null) {
                            val session = WorkSession(
                                date = currentReport.date,
                                startTime = currentReport.startTime,
                                endTime = currentReport.endTime,
                                isWeekend = currentReport.date.dayOfWeek.value >= 6,
                                pauses = currentReport.pauses
                            )
                            val isPaused = session.pauses.any { it.second == null }
                            val result = if (isPaused) {
                                Log.d("WorkSessionWidget", "Resuming session")
                                manageTimeReportUseCase.resumeSession(session)
                            } else {
                                Log.d("WorkSessionWidget", "Pausing session")
                                manageTimeReportUseCase.pauseSession(session)
                            }
                            if (result is Result.Success) {
                                val serviceIntent = Intent(context, WorkSessionService::class.java).apply {
                                    action = WorkSessionService.ACTION_PAUSE_RESUME
                                    putExtra("isPaused", isPaused)
                                }
                                ContextCompat.startForegroundService(context, serviceIntent)
                            } else {
                                Log.e("WorkSessionWidget", "Failed to pause/resume: ${result}")
                            }
                        }
                        for (appWidgetId in appWidgetIds) {
                            updateAppWidget(context, appWidgetManager, appWidgetId)
                        }
                    } catch (e: Exception) {
                        Log.e("WorkSessionWidget", "Error in ACTION_PAUSE_RESUME: ${e.message}", e)
                    }
                }
            }
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Log.d("WorkSessionWidget", "Updating widget ID: $appWidgetId")
        val views = RemoteViews(context.packageName, R.layout.work_session_widget)

        // Настройка PendingIntent
        val startStopIntent = Intent(context, WorkSessionWidget::class.java).apply {
            action = WorkSessionService.ACTION_START
        }
        val startStopPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            startStopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_start_stop_button, startStopPendingIntent)

        val pauseResumeIntent = Intent(context, WorkSessionWidget::class.java).apply {
            action = WorkSessionService.ACTION_PAUSE_RESUME
        }
        val pauseResumePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            pauseResumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_pause_resume_button, pauseResumePendingIntent)

        // Асинхронное обновление с данными
        scope.launch(Dispatchers.IO) {
            try {
                val result = manageTimeReportUseCase.getReportByDate(LocalDate.now())
                when (result) {
                    is Result.Success -> {
                        val report = result.value
                        val isActive = report?.endTime == null
                        val isPaused = report?.pauses?.any { it.second == null } == true

                        if (isActive && report != null) {
                            // Сессия активна
                            views.setTextViewText(
                                R.id.widget_session_status,
                                if (isPaused) "Сеанс приостановлен" else "Сеанс активен"
                            )
                            views.setTextViewText(
                                R.id.widget_worked_time,
                                TimeFormatter.formatTime(report.workTime)
                            )
                            views.setViewVisibility(R.id.widget_session_status, View.VISIBLE)
                            views.setViewVisibility(R.id.widget_worked_time, View.VISIBLE)
                            views.setViewVisibility(R.id.widget_pause_resume_button, View.VISIBLE)
                            views.setViewVisibility(R.id.widget_total_time, View.GONE)
                            views.setTextViewText(R.id.widget_start_stop_button, "Завершить")
                            views.setInt(
                                R.id.widget_start_stop_button,
                                "setBackgroundTint",
                                android.graphics.Color.parseColor("#FD7B7C")
                            )
                            views.setTextViewText(
                                R.id.widget_pause_resume_button,
                                if (isPaused) "Возобновить" else "Пауза"
                            )
                            views.setInt(
                                R.id.widget_pause_resume_button,
                                "setBackgroundTint",
                                android.graphics.Color.parseColor("#2196F3")
                            )
                            views.setBoolean(R.id.widget_pause_resume_button, "setEnabled", true)
                        } else {
                            // Сессия неактивна
                            views.setTextViewText(R.id.widget_session_status, "Сеанс не активен")
                            views.setTextViewText(R.id.widget_worked_time, "00:00:00")
                            views.setViewVisibility(R.id.widget_session_status, View.GONE)
                            views.setViewVisibility(R.id.widget_worked_time, View.GONE)
                            views.setViewVisibility(R.id.widget_pause_resume_button, View.GONE)
                            views.setTextViewText(R.id.widget_start_stop_button, "Начать работу")
                            views.setInt(
                                R.id.widget_start_stop_button,
                                "setBackgroundTint",
                                android.graphics.Color.parseColor("#4CAF50")
                            )
                            if (report != null) {
                                // Показываем отработанное время
                                views.setTextViewText(
                                    R.id.widget_total_time,
                                    "Отработано сегодня: ${TimeFormatter.formatTime(report.workTime)}"
                                )
                                views.setViewVisibility(R.id.widget_total_time, View.VISIBLE)
                            } else {
                                views.setViewVisibility(R.id.widget_total_time, View.GONE)
                            }
                        }
                    }
                    is Result.Failure -> {
                        Log.e("WorkSessionWidget", "Failed to load report: ${result.exception.message}")
                        views.setTextViewText(R.id.widget_session_status, "Ошибка")
                        views.setViewVisibility(R.id.widget_session_status, View.VISIBLE)
                        views.setViewVisibility(R.id.widget_worked_time, View.GONE)
                        views.setViewVisibility(R.id.widget_pause_resume_button, View.GONE)
                        views.setViewVisibility(R.id.widget_total_time, View.GONE)
                        views.setTextViewText(R.id.widget_start_stop_button, "Начать работу")
                        views.setInt(
                            R.id.widget_start_stop_button,
                            "setBackgroundTint",
                            android.graphics.Color.parseColor("#4CAF50")
                        )
                    }
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
                Log.d("WorkSessionWidget", "Async update completed for ID $appWidgetId")
            } catch (e: Exception) {
                Log.e("WorkSessionWidget", "Error updating widget: ${e.message}", e)
                views.setTextViewText(R.id.widget_session_status, "Ошибка загрузки")
                views.setViewVisibility(R.id.widget_session_status, View.VISIBLE)
                views.setViewVisibility(R.id.widget_worked_time, View.GONE)
                views.setViewVisibility(R.id.widget_start_stop_button, View.GONE)
                views.setViewVisibility(R.id.widget_pause_resume_button, View.GONE)
                views.setViewVisibility(R.id.widget_total_time, View.GONE)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}