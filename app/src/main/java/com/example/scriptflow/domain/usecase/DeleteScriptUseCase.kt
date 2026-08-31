package com.example.scriptflow.domain.usecase

import com.example.scriptflow.domain.model.Script
import com.example.scriptflow.domain.repository.ScriptRepository
import javax.inject.Inject

class DeleteScriptUseCase @Inject constructor(
    private val repository: ScriptRepository
) {
    suspend operator fun invoke(script: Script) {
        repository.deleteScript(script)
    }
}
