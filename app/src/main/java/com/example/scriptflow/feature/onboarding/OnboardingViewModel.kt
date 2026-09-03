package com.example.scriptflow.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scriptflow.data.preferences.SettingsDataStore
import com.example.scriptflow.domain.model.TeleprompterSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            val currentSettings = settingsDataStore.settingsFlow.first()
            settingsDataStore.updateSettings(currentSettings.copy(hasSeenOnboarding = true))
            onComplete()
        }
    }
}
