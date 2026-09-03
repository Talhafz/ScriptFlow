package com.example.scriptflow.feature.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
                    content = TextFieldValue(script.content),
                    category = script.category,
                    isFavorite = script.isFavorite,
                    createdAt = script.createdAt
                )
                updateStats(script.content)
            }
        }
    }

    fun onTitleChanged(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle, hasUnsavedChanges = true)
        scheduleSave()
    }

    fun onContentChanged(newValue: TextFieldValue) {
        val oldContent = _uiState.value.content.text
        if (oldContent != newValue.text) {
            val newUndo = (_uiState.value.undoStack + oldContent).takeLast(50)
            _uiState.value = _uiState.value.copy(
                content = newValue,
                hasUnsavedChanges = true,
                undoStack = newUndo,
                redoStack = emptyList()
            )
            updateStats(newValue.text)
            scheduleSave()
        } else {
            _uiState.value = _uiState.value.copy(content = newValue)
        }
    }

    fun onCategoryChanged(newCategory: String?) {
        _uiState.value = _uiState.value.copy(
            category = if (newCategory == "None") null else newCategory,
            hasUnsavedChanges = true
        )
        scheduleSave()
    }

    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(
            isFavorite = !_uiState.value.isFavorite,
            hasUnsavedChanges = true
        )
        scheduleSave()
    }

    fun applyFormatting(prefix: String, suffix: String = prefix) {
        val state = _uiState.value
        val selection = state.content.selection
        val text = state.content.text
        
        val newText = text.substring(0, selection.start) + 
                prefix + text.substring(selection.start, selection.end) + suffix +
                text.substring(selection.end)
        
        val newSelection = TextRange(selection.start + prefix.length, selection.end + prefix.length)
        onContentChanged(TextFieldValue(newText, newSelection))
    }

    fun insertText(inserted: String) {
        val state = _uiState.value
        val selection = state.content.selection
        val text = state.content.text
        
        val newText = text.substring(0, selection.start) + inserted + text.substring(selection.end)
        val newSelection = TextRange(selection.start + inserted.length)
        onContentChanged(TextFieldValue(newText, newSelection))
    }

    fun applyAlignment(alignment: String) {
        // Alignment is line-based
        val state = _uiState.value
        val text = state.content.text
        val selection = state.content.selection
        
        // Find start of current paragraph
        val startOfLine = text.lastIndexOf('\n', selection.start - 1).let { if (it == -1) 0 else it + 1 }
        
        val marker = "[ALIGN:$alignment]"
        // Strip existing alignment markers from this line first? 
        // For simplicity, just insert at start of line
        val newText = text.substring(0, startOfLine) + marker + "\n" + text.substring(startOfLine)
        onContentChanged(TextFieldValue(newText, TextRange(selection.start + marker.length + 1)))
    }

    fun undo() {
        val state = _uiState.value
        if (state.undoStack.isNotEmpty()) {
            val last = state.undoStack.last()
            val newUndo = state.undoStack.dropLast(1)
            val newRedo = state.redoStack + state.content.text
            _uiState.value = state.copy(
                content = TextFieldValue(last),
                undoStack = newUndo,
                redoStack = newRedo,
                hasUnsavedChanges = true
            )
            updateStats(last)
            scheduleSave()
        }
    }

    fun redo() {
        val state = _uiState.value
        if (state.redoStack.isNotEmpty()) {
            val last = state.redoStack.last()
            val newRedo = state.redoStack.dropLast(1)
            val newUndo = state.undoStack + state.content.text
            _uiState.value = state.copy(
                content = TextFieldValue(last),
                undoStack = newUndo,
                redoStack = newRedo,
                hasUnsavedChanges = true
            )
            updateStats(last)
            scheduleSave()
        }
    }

    fun clearFormatting() {
        val state = _uiState.value
        val regex = Regex("\\*\\*|\\*|__|# |\\[PAUSE\\]|---|\\[ALIGN:.*?\\]|\\(.*?\\)")
        val newText = state.content.text.replace(regex) { "" } // This is very naive, but follows the "clear all" intent
        onContentChanged(TextFieldValue(newText))
    }

    fun findReplace(find: String, replace: String) {
        val state = _uiState.value
        if (find.isEmpty()) return
        val newText = state.content.text.replace(find, replace)
        onContentChanged(TextFieldValue(newText))
    }

    private fun updateStats(content: String) {
        val stats = calculateScriptStatsUseCase(content, wpm = 150)
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

    private suspend fun saveScript(): Long {
        val state = _uiState.value
        _uiState.value = state.copy(isSaving = true)
        
        val script = Script(
            id = state.scriptId,
            title = state.title,
            content = state.content.text,
            createdAt = state.createdAt,
            updatedAt = System.currentTimeMillis(),
            category = state.category,
            isFavorite = state.isFavorite
        )
        
        val result = saveScriptUseCase(script)
        return result.getOrNull()?.let { newId ->
            _uiState.value = _uiState.value.copy(
                scriptId = newId,
                isSaving = false,
                hasUnsavedChanges = false
            )
            newId
        } ?: state.scriptId.also {
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }

    fun forceSave(onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            saveJob?.cancel()
            val id = saveScript()
            onComplete(id)
        }
    }
}
