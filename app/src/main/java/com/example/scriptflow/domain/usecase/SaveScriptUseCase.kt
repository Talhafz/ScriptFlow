package com.example.scriptflow.domain.usecase

import com.example.scriptflow.domain.model.Script
import com.example.scriptflow.domain.repository.ScriptRepository
import javax.inject.Inject

class SaveScriptUseCase @Inject constructor(
    private val repository: ScriptRepository
) {
    suspend operator fun invoke(script: Script): Result<Long> {
        if (script.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Title cannot be empty"))
        }
        return try {
            val id = repository.saveScript(script)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
