package com.example.timemanagerforjob.presentation.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()
    val daysOfMonth by viewModel.daysOfMonth.collectAsState()
    val today = LocalDate.now().dayOfMonth

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
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
            items(daysOfMonth) { day ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            when {
                                day == today -> Color.Cyan
                                selectedDays.contains(day) -> Color.Green
                                else -> Color.Transparent
                            }
                        )
                        .clickable { viewModel.toggleDaySelection(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "$day", fontSize = 16.sp)
                }
            }
        }
    }
}