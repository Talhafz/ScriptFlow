package com.example.scriptflow.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Script(
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastPosition: Int = 0,
    val category: String? = null
)
