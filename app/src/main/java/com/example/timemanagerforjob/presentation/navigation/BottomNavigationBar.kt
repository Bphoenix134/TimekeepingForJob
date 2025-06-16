package com.example.timemanagerforjob.presentation.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

data class BottomNavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String, val title: String)

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigateToCalendar: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val items = listOf(
        BottomNavItem("Календарь", Icons.Filled.DateRange, "calendar", "Календарь"),
        BottomNavItem("Статистика", Icons.Filled.Info, "statistics", "Статистика"),
        BottomNavItem("Настройки", Icons.Filled.Settings, "settings", "Настройки")
    )

    NavigationBar(
        modifier = Modifier.height(80.dp)
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
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                },
                alwaysShowLabel = false
            )
        }
    }
}