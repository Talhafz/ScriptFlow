package com.example.scriptflow.data.repository

import com.example.scriptflow.data.local.dao.ScriptDao
import com.example.scriptflow.data.local.entity.ScriptEntity
import com.example.scriptflow.domain.model.Script
import com.example.scriptflow.domain.repository.ScriptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScriptRepositoryImpl @Inject constructor(
    private val scriptDao: ScriptDao
) : ScriptRepository {

    override fun getAllScripts(): Flow<List<Script>> {
        return scriptDao.getAllScripts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getScriptById(id: Long): Script? {
        return scriptDao.getScriptById(id)?.toDomain()
    }

    override suspend fun saveScript(script: Script): Long {
        val entity = ScriptEntity.fromDomain(script)
        return if (script.id == 0L) {
            scriptDao.insertScript(entity)
        } else {
            scriptDao.updateScript(entity)
            script.id
        }
    }

    override suspend fun deleteScript(script: Script) {
        scriptDao.deleteScript(ScriptEntity.fromDomain(script))
    }
}
