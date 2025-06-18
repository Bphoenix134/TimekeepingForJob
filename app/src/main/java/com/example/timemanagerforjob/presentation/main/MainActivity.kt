package com.example.timemanagerforjob.presentation.main

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
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
            val authState = authViewModel.uiState.collectAsState().value

            LaunchedEffect(authState.isAuthenticated) {
                if (authState.isAuthenticated) {
                    navController.navigate(Routes.CALENDAR) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                } else {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.CALENDAR) { inclusive = true }
                    }
                }
            }

            NavHost(
                navController = navController,
                startDestination = Routes.AUTH
            ) {
                composable(Routes.AUTH) {
                    AuthScreen(
                        onAuthenticated = {
                            navController.navigate(Routes.CALENDAR) {
                                popUpTo(Routes.AUTH) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.CALENDAR) {
                    WorkScreenContainer(
                        onNavigateToStatistics = { navController.navigate(Routes.STATISTICS) },
                        onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
                    )
                }
                composable(Routes.STATISTICS) {
                    StatisticsScreen(
                        onNavigateToCalendar = { navController.navigate(Routes.CALENDAR) },
                        onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onNavigateToCalendar = { navController.navigate(Routes.CALENDAR) },
                        onNavigateToStatistics = { navController.navigate(Routes.STATISTICS) },
                        onNavigateToAuth = {
                            navController.navigate(Routes.AUTH) {
                                popUpTo(Routes.SETTINGS) { inclusive = true }
                            }
                        }
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
    const val AUTH = "auth"
    const val CALENDAR = "calendar"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
}