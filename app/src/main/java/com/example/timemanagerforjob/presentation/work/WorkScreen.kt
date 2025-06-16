package com.example.timemanagerforjob.presentation.work

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.timemanagerforjob.presentation.navigation.BottomNavigationBar
import com.example.timemanagerforjob.presentation.work.components.CalendarGrid
import com.example.timemanagerforjob.presentation.work.components.SessionControls
import com.example.timemanagerforjob.utils.ErrorHandler
import com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTime
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun WorkScreen(
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
    reportViewModel: ReportViewModel = hiltViewModel(),
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val calendarState = calendarViewModel.uiState.collectAsState().value
    val sessionState = sessionViewModel.uiState.collectAsState().value
    val reportState = reportViewModel.uiState.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        ErrorHandler.handleErrors(snackbarHostState)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "calendar",
                onNavigateToCalendar = { /* Already on calendar */ },
                onNavigateToStatistics = onNavigateToStatistics,
                onNavigateToSettings = onNavigateToSettings,
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            CalendarGrid(
                currentMonth = calendarState.currentMonth,
                selectedDays = calendarState.selectedDays,
                onDayClick = { calendarViewModel.toggleDaySelection(it) },
                todayDay = if (calendarState.currentMonth == YearMonth.from(LocalDate.now())) LocalDate.now().dayOfMonth else null,
                onPreviousMonth = { calendarViewModel.goToPreviousMonth() },
                onNextMonth = { calendarViewModel.goToNextMonth() }
            )

            if (reportState.reportState != null && sessionState.sessionState == null) {
                Text(
                    text = "Отработано сегодня: ${formatTime(reportState.reportState.workTime)}",
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            SessionControls(
                isWorking = sessionState.sessionState != null,
                isPaused = sessionState.isPaused,
                workedTime = sessionState.workedTime,
                onStartStop = {
                    if (sessionState.sessionState != null) {
                        sessionViewModel.stopTimeReport()
                    } else {
                        sessionViewModel.startTimeReport()
                    }
                },
                onPauseResume = {
                    if (sessionState.isPaused) {
                        sessionViewModel.resumeTimeReport()
                    } else {
                        sessionViewModel.pauseTimeReport()
                    }
                }
            )

        }
    }
}