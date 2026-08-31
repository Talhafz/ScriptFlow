package com.example.scriptflow.domain.usecase

import com.example.scriptflow.domain.model.Script
import com.example.scriptflow.domain.repository.ScriptRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetScriptsUseCase @Inject constructor(
    private val repository: ScriptRepository
) {
    operator fun invoke(): Flow<List<Script>> {
        return repository.getAllScripts()
    }
}
