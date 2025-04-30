package com.example.timemanagerforjob.presentation.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String)

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigateToCalendar: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val items = listOf(
        BottomNavItem("Календарь", Icons.Filled.DateRange, "calendar"),
        BottomNavItem("Статистика", Icons.Filled.Info, "statistics"),
        BottomNavItem("Настройки", Icons.Filled.Settings, "settings")
    )

    NavigationBar(
        modifier = Modifier.height(56.dp)
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    when (item.route) {
                        "calendar" -> onNavigateToCalendar()
                        "statistics" -> onNavigateToStatistics()
                        "settings" -> onNavigateToSettings()
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.semantics { contentDescription = item.label }
                    )
                }
            )
        }
    }
}