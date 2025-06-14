package com.example.timemanagerforjob.presentation.work

import com.example.timemanagerforjob.domain.model.TimeReport
import com.example.timemanagerforjob.domain.model.WorkSession
import java.time.YearMonth

data class WorkUiState constructor(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDays: Set<Int> = emptySet(),
    val sessionState: WorkSession? = null,
    val reportState: TimeReport? = null,
    val workedTime: Long = 0L,
    val isPaused: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)