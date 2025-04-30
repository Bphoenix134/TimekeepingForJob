package com.example.timemanagerforjob.data.local

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return try {
            dateString?.let { LocalDate.parse(it) }
        } catch (e: Exception) {
            Log.e("Converters", "Error parsing LocalDate: ${e.message}", e)
            null
        }
    }

    @TypeConverter
    fun fromPauses(pauses: List<Pair<Long, Long?>>): String {
        return Json.encodeToString(pauses)
    }

    @TypeConverter
    fun toPauses(pausesJson: String): List<Pair<Long, Long?>> {
        return try {
            Json.decodeFromString(pausesJson)
        } catch (e: Exception) {
            Log.e("Converters", "Error parsing pauses: ${e.message}", e)
            emptyList()
        }
    }
}