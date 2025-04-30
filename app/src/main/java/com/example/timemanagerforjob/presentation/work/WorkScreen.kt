package com.example.timemanagerforjob.presentation.work

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.timemanagerforjob.presentation.navigation.BottomNavigationBar
import com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTime
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WorkScreen(
    viewModel: WorkViewModel = hiltViewModel(),
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value
    val currentMonth = uiState.currentMonth
    val selectedDays = uiState.selectedDays
    val sessionState = uiState.sessionState
    val reportState = uiState.reportState
    val workedTime = uiState.workedTime
    val isPaused = uiState.isPaused
    val today = LocalDate.now()
    val isTodayInCurrentMonth = today.year == currentMonth.year && today.month == currentMonth.month
    val todayDay = if (isTodayInCurrentMonth) today.dayOfMonth else null
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek
    val shift = (firstDayOfWeek.value + 6) % 7
    val daysInMonth = currentMonth.lengthOfMonth()
    val paddedDays = List(shift) { null } + (1..daysInMonth).map { it }
    val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val isWorking = sessionState != null
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "calendar",
                onNavigateToCalendar = { /* Already on calendar */ },
                onNavigateToStatistics = onNavigateToStatistics,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.goToPreviousMonth() },
                    modifier = Modifier.semantics { contentDescription = "Previous Month" }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month"
                    )
                }
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { viewModel.goToNextMonth() },
                    modifier = Modifier.semantics { contentDescription = "Next Month" }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEachIndexed { index, day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = if (index >= 5) Color.Red else Color.Black // Highlight weekends
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(paddedDays.size) { index ->
                    val day = paddedDays[index]
                    val date = day?.let { currentMonth.atDay(it) }
                    val isWeekend = date?.dayOfWeek?.value?.let { it >= 6 } == true
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    day == null -> Color.Transparent
                                    day == todayDay -> Color(0xFFBEF574)
                                    selectedDays.contains(day) -> Color(0xFFFD7B7C)
                                    else -> Color.White
                                }
                            )
                            .border(
                                0.5.dp,
                                if (day != null) Color.Gray else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = day != null) {
                                day?.let { viewModel.toggleDaySelection(it) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            Text(
                                text = "$day",
                                fontSize = 16.sp,
                                color = if (selectedDays.contains(day)) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        if (isWorking) {
                            viewModel.stopTimeReport()
                        } else {
                            viewModel.startTimeReport()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isWorking) Color(0xFFFD7B7C) else Color(0xFF4CAF50)
                    )
                ) {
                    Text(
                        text = if (isWorking) "Завершить работу" else "Начать работу",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }

                if (isWorking) {
                    Button(
                        onClick = {
                            if (isPaused) {
                                viewModel.resumeTimeReport()
                            } else {
                                viewModel.pauseTimeReport()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaused) Color(0xFF2196F3) else Color.Gray
                        )
                    ) {
                        Text(
                            text = if (isPaused) "Возобновить" else "Приостановить",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }

                if (isWorking) {
                    Text(
                        text = formatTime(workedTime),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaused) Color.Red else Color.Black,
                        modifier = Modifier.semantics { contentDescription = "Current session duration" }
                    )
                } else if (reportState != null) {
                    Text(
                        text = "Отработано сегодня: ${formatTime(reportState.workTime)}",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}