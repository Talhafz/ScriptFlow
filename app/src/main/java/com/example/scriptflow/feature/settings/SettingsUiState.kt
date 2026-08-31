package com.example.scriptflow.feature.settings

import com.example.scriptflow.domain.model.TeleprompterSettings

enum class PresetTheme {
    CLASSIC, DARK, HIGH_CONTRAST, CUSTOM
}

data class SettingsUiState(
    val settings: TeleprompterSettings = TeleprompterSettings(),
    val isLoading: Boolean = true,
    val activePreset: PresetTheme = PresetTheme.CLASSIC
)
