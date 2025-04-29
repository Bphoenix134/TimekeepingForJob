package com.example.timemanagerforjob.domain.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import android.os.Parcel
import android.os.Parcelable

data class WorkSession(
    val date: LocalDate,
    val startTime: Long,
    val endTime: Long? = null,
    val isWeekend: Boolean,
    val pauses: List<Pair<Long, Long?>> = emptyList()
) : Parcelable {

    fun calculateWorkTime(currentTime: Long = System.currentTimeMillis()): Long {
        val baseTime = (endTime ?: currentTime) - startTime
        val pauseTime = pauses.sumOf { pause ->
            val end = pause.second ?: currentTime
            end - pause.first
        }
        return baseTime - pauseTime
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun create(date: LocalDate, startTime: Long): WorkSession {
            val isWeekend = date.dayOfWeek.value >= 6
            return WorkSession(
                date = date,
                startTime = startTime,
                isWeekend = isWeekend
            )
        }

        @JvmField
        val CREATOR = object : Parcelable.Creator<WorkSession> {
            override fun createFromParcel(parcel: Parcel): WorkSession {
                return WorkSession(parcel)
            }

            override fun newArray(size: Int): Array<WorkSession?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcel: Parcel) : this(
        date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.of(
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt()
            )
        } else {
            throw UnsupportedOperationException("LocalDate requires API 26+")
        },
        startTime = parcel.readLong(),
        endTime = parcel.readLong().let { if (it == -1L) null else it },
        isWeekend = parcel.readByte() != 0.toByte(),
        pauses = mutableListOf<Pair<Long, Long?>>().apply {
            val size = parcel.readInt()
            repeat(size) {
                val first = parcel.readLong()
                val second = parcel.readLong()
                add(first to if (second == -1L) null else second)
            }
        }
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(date.year)
        parcel.writeInt(date.monthValue)
        parcel.writeInt(date.dayOfMonth)
        parcel.writeLong(startTime)
        parcel.writeLong(endTime ?: -1L)
        parcel.writeByte(if (isWeekend) 1 else 0)
        parcel.writeInt(pauses.size)
        pauses.forEach { pause ->
            parcel.writeLong(pause.first)
            parcel.writeLong(pause.second ?: -1L)
        }
    }

    override fun describeContents(): Int = 0
}