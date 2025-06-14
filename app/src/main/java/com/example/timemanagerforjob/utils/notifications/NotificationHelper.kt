package com.example.timemanagerforjob.utils.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.timemanagerforjob.R
import com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTime

object NotificationHelper {
    private const val CHANNEL_ID = "work_session_channel"
    private const val NOTIFICATION_ID = 1

    fun createNotificationChannel(context: Context) {
        val name = "Work Session"
        val descriptionText = "Notifications for work session tracking"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(
        context: Context,
        workTime: Long,
        isPaused: Boolean,
        stopPendingIntent: PendingIntent,
        pauseResumePendingIntent: PendingIntent
    ): NotificationCompat.Builder {
        val formattedTime = formatTime(workTime)
        val title = if (isPaused) "Рабочий сеанс приостановлен" else "Рабочий сеанс активен"
        val pauseResumeActionLabel = if (isPaused) "Возобновить" else "Пауза"

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_work_session)
            .setContentTitle(title)
            .setContentText("Время: $formattedTime")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_stop,
                "Завершить",
                stopPendingIntent
            )
            .addAction(
                R.drawable.ic_pause_resume,
                pauseResumeActionLabel,
                pauseResumePendingIntent
            )
    }

    @SuppressLint("MissingPermission")
    fun showNotification(context: Context, builder: NotificationCompat.Builder) {
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    fun cancelNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            cancel(NOTIFICATION_ID)
        }
    }
}