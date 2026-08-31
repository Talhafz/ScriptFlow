package com.example.scriptflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.scriptflow.data.local.entity.ScriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptDao {
    @Query("SELECT * FROM scripts ORDER BY updatedAt DESC")
    fun getAllScripts(): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE id = :id")
    suspend fun getScriptById(id: Long): ScriptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: ScriptEntity): Long

    @Update
    suspend fun updateScript(script: ScriptEntity)

    @Delete
    suspend fun deleteScript(script: ScriptEntity)
}
