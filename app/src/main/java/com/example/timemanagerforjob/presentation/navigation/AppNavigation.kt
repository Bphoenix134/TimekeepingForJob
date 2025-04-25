package com.example.timemanagerforjob.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.timemanagerforjob.presentation.work.WorkScreen
import com.example.timemanagerforjob.presentation.statistics.StatisticsScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "calendar") {
        composable("calendar") {
            WorkScreen()
        }
        composable("statistics") {
            StatisticsScreen()
        }
    }
}