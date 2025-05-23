package com.example.timemanagerforjob.presentation.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.timemanagerforjob.auth.AuthScreen
import com.example.timemanagerforjob.presentation.settings.SettingsScreen
import com.example.timemanagerforjob.presentation.statistics.StatisticsScreen
import com.example.timemanagerforjob.presentation.work.WorkScreenContainer

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "calendar") {
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
        composable("auth") {
            Log.d("AppNavigation", "Navigating to auth screen")
            AuthScreen(
                onAuthenticated = {
                    Log.d("AppNavigation", "onAuthenticated triggered, navigating to calendar")
                    navController.navigate("calendar") { popUpTo("auth") { inclusive = true } }
                }
            )
        }
    }
}