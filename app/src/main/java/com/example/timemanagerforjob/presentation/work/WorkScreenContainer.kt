package com.example.timemanagerforjob.presentation.work

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun WorkScreenContainer(
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
    reportViewModel: ReportViewModel = hiltViewModel(),
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    WorkScreen(
        calendarViewModel = calendarViewModel,
        sessionViewModel = sessionViewModel,
        reportViewModel = reportViewModel,
        onNavigateToStatistics = onNavigateToStatistics,
        onNavigateToSettings = onNavigateToSettings
    )
}