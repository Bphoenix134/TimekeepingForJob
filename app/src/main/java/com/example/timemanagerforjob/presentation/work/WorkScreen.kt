package com.example.timemanagerforjob.presentation.work

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(viewModel: WorkViewModel = hiltViewModel()) {
    val currentMonth by viewModel.currentYearMonth.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()
    val daysOfMonth by viewModel.daysOfMonth.collectAsState()
    val today: LocalDate = LocalDate.now()
    val todayDay = today.dayOfMonth
    val todayMonth = today.month
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek  // e.g., MONDAY
    val shift = (firstDayOfWeek.value + 6) % 7
    val daysInMonth = currentMonth.lengthOfMonth()
    val paddedDays = List(shift) { null } + (1..daysInMonth).map { it }

    val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

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
                        .padding(1.dp)
                        .background(
                            when {
                                day == null -> Color.Transparent
                                todayMonth == currentMonth.month && day == todayDay -> Color(0xFFBEF574)
                                selectedDays.contains(day) -> Color(0xFFFD7B7C)
                                else -> Color(0xFFe0e1e1)
                            }
                        )
                        .border(
                            0.5.dp,
                            if (day != null) Color(0xFF969992) else Color.Transparent,
                        )
                        .clickable(enabled = day != null) {
                            day?.let { viewModel.toggleDaySelection(it) } },
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
    }
}