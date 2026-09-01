package com.example.scriptflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.scriptflow.domain.model.Script

@Entity(tableName = "scripts")
data class ScriptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastPosition: Int,
    val category: String? = null
) {
    fun toDomain() = Script(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastPosition = lastPosition,
        category = category
    )

    companion object {
        fun fromDomain(script: Script) = ScriptEntity(
            id = script.id,
            title = script.title,
            content = script.content,
            createdAt = script.createdAt,
            updatedAt = script.updatedAt,
            lastPosition = script.lastPosition,
            category = script.category
        )
    }
}
