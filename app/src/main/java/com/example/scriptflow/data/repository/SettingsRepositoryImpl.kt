package com.example.scriptflow.data.repository

import com.example.scriptflow.data.preferences.SettingsDataStore
import com.example.scriptflow.domain.model.TeleprompterSettings
import com.example.scriptflow.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override fun getSettings(): Flow<TeleprompterSettings> {
        return settingsDataStore.settingsFlow
    }

    override suspend fun updateSettings(settings: TeleprompterSettings) {
        settingsDataStore.updateSettings(settings)
    }
}
