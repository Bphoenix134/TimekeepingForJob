package com.example.timemanagerforjob.utils.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import java.time.YearMonth

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true)
    }

    fun setFirstLaunchCompleted() {
        prefs.edit() {
            putBoolean("is_first_launch", false)
        }
    }

    fun isYearInitialized(year: Int): Boolean {
        return prefs.getBoolean("year_initialized_$year", false)
    }

    @SuppressLint("CommitPrefEdits")
    fun setYearInitialized(year: Int) {
        prefs.edit()
            .putBoolean("year_initialized_$year", true)
    }

    fun getWeekdayHourlyRate(): Float {
        return prefs.getFloat("weekday_rate", 500f)
    }

    fun setWeekdayHourlyRate(rate: Float) {
        prefs.edit() {
            putFloat("weekday_rate", rate)
        }
    }

    fun getWeekendHourlyRate(): Float {
        return prefs.getFloat("weekend_rate", 750f)
    }

    @SuppressLint("CommitPrefEdits")
    fun setWeekendHourlyRate(rate: Float) {
        prefs.edit()
            .putFloat("weekend_rate", rate)
    }

    @SuppressLint("CommitPrefEdits")
    fun saveUserEmail(email: String?) {
        prefs.edit()
            .putString("user_email", email)
    }

    fun getUserEmail(): String? {
        return prefs.getString("user_email", null)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun setBoolean(key: String, value: Boolean) {
        prefs.edit() { putBoolean(key, value) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isMonthInitialized(yearMonth: YearMonth): Boolean {
        return getBoolean("month_${yearMonth.year}_${yearMonth.monthValue}_initialized", false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setMonthInitialized(yearMonth: YearMonth) {
        setBoolean("month_${yearMonth.year}_${yearMonth.monthValue}_initialized", true)
    }
}