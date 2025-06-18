package com.example.timemanagerforjob.presentation.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.timemanagerforjob.auth.AuthEvent
import com.example.timemanagerforjob.auth.AuthScreen
import com.example.timemanagerforjob.auth.AuthViewModel
import com.example.timemanagerforjob.presentation.settings.SettingsScreen
import com.example.timemanagerforjob.presentation.statistics.StatisticsScreen
import com.example.timemanagerforjob.presentation.work.WorkScreenContainer
import com.example.timemanagerforjob.utils.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)

        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            lifecycleScope.launch {
                authViewModel.performSignIn(this@MainActivity, isSignUp = false)
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Log.w("MainActivity", "Notification permission denied")
            }
        }

        requestStorageAndNotificationPermissions()

        setContent {
            val navController = rememberNavController()
            val authState = authViewModel.uiState.collectAsState().value
            val authEvent = authViewModel.authEvent.collectAsState().value

            LaunchedEffect(authEvent) {
                when (authEvent) {
                    is AuthEvent.RequestSilentSignIn -> {
                        authViewModel.performSilentSignIn(this@MainActivity)
                    }
                    is AuthEvent.RequestSignIn -> {
                        signInLauncher.launch(Intent())
                    }
                    is AuthEvent.ShowAuthScreen -> {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(Routes.CALENDAR) { inclusive = true }
                        }
                    }
                    null -> {}
                }
            }

            if (authState.isLoading) {
                LoadingScreen()
            } else {
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

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}