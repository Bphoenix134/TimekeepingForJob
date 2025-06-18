package com.example.timemanagerforjob.presentation.work

import java.time.YearMonth

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDays: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)