package com.example.scriptflow.domain.usecase

import com.example.scriptflow.domain.model.TeleprompterSettings
import com.example.scriptflow.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<TeleprompterSettings> {
        return repository.getSettings()
    }
}
