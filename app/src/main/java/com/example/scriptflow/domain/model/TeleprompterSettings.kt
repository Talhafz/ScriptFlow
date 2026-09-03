package com.example.scriptflow.domain.model

import androidx.compose.runtime.Immutable

enum class ScreenOrientation {
    AUTO, LANDSCAPE
}

enum class DisplayMode {
    VERTICAL, HORIZONTAL
}

@Immutable
data class TeleprompterSettings(
    val fontSize: Float = 24f,
    val scrollSpeed: Float = 1.0f,
    val wpm: Int = 150,
    val textColor: Long = 0xFFFFFFFF,
    val backgroundColor: Long = 0xFF000000,
    val lineSpacing: Float = 1.2f,
    val letterSpacing: Float = 0f,
    val textAlignment: TextAlignment = TextAlignment.CENTER,
    val mirrorMode: Boolean = false,
    val countdownSeconds: Int = 3,
    val keepScreenAwake: Boolean = true,
    val orientation: ScreenOrientation = ScreenOrientation.LANDSCAPE,
    val hasSeenOnboarding: Boolean = false,
    val displayMode: DisplayMode = DisplayMode.VERTICAL
)
