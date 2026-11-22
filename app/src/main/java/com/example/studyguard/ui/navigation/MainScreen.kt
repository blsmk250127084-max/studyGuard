package com.example.studyguard.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.studyguard.ui.screens.FocusScreen
import com.example.studyguard.ui.screens.HistoryScreen
import com.example.studyguard.ui.screens.ScheduleScreen
import com.example.studyguard.ui.theme.*
import com.example.studyguard.viewmodel.StudyViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Focus : Screen("focus", "Fokus", Icons.Default.Schedule)
    object Schedule : Screen("schedule", "Jadwal", Icons.Default.Today)
    object History : Screen("history", "Riwayat", Icons.AutoMirrored.Filled.FormatListBulleted)
}

@Composable
fun MainScreen(viewModel: StudyViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.Focus, Screen.Schedule, Screen.History)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = BgSecondary,
                contentColor = TextPrimary,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Focus.route

                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 12.sp,
                                color = if (isSelected) PrimaryBlue else TextSecondary
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = PrimaryBlue.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = Screen.Focus.route) {
                composable(Screen.Focus.route) {
                    FocusScreen(viewModel = viewModel)
                }
                composable(Screen.Schedule.route) {
                    ScheduleScreen(viewModel = viewModel)
                }
                composable(Screen.History.route) {
                    HistoryScreen(viewModel = viewModel)
                }
            }
        }
    }
}
