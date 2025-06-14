package com.example.timemanagerforjob.presentation.work.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timemanagerforjob.utils.formatters.TimeFormatter.formatTime

@Composable
fun SessionControls(
    isWorking: Boolean,
    isPaused: Boolean,
    workedTime: Long,
    onStartStop: () -> Unit,
    onPauseResume: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onStartStop,
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
                onClick = onPauseResume,
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
        }
    }
}