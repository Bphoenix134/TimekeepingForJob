package com.example.timemanagerforjob.presentation.work

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.timemanagerforjob.util.formatTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WorkScreen(viewModel: WorkViewModel = hiltViewModel()) {
    val currentMonth by viewModel.currentYearMonth.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()
    val today: LocalDate = LocalDate.now()
    val todayDay = today.dayOfMonth
    val todayMonth = today.month
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek
    val shift = (firstDayOfWeek.value + 6) % 7
    val daysInMonth = currentMonth.lengthOfMonth()
    val paddedDays = List(shift) { null } + (1..daysInMonth).map { it }
    val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val reportState by viewModel.reportState.collectAsState()
    val workedTime by viewModel.workedTime.collectAsState()
    val isWorking = reportState?.endTime == null && reportState != null
    val isPaused by viewModel.isPaused

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.goToPreviousMonth() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            IconButton(onClick = { viewModel.goToNextMonth() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(paddedDays) { day ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(0.9.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                day == null -> Color.Transparent
                                todayMonth == currentMonth.month && day == todayDay -> Color(0xFFBEF574)
                                selectedDays.contains(day) -> Color(0xFFFD7B7C)
                                else -> Color.White
                            }
                        )
                        .border(
                            0.5.dp,
                            if (day != null) Color(0xFF969992) else Color.Transparent,
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
                            color = Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isWorking) Color(0xFFFD7B7C) else Color.White)
                    .border(
                        width = 0.5.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(50)
                    )
                    .clickable {
                        if (isWorking) {
                            viewModel.stopTimeReport()
                        } else {
                            viewModel.startTimeReport()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isWorking) "Завершить работу" else "Начать работу",
                    color = if (isWorking) Color.White else Color.Black,
                    fontSize = 19.sp,
                )
            }

            if (isWorking) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isPaused) Color(0xFF4a4a4a) else Color.Gray)
                        .border(
                            width = 0.5.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(50)
                        )
                        .clickable {
                            if (isPaused) {
                                viewModel.resumeTimeReport()
                            } else {
                                viewModel.pauseTimeReport()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPaused) "Возобновить работу" else "Приостановить",
                        color = Color.White,
                        fontSize = 19.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isWorking) {
                Text(
                    text = formatTime(workedTime),
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isPaused) Color.Red else Color.Unspecified
                )
            } else if (reportState != null) {
                val duration = reportState?.workTime ?: 0L
                Text(
                    text = "Вы отработали сегодня: ${formatTime(duration)}",
                    fontSize = 20.sp
                )
            }
        }
    }
}