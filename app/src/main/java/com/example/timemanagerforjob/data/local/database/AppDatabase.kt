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
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun selectedDayDao(): SelectedDayDao
    abstract fun timeReportDao(): TimeReportDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
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
                database.execSQL("""
                    INSERT INTO time_reports_new (date, startTime, endTime, workTime, pauses)
                    SELECT date, startTime, endTime, workTime, pauses
                    FROM time_reports
                    GROUP BY date
                    HAVING id = (SELECT MAX(id) FROM time_reports t WHERE t.date = time_reports.date)
                """)
                database.execSQL("DROP TABLE time_reports")
                database.execSQL("ALTER TABLE time_reports_new RENAME TO time_reports")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE selected_days_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        day INTEGER NOT NULL,
                        month INTEGER NOT NULL,
                        year INTEGER NOT NULL,
                        userEmail TEXT NOT NULL
                    )
                """)
                database.execSQL("""
                    INSERT INTO selected_days_new (id, day, month, year, userEmail)
                    SELECT id, day, month, year, '' AS userEmail
                    FROM selected_days
                """)
                database.execSQL("DROP TABLE selected_days")
                database.execSQL("ALTER TABLE selected_days_new RENAME TO selected_days")

                database.execSQL("""
                    CREATE TABLE time_reports_new (
                        date TEXT NOT NULL,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER,
                        workTime INTEGER NOT NULL,
                        pauses TEXT NOT NULL,
                        userEmail TEXT NOT NULL,
                        PRIMARY KEY(date)
                    )
                """)
                database.execSQL("""
                    INSERT INTO time_reports_new (date, startTime, endTime, workTime, pauses, userEmail)
                    SELECT date, startTime, endTime, workTime, pauses, '' AS userEmail
                    FROM time_reports
                """)
                database.execSQL("DROP TABLE time_reports")
                database.execSQL("ALTER TABLE time_reports_new RENAME TO time_reports")
            }
        }
    }
}