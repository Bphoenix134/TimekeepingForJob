package com.example.timemanagerforjob.presentation.settings

data class SettingsUiState(
    val weekdayRate: Float = 0f,
    val weekendRate: Float = 0f,
    val userEmail: String? = null
)