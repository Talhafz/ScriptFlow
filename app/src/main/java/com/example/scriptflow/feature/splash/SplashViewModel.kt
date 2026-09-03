package com.example.scriptflow.feature.splash

import androidx.lifecycle.ViewModel
import com.example.scriptflow.data.preferences.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    val hasSeenOnboarding = settingsDataStore.settingsFlow.map { it.hasSeenOnboarding }
}
