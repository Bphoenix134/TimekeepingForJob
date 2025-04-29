package com.example.timemanagerforjob.utils.preferences

import android.content.Context
import androidx.core.content.edit
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
}