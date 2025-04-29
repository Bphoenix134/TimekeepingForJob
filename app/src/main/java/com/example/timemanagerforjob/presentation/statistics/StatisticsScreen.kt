package com.example.timemanagerforjob.presentation.statistics

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.Button

@Composable
fun StatisticsScreen(
    onNavigateToCalendar: () -> Unit
) {
    Text(text = "Экран статистики")
    Button(onClick = onNavigateToCalendar) {
        Text("Back to Calendar")
    }
}