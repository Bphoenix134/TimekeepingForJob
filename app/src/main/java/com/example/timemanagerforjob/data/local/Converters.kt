package com.example.timemanagerforjob.data.local

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import java.time.LocalDate
import kotlinx.serialization.json.Json

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
            emptyList()
        }
    }
}