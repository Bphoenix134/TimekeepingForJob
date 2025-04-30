package com.example.timemanagerforjob.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.timemanagerforjob.data.local.Converters
import com.example.timemanagerforjob.data.local.dao.SelectedDayDao
import com.example.timemanagerforjob.data.local.dao.TimeReportDao
import com.example.timemanagerforjob.data.local.entity.SelectedDayEntity
import com.example.timemanagerforjob.data.local.entity.TimeReportEntity

@Database(
    entities = [SelectedDayEntity::class, TimeReportEntity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun selectedDayDao(): SelectedDayDao
    abstract fun timeReportDao(): TimeReportDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Создаём временную таблицу
                database.execSQL("""
                CREATE TABLE time_reports_new (
                    date TEXT NOT NULL,
                    startTime INTEGER NOT NULL,
                    endTime INTEGER,
                    workTime INTEGER NOT NULL,
                    pauses TEXT NOT NULL,
                    PRIMARY KEY(date)
                )
            """)
                // Копируем данные, выбирая последнюю запись для каждой даты
                database.execSQL("""
                INSERT INTO time_reports_new (date, startTime, endTime, workTime, pauses)
                SELECT date, startTime, endTime, workTime, pauses
                FROM time_reports
                GROUP BY date
                HAVING id = (SELECT MAX(id) FROM time_reports t WHERE t.date = time_reports.date)
            """)
                // Удаляем старую таблицу
                database.execSQL("DROP TABLE time_reports")
                // Переименовываем новую
                database.execSQL("ALTER TABLE time_reports_new RENAME TO time_reports")
            }
        }
    }
}