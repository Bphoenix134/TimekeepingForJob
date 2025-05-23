package com.example.timemanagerforjob.di

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
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
import com.example.timemanagerforjob.presentation.work.WorkViewModel
import com.example.timemanagerforjob.utils.preferences.AppPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class) object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "app_database"
        )
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration(false)
            .build()
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
    fun provideCalendarRepository(dao: SelectedDayDao): CalendarRepository {
        return CalendarRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideTimeReportRepository(dao: TimeReportDao): TimeReportRepository {
        return TimeReportRepositoryImpl(dao)
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
    fun provideAuthRepository(@ApplicationContext context: Context): AuthRepository {
        return AuthRepository(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideWorkViewModel(
        timeReportRepository: TimeReportRepository,
        timeReportDao: TimeReportDao,
        calendarRepository: CalendarRepository,
        getMonthDataUseCase: GetMonthDataUseCase,
        manageTimeReportUseCase: ManageTimeReportUseCase,
        appPreferences: AppPreferences,
        context: Context
    ): WorkViewModel {
        return WorkViewModel(
            timeReportRepository,
            timeReportDao,
            calendarRepository,
            getMonthDataUseCase,
            manageTimeReportUseCase,
            appPreferences,
            context
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideStatisticsViewModel(
        timeReportRepository: TimeReportRepository,
        calendarRepository: CalendarRepository,
        appPreferences: AppPreferences
    ): StatisticsViewModel {
        return StatisticsViewModel(timeReportRepository, calendarRepository, appPreferences)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideSettingsViewModel(
        appPreferences: AppPreferences,
        authRepository: AuthRepository
    ): SettingsViewModel {
        return SettingsViewModel(appPreferences, authRepository)
    }

}