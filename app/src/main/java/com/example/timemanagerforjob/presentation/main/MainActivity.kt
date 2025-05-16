package com.example.timemanagerforjob.presentation.main

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
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
@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
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
        setContent {
            val navController = rememberNavController()
            AppNavigation(navController)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: androidx.navigation.NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val isAuthenticated = authViewModel.uiState.collectAsState().value.isAuthenticated

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) "calendar" else "auth"
    ) {
        composable("auth") {
            AuthScreen(
                onAuthenticated = { navController.navigate("calendar") { popUpTo("auth") { inclusive = true } } }
            )
        }
        composable("calendar") {
            WorkScreenContainer(
                onNavigateToStatistics = { navController.navigate("statistics") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("statistics") {
            StatisticsScreen(
                onNavigateToCalendar = { navController.navigate("calendar") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateToCalendar = { navController.navigate("calendar") },
                onNavigateToStatistics = { navController.navigate("statistics") }
            )
        }
    }
}