package com.example.scriptflow.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scriptflow.domain.model.Script
import com.example.scriptflow.domain.usecase.DeleteScriptUseCase
import com.example.scriptflow.domain.usecase.GetScriptsUseCase
import com.example.scriptflow.domain.usecase.SaveScriptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getScriptsUseCase: GetScriptsUseCase,
    private val deleteScriptUseCase: DeleteScriptUseCase,
    private val saveScriptUseCase: SaveScriptUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allScripts = emptyList<Script>()

    init {
        loadScripts()
    }

    private fun loadScripts() {
        viewModelScope.launch {
            getScriptsUseCase().collectLatest { scripts ->
                allScripts = scripts
                updateState()
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(searchQuery = query)
            filterScripts(query)
        }
    }

    private fun filterScripts(query: String) {
        val filtered = if (query.isBlank()) {
            allScripts
        } else {
            allScripts.filter { it.title.contains(query, ignoreCase = true) }
        }
        
        if (filtered.isEmpty() && query.isBlank()) {
            _uiState.value = HomeUiState.Empty
        } else {
            _uiState.value = HomeUiState.Success(scripts = filtered, searchQuery = query)
        }
    }

    private fun updateState() {
        if (allScripts.isEmpty()) {
            _uiState.value = HomeUiState.Empty
        } else {
            val query = (_uiState.value as? HomeUiState.Success)?.searchQuery ?: ""
            filterScripts(query)
        }
    }

    fun deleteScript(script: Script) {
        viewModelScope.launch {
            deleteScriptUseCase(script)
        }
    }

    fun duplicateScript(script: Script) {
        viewModelScope.launch {
            val newScript = script.copy(
                id = 0,
                title = "${script.title} (Copy)",
                updatedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )
            saveScriptUseCase(newScript)
        }
    }
}
