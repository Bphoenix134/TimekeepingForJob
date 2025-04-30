package com.example.timemanagerforjob.utils.preferences

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true)
    }

    fun setFirstLaunchCompleted() {
        prefs.edit { putBoolean("is_first_launch", false) }
    }

    fun isYearInitialized(year: Int): Boolean {
        return prefs.getBoolean("year_initialized_$year", false)
    }

    fun setYearInitialized(year: Int) {
        prefs.edit { putBoolean("year_initialized_$year", true) }
    }

    fun getWeekdayHourlyRate(): Float {
        return prefs.getFloat("weekday_hourly_rate", 20.0f)
    }

    fun setWeekdayHourlyRate(rate: Float) {
        prefs.edit { putFloat("weekday_hourly_rate", rate) }
    }

    fun getWeekendHourlyRate(): Float {
        return prefs.getFloat("weekend_hourly_rate", 30.0f)
    }

    fun setWeekendHourlyRate(rate: Float) {
        prefs.edit { putFloat("weekend_hourly_rate", rate) }
    }

    private inline fun SharedPreferences.edit(action: SharedPreferences.Editor.() -> Unit) {
        val editor = edit()
        action(editor)
        editor.apply()
    }
}