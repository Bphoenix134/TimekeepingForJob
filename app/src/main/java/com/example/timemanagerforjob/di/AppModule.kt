package com.example.timemanagerforjob.di

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.room.Room
import com.example.timemanagerforjob.auth.AuthRepository
import com.example.timemanagerforjob.data.local.dao.SelectedDayDao
import com.example.timemanagerforjob.data.local.dao.TimeReportDao
import com.example.timemanagerforjob.data.local.database.AppDatabase
import com.example.timemanagerforjob.data.repository.CalendarRepositoryImpl
import com.example.timemanagerforjob.data.repository.TimeReportRepositoryImpl
import com.example.timemanagerforjob.domain.repository.CalendarRepository
import com.example.timemanagerforjob.domain.repository.TimeReportRepository
import com.example.timemanagerforjob.domain.usecases.GetMonthDataUseCase
import com.example.timemanagerforjob.domain.usecases.ManageTimeReportUseCase
import com.example.timemanagerforjob.presentation.settings.SettingsViewModel
import com.example.timemanagerforjob.presentation.statistics.StatisticsViewModel
import com.example.timemanagerforjob.presentation.work.CalendarViewModel
import com.example.timemanagerforjob.presentation.work.ReportViewModel
import com.example.timemanagerforjob.presentation.work.SessionViewModel
import com.example.timemanagerforjob.utils.preferences.AppPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "app_database"
        )
            .addMigrations(AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager {
        return CredentialManager.create(context)
    }

    @Provides
    @Singleton
    fun provideSelectedDayDao(db: AppDatabase): SelectedDayDao {
        return db.selectedDayDao()
    }

    @Provides
    @Singleton
    fun provideTimeReportDao(db: AppDatabase): TimeReportDao {
        return db.timeReportDao()
    }

    @Provides
    @Singleton
    fun provideCalendarRepository(dao: SelectedDayDao, appPreferences: AppPreferences): CalendarRepository {
        return CalendarRepositoryImpl(dao, appPreferences)
    }

    @Provides
    @Singleton
    fun provideTimeReportRepository(dao: TimeReportDao, appPreferences: AppPreferences): TimeReportRepository {
        return TimeReportRepositoryImpl(dao, appPreferences)
    }

    @Provides
    fun provideGetMonthDataUseCase(repository: CalendarRepository): GetMonthDataUseCase {
        return GetMonthDataUseCase(repository)
    }

    @Provides
    fun provideManageTimeReportUseCase(repository: TimeReportRepository): ManageTimeReportUseCase {
        return ManageTimeReportUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferences(context)
    }

    @Provides
    @Singleton
    fun provideContext(app: Application): Context {
        return app.applicationContext
    }

    @Provides
    @Singleton
    fun provideCalendarViewModel(
        calendarRepository: CalendarRepository,
        getMonthDataUseCase: GetMonthDataUseCase,
        appPreferences: AppPreferences
    ): CalendarViewModel {
        return CalendarViewModel(calendarRepository, getMonthDataUseCase, appPreferences)
    }

    @Provides
    @Singleton
    fun provideSessionViewModel(
        manageTimeReportUseCase: ManageTimeReportUseCase,
        context: Context
    ): SessionViewModel {
        return SessionViewModel(manageTimeReportUseCase, context)
    }

    @Provides
    @Singleton
    fun provideReportViewModel(
        timeReportRepository: TimeReportRepository,
        timeReportDao: TimeReportDao,
        calendarRepository: CalendarRepository,
        appPreferences: AppPreferences
    ): ReportViewModel {
        return ReportViewModel(timeReportRepository, timeReportDao, calendarRepository, appPreferences)
    }

    @Provides
    @Singleton
    fun provideStatisticsViewModel(
        timeReportRepository: TimeReportRepository,
        calendarRepository: CalendarRepository,
        appPreferences: AppPreferences
    ): StatisticsViewModel {
        return StatisticsViewModel(timeReportRepository, calendarRepository, appPreferences)
    }

    @Provides
    @Singleton
    fun provideSettingsViewModel(
        appPreferences: AppPreferences,
        authRepository: AuthRepository
    ): SettingsViewModel {
        return SettingsViewModel(appPreferences, authRepository)
    }
}