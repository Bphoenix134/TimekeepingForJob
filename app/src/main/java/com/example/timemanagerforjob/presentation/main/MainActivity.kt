package com.example.timemanagerforjob.presentation.main

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.timemanagerforjob.auth.AuthScreen
import com.example.timemanagerforjob.auth.AuthViewModel
import com.example.timemanagerforjob.presentation.settings.SettingsScreen
import com.example.timemanagerforjob.presentation.statistics.StatisticsScreen
import com.example.timemanagerforjob.presentation.work.WorkScreenContainer
import com.example.timemanagerforjob.utils.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        requestStorageAndNotificationPermissions()
        setContent {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = hiltViewModel()
            val isAuthenticated = authViewModel.uiState.collectAsState().value.isAuthenticated

            NavHost(
                navController = navController,
                startDestination = if (isAuthenticated) Routes.Calendar else Routes.Auth
            ) {
                composable(Routes.Auth) {
                    AuthScreen(
                        onAuthenticated = {
                            navController.navigate(Routes.Calendar) {
                                popUpTo(Routes.Auth) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.Calendar) {
                    WorkScreenContainer(
                        onNavigateToStatistics = { navController.navigate(Routes.Statistics) },
                        onNavigateToSettings = { navController.navigate(Routes.Settings) }
                    )
                }
                composable(Routes.Statistics) {
                    StatisticsScreen(
                        onNavigateToCalendar = { navController.navigate(Routes.Calendar) },
                        onNavigateToSettings = { navController.navigate(Routes.Settings) }
                    )
                }
                composable(Routes.Settings) {
                    SettingsScreen(
                        onNavigateToCalendar = { navController.navigate(Routes.Calendar) },
                        onNavigateToStatistics = { navController.navigate(Routes.Statistics) }
                    )
                }
            }
        }
    }

    private fun requestStorageAndNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                100
            )
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requestPermissions(
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                100
            )
        }
    }
}

object Routes {
    const val Auth = "auth"
    const val Calendar = "calendar"
    const val Statistics = "statistics"
    const val Settings = "settings"
}