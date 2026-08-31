package com.example.scriptflow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.scriptflow.feature.home.HomeScreen
import com.example.scriptflow.feature.editor.EditorScreen
import com.example.scriptflow.feature.teleprompter.TeleprompterScreen
import com.example.scriptflow.feature.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToEditor = { scriptId ->
                    navController.navigate(Screen.Editor.createRoute(scriptId))
                },
                onNavigateToTeleprompter = { scriptId ->
                    navController.navigate(Screen.Teleprompter.createRoute(scriptId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(
            route = Screen.Editor.route,
            arguments = listOf(navArgument("scriptId") { type = NavType.LongType })
        ) {
            EditorScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTeleprompter = { scriptId ->
                    navController.navigate(Screen.Teleprompter.createRoute(scriptId))
                }
            )
        }
        composable(
            route = Screen.Teleprompter.route,
            arguments = listOf(navArgument("scriptId") { type = NavType.LongType })
        ) {
            TeleprompterScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
