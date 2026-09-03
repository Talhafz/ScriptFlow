package com.example.scriptflow.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.example.scriptflow.feature.splash.SplashScreen
import com.example.scriptflow.feature.onboarding.OnboardingScreen
import com.example.scriptflow.feature.splash.SplashViewModel
import com.example.scriptflow.feature.home.QuickStartScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    splashViewModel: SplashViewModel = hiltViewModel()
) {
    val hasSeenOnboarding by splashViewModel.hasSeenOnboarding.collectAsState(initial = null)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    val target = if (hasSeenOnboarding == true) Screen.Home.route else Screen.Onboarding.route
                    navController.navigate(target) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
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
                },
                onNavigateToQuickStart = {
                    navController.navigate(Screen.QuickStart.route)
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
        composable(Screen.QuickStart.route) {
            QuickStartScreen(
                onNavigateToTeleprompter = { scriptId ->
                    navController.navigate(Screen.Teleprompter.createRoute(scriptId))
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
