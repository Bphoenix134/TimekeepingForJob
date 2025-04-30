package com.example.timemanagerforjob.presentation.statistics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.timemanagerforjob.utils.formatters.TimeFormatter
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.timemanagerforjob.presentation.navigation.BottomNavigationBar


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(
    onNavigateToCalendar: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentMode = uiState.mode
    val currentDate = uiState.currentDate
    val currentWeek = uiState.currentWeek
    val currentMonth = uiState.currentMonth
    val statisticsData = uiState.statisticsData

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "statistics",
                onNavigateToCalendar = onNavigateToCalendar,
                onNavigateToStatistics = { /* Already on statistics */ }
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
            // Add "Statistics" header
            Text(
                text = "Статистика",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            // Mode selection buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModeButton(
                    text = "День",
                    isSelected = currentMode == StatisticsMode.DAY,
                    onClick = { viewModel.setMode(StatisticsMode.DAY) }
                )
                ModeButton(
                    text = "Неделя",
                    isSelected = currentMode == StatisticsMode.WEEK,
                    onClick = { viewModel.setMode(StatisticsMode.WEEK) }
                )
                ModeButton(
                    text = "Месяц",
                    isSelected = currentMode == StatisticsMode.MONTH,
                    onClick = { viewModel.setMode(StatisticsMode.MONTH) }
                )
            }

            // Navigation arrows and period label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigatePrevious() },
                    modifier = Modifier.semantics { contentDescription = "Previous Period" }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Period"
                    )
                }

                Text(
                    text = when (currentMode) {
                        StatisticsMode.DAY -> currentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                        StatisticsMode.WEEK -> "${currentWeek.first.format(DateTimeFormatter.ofPattern("dd MMM"))} - ${currentWeek.second.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}"
                        StatisticsMode.MONTH -> currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { viewModel.navigateNext() },
                    modifier = Modifier.semantics { contentDescription = "Next Period" }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Period"
                    )
                }
            }

            // Statistics content
            when (currentMode) {
                StatisticsMode.DAY -> DayStatistics(statisticsData as? DayStatisticsData)
                StatisticsMode.WEEK -> WeekStatistics(statisticsData as? WeekStatisticsData)
                StatisticsMode.MONTH -> MonthStatistics(statisticsData as? MonthStatisticsData)
            }
        }
    }
}

@Composable
private fun ModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF4CAF50) else Color.LightGray)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        color = if (isSelected) Color.White else Color.Black,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun DayStatistics(data: DayStatisticsData?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (data == null) {
            Text(
                text = "Нет данных за этот день",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            val report = data.report
            StatisticItem("Отработано", TimeFormatter.formatTime(report.workTime))
            StatisticItem("Начало работы", data.startTime)
            StatisticItem("Конец работы", data.endTime ?: "Сеанс активен")
            StatisticItem("Перерывы", "${report.pauses.size} (${TimeFormatter.formatTimeForStatistics(data.totalPauseTime)})")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeekStatistics(data: WeekStatisticsData?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (data == null || data.reports.isEmpty()) {
            Text(
                text = "Нет данных за эту неделю",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            StatisticItem("Общее время", TimeFormatter.formatTimeForStatistics(data.totalWorkTime))
            StatisticItem("Рабочих дней", "${data.reports.size}")
            StatisticItem("Среднее время в день", TimeFormatter.formatTimeForStatistics(data.averageWorkTime))
            StatisticItem("Общее время перерывов", TimeFormatter.formatTimeForStatistics(data.totalPauseTime))
            StatisticItem("Количество выходных", "${data.weekendsInWeek}")

            // Display daily breakdown
            Text(
                text = "По дням:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(data.reports.size) { index ->
                    val report = data.reports[index]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5F5F5))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = report.date.format(DateTimeFormatter.ofPattern("dd MMM")),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = TimeFormatter.formatTime(report.workTime),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthStatistics(data: MonthStatisticsData?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (data == null || data.reports.isEmpty()) {
            Text(
                text = "Нет данных за этот месяц",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            StatisticItem("Общее время", TimeFormatter.formatTimeForStatistics(data.totalWorkTime))
            StatisticItem("Рабочих дней", "${data.reports.size}")
            StatisticItem("Среднее время в день", TimeFormatter.formatTimeForStatistics(data.averageWorkTime))
            StatisticItem("Общее время перерывов", TimeFormatter.formatTimeForStatistics(data.totalPauseTime))
            StatisticItem("Самый длинный день", data.longestDay?.let {
                "${it.date.format(DateTimeFormatter.ofPattern("dd MMM"))}: ${TimeFormatter.formatTimeForStatistics(it.workTime)}"
            } ?: "Нет данных")
            StatisticItem("Самый короткий день", data.shortestDay?.let {
                "${it.date.format(DateTimeFormatter.ofPattern("dd MMM"))}: ${TimeFormatter.formatTimeForStatistics(it.workTime)}"
            } ?: "Нет данных")
            StatisticItem("Количество выходных", "${data.weekendsInMonth}")
        }
    }
}

@Composable
private fun StatisticItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 16.sp
        )
    }
}