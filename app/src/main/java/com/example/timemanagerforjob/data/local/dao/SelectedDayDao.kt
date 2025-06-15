package com.example.timemanagerforjob.data.local.dao

import androidx.room.*
import com.example.timemanagerforjob.data.local.entity.SelectedDayEntity

@Dao
interface SelectedDayDao {
    @Query("SELECT * FROM selected_days WHERE month = :month AND year = :year AND userEmail = :userEmail")
    suspend fun getSelectedDaysForMonth(month: Int, year: Int, userEmail: String): List<SelectedDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(day: SelectedDayEntity)

    @Delete
    suspend fun delete(day: SelectedDayEntity)

    @Query("SELECT * FROM selected_days WHERE day = :day AND month = :month AND year = :year AND userEmail = :userEmail LIMIT 1")
    suspend fun getSelectedDay(day: Int, month: Int, year: Int, userEmail: String): SelectedDayEntity?
}