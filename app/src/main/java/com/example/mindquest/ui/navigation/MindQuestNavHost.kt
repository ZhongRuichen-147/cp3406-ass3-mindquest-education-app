package com.example.mindquest.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mindquest.ui.activityscreen.ActivityScreen
import com.example.mindquest.ui.landing.LandingScreen
import com.example.mindquest.ui.settings.SettingsScreen
import com.example.mindquest.ui.stats.StatisticsScreen

private data class BottomNavItem(
    val matchRoute: String,
    val navigateRoute: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Destination.Landing.route, Destination.Landing.route, "Home", Icons.Filled.Home),
    BottomNavItem(Destination.Activity.route, activityRoute(), "Play", Icons.Filled.Extension),
    BottomNavItem(Destination.Statistics.route, Destination.Statistics.route, "Stats", Icons.Filled.BarChart)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindQuestApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val onSettingsScreen = currentRoute == Destination.Settings.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (onSettingsScreen) "Settings" else "MindQuest") },
                navigationIcon = {
                    if (onSettingsScreen) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (!onSettingsScreen) {
                        IconButton(onClick = { navController.navigate(Destination.Settings.route) }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!onSettingsScreen) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.matchRoute,
                            onClick = {
                                navController.navigate(item.navigateRoute) {
                                    popUpTo(Destination.Landing.route)
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Destination.Landing.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Destination.Landing.route) {
                LandingScreen(
                    onPlayQuiz = { navController.navigate(activityRoute(tab = "quiz")) },
                    onPlayMemory = { navController.navigate(activityRoute(tab = "memory")) }
                )
            }
            composable(
                route = Destination.Activity.route,
                arguments = listOf(activityTabArgument())
            ) { entry ->
                val startTab = entry.arguments?.getString(Destination.Activity.ARG_TAB)
                ActivityScreen(startTab = startTab)
            }
            composable(Destination.Statistics.route) { StatisticsScreen() }
            composable(Destination.Settings.route) { SettingsScreen() }
        }
    }
}

private fun activityTabArgument(): NamedNavArgument = navArgument(Destination.Activity.ARG_TAB) {
    type = NavType.StringType
    nullable = true
    defaultValue = null
}
