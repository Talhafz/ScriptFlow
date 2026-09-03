package com.example.scriptflow.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scriptflow.domain.model.TeleprompterSettings
import com.example.scriptflow.domain.model.TextAlignment
import com.example.scriptflow.domain.usecase.GetSettingsUseCase
import com.example.scriptflow.domain.usecase.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getSettingsUseCase().collectLatest { settings ->
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    isLoading = false,
                    activePreset = detectPreset(settings)
                )
            }
        }
    }

    private fun detectPreset(settings: TeleprompterSettings): PresetTheme {
        return when {
            settings.textColor == 0xFFFFFFFFL && settings.backgroundColor == 0xFF000000L -> PresetTheme.CLASSIC
            settings.textColor == 0xFFB0B0B0L && settings.backgroundColor == 0xFF121212L -> PresetTheme.DARK
            settings.textColor == 0xFFFFFF00L && settings.backgroundColor == 0xFF000000L -> PresetTheme.HIGH_CONTRAST
            else -> PresetTheme.CUSTOM
        }
    }

    fun updateFontSize(size: Float) {
        updateSettings { it.copy(fontSize = size) }
    }

    fun updateScrollSpeed(speed: Float) {
        updateSettings { it.copy(scrollSpeed = speed) }
    }

    fun updateWpm(wpm: Int) {
        updateSettings { it.copy(wpm = wpm) }
    }

    fun updateTextColor(color: Long) {
        updateSettings { it.copy(textColor = color) }
    }

    fun updateBackgroundColor(color: Long) {
        updateSettings { it.copy(backgroundColor = color) }
    }

    fun updateLineSpacing(spacing: Float) {
        updateSettings { it.copy(lineSpacing = spacing) }
    }

    fun updateLetterSpacing(spacing: Float) {
        updateSettings { it.copy(letterSpacing = spacing) }
    }

    fun updateTextAlignment(alignment: TextAlignment) {
        updateSettings { it.copy(textAlignment = alignment) }
    }

    fun toggleMirrorMode(enabled: Boolean) {
        updateSettings { it.copy(mirrorMode = enabled) }
    }

    fun updateCountdownSeconds(seconds: Int) {
        updateSettings { it.copy(countdownSeconds = seconds) }
    }

    fun toggleKeepScreenAwake(enabled: Boolean) {
        updateSettings { it.copy(keepScreenAwake = enabled) }
    }

    fun updateOrientation(orientation: com.example.scriptflow.domain.model.ScreenOrientation) {
        updateSettings { it.copy(orientation = orientation) }
    }

    fun updateDisplayMode(mode: com.example.scriptflow.domain.model.DisplayMode) {
        updateSettings { it.copy(displayMode = mode) }
    }

    fun applyThemePreset(preset: PresetTheme) {
        val newSettings = when (preset) {
            PresetTheme.CLASSIC -> _uiState.value.settings.copy(
                textColor = 0xFFFFFFFFL,
                backgroundColor = 0xFF000000L
            )
            PresetTheme.DARK -> _uiState.value.settings.copy(
                textColor = 0xFFB0B0B0L,
                backgroundColor = 0xFF121212L
            )
            PresetTheme.HIGH_CONTRAST -> _uiState.value.settings.copy(
                textColor = 0xFFFFFF00L,
                backgroundColor = 0xFF000000L
            )
            PresetTheme.CUSTOM -> return
        }
        updateSettings { newSettings }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            updateSettingsUseCase(TeleprompterSettings())
        }
    }

    private fun updateSettings(transform: (TeleprompterSettings) -> TeleprompterSettings) {
        viewModelScope.launch {
            val newSettings = transform(_uiState.value.settings)
            updateSettingsUseCase(newSettings)
        }
    }
}
