package com.example.timemanagerforjob.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.timemanagerforjob.presentation.navigation.BottomNavigationBar

@Composable
fun SettingsScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel() ) {

    val uiState = viewModel.uiState.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "settings",
                onNavigateToCalendar = onNavigateToCalendar,
                onNavigateToStatistics = onNavigateToStatistics
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Настройки",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = uiState.weekdayRate.toString(),
                onValueChange = { viewModel.updateWeekdayRate(it.toFloatOrNull() ?: 0f) },
                label = { Text("Почасовая ставка в будние дни (руб.)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.weekendRate.toString(),
                onValueChange = { viewModel.updateWeekendRate(it.toFloatOrNull() ?: 0f) },
                label = { Text("Почасовая ставка в выходные дни (руб.)") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Аккаунт: ${uiState.userEmail ?: "Вошел в систему"}",
                    fontSize = 16.sp
                )
                Button(
                    onClick = { viewModel.switchAccount() },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    )
                ){
                    Text("Поменять аккаунт")
                }
            }
        }
    }

}