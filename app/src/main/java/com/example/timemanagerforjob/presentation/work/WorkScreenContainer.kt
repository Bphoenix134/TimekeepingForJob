package com.example.timemanagerforjob.presentation.work

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WorkScreenContainer(
    viewModel: WorkViewModel = hiltViewModel(),
    onNavigateToStatistics: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value

    WorkScreen(
        viewModel = viewModel,
        onNavigateToStatistics = onNavigateToStatistics
    )
}