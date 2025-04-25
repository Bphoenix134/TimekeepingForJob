package com.example.timemanagerforjob.di

import android.app.Application
import androidx.room.Room
import com.example.timemanagerforjob.data.local.database.CalendarDatabase
import com.example.timemanagerforjob.data.local.database.TimeReportDatabase
import com.example.timemanagerforjob.data.local.dao.SelectedDayDao
import com.example.timemanagerforjob.data.local.dao.TimeReportDao
import com.example.timemanagerforjob.data.repository.CalendarRepositoryImpl
import com.example.timemanagerforjob.data.repository.TimeReportRepositoryImpl
import com.example.timemanagerforjob.domain.repository.CalendarRepository
import com.example.timemanagerforjob.domain.repository.TimeReportRepository
import com.example.timemanagerforjob.domain.usecase.GetDaysOfMonthUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): CalendarDatabase {
        return Room.databaseBuilder(
            app,
            CalendarDatabase::class.java,
            "calendar_db"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideSelectedDayDao(db: CalendarDatabase): SelectedDayDao {
        return db.selectedDayDao()
    }

    @Provides
    @Singleton
    fun provideCalendarRepository(dao: SelectedDayDao): CalendarRepository {
        return CalendarRepositoryImpl(dao)
    }

    @Provides
    fun provideGetDaysOfMonthUseCase(repository: CalendarRepository): GetDaysOfMonthUseCase {
        return GetDaysOfMonthUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideTimeReportDatabase(app: Application): TimeReportDatabase {
        return Room.databaseBuilder(
            app,
            TimeReportDatabase::class.java,
            "time_report_db"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideTimeReportDao(db: TimeReportDatabase): TimeReportDao {
        return db.timeReportDao()
    }

    @Provides
    @Singleton
    fun provideTimeReportRepository(
        dao: TimeReportDao
    ): TimeReportRepository {
        return TimeReportRepositoryImpl(dao)
    }
}
