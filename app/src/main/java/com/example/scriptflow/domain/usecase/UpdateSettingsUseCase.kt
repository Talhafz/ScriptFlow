package com.example.scriptflow.domain.usecase

import com.example.scriptflow.domain.model.TeleprompterSettings
import com.example.scriptflow.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(settings: TeleprompterSettings) {
        repository.updateSettings(settings)
    }
}
