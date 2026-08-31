package com.example.scriptflow.domain.usecase

import com.example.scriptflow.domain.model.Script
import com.example.scriptflow.domain.repository.ScriptRepository
import javax.inject.Inject

class GetScriptByIdUseCase @Inject constructor(
    private val repository: ScriptRepository
) {
    suspend operator fun invoke(id: Long): Script? {
        return repository.getScriptById(id)
    }
}
