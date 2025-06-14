package com.example.timemanagerforjob.presentation.work.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDays: Set<Int>,
    onDayClick: (Int) -> Unit,
    todayDay: Int?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek
    val shift = (firstDayOfWeek.value + 6) % 7
    val daysInMonth = currentMonth.lengthOfMonth()
    val paddedDays = List(shift) { null } + (1..daysInMonth).map { it }
    val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousMonth,
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
                onClick = onNextMonth,
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
                    color = if (index >= 5) Color.Red else Color.Black
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
                            day?.let { onDayClick(it) }
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
    }
}