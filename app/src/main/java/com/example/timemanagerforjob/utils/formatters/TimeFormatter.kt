package com.example.timemanagerforjob.utils.formatters

import android.annotation.SuppressLint

object TimeFormatter {
    @SuppressLint("DefaultLocale")
    fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    @SuppressLint("DefaultLocale")
    fun formatTimeForStatistics(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return String.format("%d часов %d минут", hours, minutes)
    }

    @SuppressLint("DefaultLocale")
    fun formatTimeForStartAndEnd(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}