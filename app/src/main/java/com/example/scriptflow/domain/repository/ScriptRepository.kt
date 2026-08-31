package com.example.scriptflow.domain.repository

import com.example.scriptflow.domain.model.Script
import kotlinx.coroutines.flow.Flow

interface ScriptRepository {
    fun getAllScripts(): Flow<List<Script>>
    suspend fun getScriptById(id: Long): Script?
    suspend fun saveScript(script: Script): Long
    suspend fun deleteScript(script: Script)
}
