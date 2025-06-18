package com.example.timemanagerforjob.utils.preferences

import android.annotation.SuppressLint
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import java.time.YearMonth
import android.util.Log

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true)
    }

    fun setFirstLaunchCompleted() {
        prefs.edit { putBoolean("is_first_launch", false) }
    }

    fun getWeekdayHourlyRate(): Float {
        return prefs.getFloat("weekday_rate", 500f)
    }

    fun setWeekdayHourlyRate(rate: Float) {
        prefs.edit { putFloat("weekday_rate", rate) }
    }

    fun getWeekendHourlyRate(): Float {
        return prefs.getFloat("weekend_rate", 750f)
    }

    @SuppressLint("CommitPrefEdits")
    fun setWeekendHourlyRate(rate: Float) {
        prefs.edit()
            .putFloat("weekend_rate", rate)
    }

    fun saveUserEmail(email: String?) {
        Log.d("AppPreferences", "Saving email: $email")
        prefs.edit {
            putString("user_email", email)
        }
    }

    fun getUserEmail(): String? {
        val userEmail = prefs.getString("user_email", null)
        Log.d("AppPreferences", "Retrieved email: $userEmail")
        return userEmail
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun setBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    fun isMonthInitialized(yearMonth: YearMonth): Boolean {
        return this.getBoolean("month_${yearMonth.year}_${yearMonth.monthValue}_initialized", false)
    }

    fun setMonthInitialized(yearMonth: YearMonth) {
        setBoolean("month_${yearMonth.year}_${yearMonth.monthValue}_initialized", true)
    }
}