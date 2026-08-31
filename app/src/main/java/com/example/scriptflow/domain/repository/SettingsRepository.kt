package com.example.scriptflow.domain.repository

import com.example.scriptflow.domain.model.TeleprompterSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<TeleprompterSettings>
    suspend fun updateSettings(settings: TeleprompterSettings)
}
