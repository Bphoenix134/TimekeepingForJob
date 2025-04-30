package com.example.timemanagerforjob.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.timemanagerforjob.utils.events.SessionEvent
import com.example.timemanagerforjob.utils.events.SessionEventBus
import com.example.timemanagerforjob.utils.notifications.NotificationHelper
import com.example.timemanagerforjob.domain.model.Result
import com.example.timemanagerforjob.domain.model.WorkSession
import com.example.timemanagerforjob.domain.usecases.ManageTimeReportUseCase
import com.example.timemanagerforjob.widget.WorkSessionWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WorkSessionService : Service() {

    @Inject
    lateinit var manageTimeReportUseCase: ManageTimeReportUseCase

    private var timerJob: Job? = null
    private var session: WorkSession? = null
    private var isPaused: Boolean = false

    override fun onCreate() {
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val newSession = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("session", WorkSession::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra("session")
                }
                if (newSession != null) {
                    session = newSession
                    isPaused = false
                    startForegroundService()
                    startTimer()
                }
            }
            ACTION_STOP -> {
                stopSession()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_PAUSE_RESUME -> {
                val wasPaused = intent.getBooleanExtra("isPaused", false)
                if (wasPaused) {
                    resumeSession()
                } else {
                    pauseSession()
                }
            }
        }
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification.build())
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            flow {
                while (true) {
                    emit(Unit)
                    delay(1000)
                }
            }.collect {
                session?.let { currentSession ->
                    if (!isPaused) {
                        val workTime = currentSession.calculateWorkTime()
                        updateNotification(workTime)
                        SessionEventBus.emitEvent(
                            SessionEvent.SessionUpdated(currentSession, workTime, isPaused)
                        )
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopSession() {
        session?.let { currentSession ->
            CoroutineScope(Dispatchers.IO).launch {
                val result = manageTimeReportUseCase.stopSession(currentSession)
                if (result is Result.Success) {
                    Log.d("WorkSessionService", "Session stopped: ${result.value}")
                    SessionEventBus.emitEvent(SessionEvent.SessionStopped(result.value))
                    val intent = Intent(this@WorkSessionService, WorkSessionWidget::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    }
                    sendBroadcast(intent)
                } else {
                    Log.e("WorkSessionService", "Failed to stop session: $result")
                }
            }
        }
        timerJob?.cancel()
        session = null
        isPaused = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS)
    private fun pauseSession() {
        if (session?.endTime != null) {
            Log.w("WorkSessionService", "pauseSession: session already completed — skipping")
            return
        }
        session?.let { currentSession ->
            CoroutineScope(Dispatchers.IO).launch {
                val result = manageTimeReportUseCase.pauseSession(currentSession)
                if (result is Result.Success) {
                    session = result.value
                    isPaused = true
                    updateNotification(result.value.calculateWorkTime())
                    SessionEventBus.emitEvent(SessionEvent.SessionPausedResumed(result.value, true))
                    val intent = Intent(this@WorkSessionService, WorkSessionWidget::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    }
                    sendBroadcast(intent)
                }
            }
        }
        timerJob?.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun resumeSession() {
        if (session?.endTime != null) {
            Log.w("WorkSessionService", "resumeSession: session already completed — skipping")
            return
        }
        session?.let { currentSession ->
            CoroutineScope(Dispatchers.IO).launch {
                val result = manageTimeReportUseCase.resumeSession(currentSession)
                if (result is Result.Success) {
                    session = result.value
                    isPaused = false
                    startTimer()
                    SessionEventBus.emitEvent(SessionEvent.SessionPausedResumed(result.value, false))
                    val intent = Intent(this@WorkSessionService, WorkSessionWidget::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    }
                    sendBroadcast(intent)
                }
            }
        }
    }

    private fun buildNotification(): NotificationCompat.Builder {
        val stopIntent = Intent(this, WorkSessionService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeIntent = Intent(this, WorkSessionService::class.java).apply {
            action = ACTION_PAUSE_RESUME
            putExtra("isPaused", isPaused)
        }
        val pauseResumePendingIntent = PendingIntent.getService(
            this,
            1,
            pauseResumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationHelper.buildNotification(
            this,
            session?.calculateWorkTime() ?: 0L,
            isPaused,
            stopPendingIntent,
            pauseResumePendingIntent
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun updateNotification(workTime: Long) {
        val notification = buildNotification()
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification.build())
    }

    override fun onDestroy() {
        timerJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.example.timemanagerforjob.ACTION_START"
        const val ACTION_STOP = "com.example.timemanagerforjob.ACTION_STOP"
        const val ACTION_PAUSE_RESUME = "com.example.timemanagerforjob.ACTION_PAUSE_RESUME"

        fun startService(context: Context, session: WorkSession) {
            val intent = Intent(context, WorkSessionService::class.java).apply {
                action = ACTION_START
                putExtra("session", session)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, WorkSessionService::class.java).apply {
                action = ACTION_STOP
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }
}