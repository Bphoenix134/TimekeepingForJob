package com.example.timemanagerforjob.presentation.statistics

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.timemanagerforjob.presentation.navigation.BottomNavigationBar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StatisticsScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    val currentMode = uiState.mode
    val currentDate = uiState.currentDate
    val currentWeek = uiState.currentWeek
    val currentMonth = uiState.currentMonth
    val statisticsData = uiState.statisticsData
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Permission handling for WRITE_EXTERNAL_STORAGE
    val storagePermissionState = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    } else {
        null
    }

    LaunchedEffect(uiState.exportResult, uiState.exportError) {
        uiState.exportResult?.let {
            snackbarHostState.showSnackbar("Экспорт успешен: $it")
        }
        uiState.exportError?.let {
            snackbarHostState.showSnackbar("Ошибка экспорта: $it")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "statistics",
                onNavigateToCalendar = onNavigateToCalendar,
                onNavigateToStatistics = { /* Already on statistics */ },
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
            // Header with export button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Статистика",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = {
                        if (storagePermissionState?.status?.isGranted == false) {
                            storagePermissionState.launchPermissionRequest()
                        } else {
                            viewModel.exportToExcel(context) // Use captured context
                        }
                    },
                    modifier = Modifier.semantics { contentDescription = "Export to Excel" }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export to Excel",
                        tint = Color.Black
                    )
                }
            }

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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Period"
                    )
                }

                Text(
                    text = when (currentMode) {
                        StatisticsMode.DAY -> currentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                        StatisticsMode.WEEK -> "${currentWeek.first.format(DateTimeFormatter.ofPattern("dd MMM"))} - ${currentWeek.second.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}"
                        StatisticsMode.MONTH -> currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                        else -> ""
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { viewModel.navigateNext() },
                    modifier = Modifier.semantics { contentDescription = "Next Period" }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
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
            StatisticItem("Отработано", com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTime(report.workTime))
            StatisticItem("Начало работы", data.startTime)
            StatisticItem("Конец работы", data.endTime ?: "Сеанс активен")
            StatisticItem("Перерывы", "${report.pauses.size} (${com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTimeForStatistics(data.totalPauseTime)})")
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
            StatisticItem("Общее время", com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTimeForStatistics(data.totalWorkTime))
            StatisticItem("Рабочих дней", "${data.reports.size}")
            StatisticItem("Среднее время в день", com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTimeForStatistics(data.averageWorkTime))
            StatisticItem("Общее время перерывов", com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTimeForStatistics(data.totalPauseTime))
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
                items(data.reports, key = { it.date.toString() }) { report ->
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
                                text = com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTime(report.workTime),
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
            StatisticItem("Общее время", com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTimeForStatistics(data.totalWorkTime))
            StatisticItem("Рабочих дней", "${data.reports.size}")
            StatisticItem("Среднее время в день", com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTimeForStatistics(data.averageWorkTime))
            StatisticItem("Общее время перерывов", com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTimeForStatistics(data.totalPauseTime))
            StatisticItem("Самый длинный день", data.longestDay?.let {
                "${it.date.format(DateTimeFormatter.ofPattern("dd MMM"))}: ${com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTimeForStatistics(it.workTime)}"
            } ?: "Нет данных")
            StatisticItem("Самый короткий день", data.shortestDay?.let {
                "${it.date.format(DateTimeFormatter.ofPattern("dd MMM"))}: ${com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTimeForStatistics(it.workTime)}"
            } ?: "Нет данных")
            StatisticItem("Количество выходных", "${data.weekendsInMonth}")
            StatisticItem("Потенциальный заработок", "${String.format("%.2f", data.totalEarnings)} рублей")
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