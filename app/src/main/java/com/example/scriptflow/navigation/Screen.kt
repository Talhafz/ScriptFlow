package com.example.scriptflow.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Editor : Screen("editor/{scriptId}") {
        fun createRoute(scriptId: Long) = "editor/$scriptId"
    }
    object Teleprompter : Screen("teleprompter/{scriptId}") {
        fun createRoute(scriptId: Long) = "teleprompter/$scriptId"
    }
    object Settings : Screen("settings")
    object QuickStart : Screen("quick_start")
}
