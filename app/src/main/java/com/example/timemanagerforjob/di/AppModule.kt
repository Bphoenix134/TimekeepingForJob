package com.example.timemanagerforjob.di

import com.example.timemanagerforjob.data.repository.CalendarRepositoryImpl
import com.example.timemanagerforjob.domain.repository.CalendarRepository
import com.example.timemanagerforjob.domain.usecase.GetDaysOfMonthUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideCalendarRepository(): CalendarRepository = CalendarRepositoryImpl()

    @Provides
    fun provideGetDaysOfMonthUseCase(repository: CalendarRepository): GetDaysOfMonthUseCase {
        return GetDaysOfMonthUseCase(repository)
    }
}