package com.example.timemanagerforjob.presentation.main

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.navigation.compose.rememberNavController
import com.example.timemanagerforjob.presentation.navigation.AppNavigation
import com.example.timemanagerforjob.utils.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
        setContent {
            val navController = rememberNavController()
            AppNavigation(navController)
            // If WorkScreenContainer is used directly, provide onNavigateToStatistics
            // WorkScreenContainer(
            //     onNavigateToStatistics = { navController.navigate("statistics") }
            // )
        }
    }
}