package com.example.scriptflow.feature.editor

data class EditorUiState(
    val scriptId: Long = 0,
    val title: String = "",
    val content: String = "",
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val estimatedDuration: String = "0:00",
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)
