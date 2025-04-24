package com.example.timemanagerforjob.data.local.dao

import androidx.room.*
import com.example.timemanagerforjob.data.local.entity.SelectedDayEntity

@Dao
interface SelectedDayDao {
    @Query("SELECT * FROM selected_days WHERE month = :month AND year = :year")
    suspend fun getSelectedDaysForMonth(month: Int, year: Int): List<SelectedDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(day: SelectedDayEntity)

    @Delete
    suspend fun delete(day: SelectedDayEntity)
}