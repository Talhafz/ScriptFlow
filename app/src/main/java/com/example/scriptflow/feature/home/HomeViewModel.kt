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
            filterScripts(query, currentState.selectedCategory)
        }
    }

    fun toggleSearch() {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            val nextSearchActive = !currentState.isSearchActive
            val nextQuery = if (nextSearchActive) currentState.searchQuery else ""
            _uiState.value = currentState.copy(
                isSearchActive = nextSearchActive,
                searchQuery = nextQuery
            )
            if (!nextSearchActive) {
                filterScripts("", currentState.selectedCategory)
            }
        }
    }

    fun onCategorySelected(category: String) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(selectedCategory = category)
            filterScripts(currentState.searchQuery, category)
        }
    }

    private fun filterScripts(query: String, category: String) {
        var filtered = allScripts

        if (query.isNotBlank()) {
            filtered = filtered.filter { it.title.contains(query, ignoreCase = true) }
        }

        if (category != "All") {
            filtered = filtered.filter { it.category == category }
        }
        
        if (filtered.isEmpty() && query.isBlank() && category == "All") {
            _uiState.value = HomeUiState.Empty
        } else {
            val currentState = _uiState.value
            val isSearchActive = (currentState as? HomeUiState.Success)?.isSearchActive ?: false
            _uiState.value = HomeUiState.Success(
                scripts = filtered, 
                searchQuery = query,
                isSearchActive = isSearchActive,
                selectedCategory = category
            )
        }
    }

    private fun updateState() {
        if (allScripts.isEmpty()) {
            _uiState.value = HomeUiState.Empty
        } else {
            val currentState = _uiState.value as? HomeUiState.Success
            val query = currentState?.searchQuery ?: ""
            val category = currentState?.selectedCategory ?: "All"
            filterScripts(query, category)
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
