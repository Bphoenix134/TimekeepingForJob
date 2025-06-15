package com.example.timemanagerforjob.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
    onNavigateToAuth: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState = settingsViewModel.uiState.collectAsState().value
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
                onValueChange = { settingsViewModel.updateWeekdayRate(it.toFloatOrNull() ?: 0f) },
                label = { Text("Почасовая ставка в будние дни (руб.)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.weekendRate.toString(),
                onValueChange = { settingsViewModel.updateWeekendRate(it.toFloatOrNull() ?: 0f) },
                label = { Text("Почасовая ставка в выходные дни (руб.)") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Аккаунт: ${uiState.userEmail ?: "Не авторизован"}",
                    fontSize = 16.sp
                )
                Button(
                    onClick = {
                        settingsViewModel.switchAccount()
                        onNavigateToAuth()
                    },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Выйти")
                }
            }
        }
    }
}