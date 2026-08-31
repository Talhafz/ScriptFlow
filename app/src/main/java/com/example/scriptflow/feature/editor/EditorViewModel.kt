package com.example.scriptflow.feature.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scriptflow.domain.model.Script
import com.example.scriptflow.domain.usecase.CalculateScriptStatsUseCase
import com.example.scriptflow.domain.usecase.GetScriptByIdUseCase
import com.example.scriptflow.domain.usecase.SaveScriptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val getScriptByIdUseCase: GetScriptByIdUseCase,
    private val saveScriptUseCase: SaveScriptUseCase,
    private val calculateScriptStatsUseCase: CalculateScriptStatsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val scriptId: Long = savedStateHandle.get<Long>("scriptId") ?: -1L
    
    private val _uiState = MutableStateFlow(EditorUiState(scriptId = if (scriptId == -1L) 0 else scriptId))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var saveJob: Job? = null

    init {
        if (scriptId != -1L) {
            loadScript(scriptId)
        }
    }

    private fun loadScript(id: Long) {
        viewModelScope.launch {
            getScriptByIdUseCase(id)?.let { script ->
                _uiState.value = _uiState.value.copy(
                    title = script.title,
                    content = script.content
                )
                updateStats(script.content)
            }
        }
    }

    fun onTitleChanged(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle, hasUnsavedChanges = true)
        scheduleSave()
    }

    fun onContentChanged(newContent: String) {
        _uiState.value = _uiState.value.copy(content = newContent, hasUnsavedChanges = true)
        updateStats(newContent)
        scheduleSave()
    }

    private fun updateStats(content: String) {
        val stats = calculateScriptStatsUseCase(content, wpm = 150) // Default 150 WPM for now
        val minutes = stats.durationSeconds / 60
        val seconds = stats.durationSeconds % 60
        _uiState.value = _uiState.value.copy(
            wordCount = stats.wordCount,
            characterCount = stats.charCount,
            estimatedDuration = String.format("%d:%02d", minutes, seconds)
        )
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            saveScript()
        }
    }

    private suspend fun saveScript() {
        val state = _uiState.value
        _uiState.value = state.copy(isSaving = true)
        
        val script = Script(
            id = state.scriptId,
            title = state.title,
            content = state.content,
            createdAt = System.currentTimeMillis(), // Ideally passed from load
            updatedAt = System.currentTimeMillis()
        )
        
        val result = saveScriptUseCase(script)
        result.onSuccess { newId ->
            _uiState.value = _uiState.value.copy(
                scriptId = newId,
                isSaving = false,
                hasUnsavedChanges = false
            )
        }.onFailure {
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }

    fun forceSave(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            saveJob?.cancel()
            saveScript()
            onComplete()
        }
    }
}
