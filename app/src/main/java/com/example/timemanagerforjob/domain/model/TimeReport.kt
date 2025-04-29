package com.example.timemanagerforjob.domain.model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
data class TimeReport(
    val date: LocalDate,
    val startTime: Long,
    val endTime: Long?,
    val workTime: Long,
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

    constructor(parcel: Parcel) : this(
        date = LocalDate.of(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()
        ),
        startTime = parcel.readLong(),
        endTime = parcel.readLong().let { if (it == -1L) null else it },
        workTime = parcel.readLong(),
        pauses = mutableListOf<Pair<Long, Long?>>().apply {
            val size = parcel.readInt()
            repeat(size) {
                val first = parcel.readLong()
                val second = parcel.readLong()
                add(first to if (second == -1L) null else second)
            }
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(date.year)
        parcel.writeInt(date.monthValue)
        parcel.writeInt(date.dayOfMonth)
        parcel.writeLong(startTime)
        parcel.writeLong(endTime ?: -1L)
        parcel.writeLong(workTime)
        parcel.writeInt(pauses.size)
        pauses.forEach { pause ->
            parcel.writeLong(pause.first)
            parcel.writeLong(pause.second ?: -1L)
        }
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TimeReport> {
        override fun createFromParcel(parcel: Parcel): TimeReport {
            return TimeReport(parcel)
        }

        override fun newArray(size: Int): Array<TimeReport?> {
            return arrayOfNulls(size)
        }
    }
}