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
            filterScripts(currentState.searchQuery, category, currentState.sortOption)
        }
    }

    fun onSortOptionSelected(sortOption: SortOption) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(sortOption = sortOption)
            filterScripts(currentState.searchQuery, currentState.selectedCategory, sortOption)
        }
    }

    fun toggleFavorite(script: Script) {
        viewModelScope.launch {
            saveScriptUseCase(script.copy(isFavorite = !script.isFavorite))
        }
    }

    fun updateScriptCategory(script: Script, category: String?) {
        viewModelScope.launch {
            saveScriptUseCase(script.copy(category = if (category == "None") null else category))
        }
    }

    fun toggleBulkSelectionMode() {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(
                isBulkSelectionMode = !currentState.isBulkSelectionMode,
                selectedScriptIds = emptySet()
            )
        }
    }

    fun toggleScriptSelection(scriptId: Long) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            val nextSelected = if (currentState.selectedScriptIds.contains(scriptId)) {
                currentState.selectedScriptIds - scriptId
            } else {
                currentState.selectedScriptIds + scriptId
            }
            _uiState.value = currentState.copy(selectedScriptIds = nextSelected)
        }
    }

    fun deleteSelectedScripts() {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            viewModelScope.launch {
                currentState.selectedScriptIds.forEach { id ->
                    allScripts.find { it.id == id }?.let { deleteScriptUseCase(it) }
                }
                _uiState.value = currentState.copy(
                    isBulkSelectionMode = false,
                    selectedScriptIds = emptySet()
                )
            }
        }
    }

    private fun filterScripts(query: String, category: String, sortOption: SortOption = SortOption.RECENTLY_UPDATED) {
        var filtered = allScripts

        if (query.isNotBlank()) {
            filtered = filtered.filter { it.title.contains(query, ignoreCase = true) }
        }

        if (category != "All") {
            if (category == "Recent") {
                // Derived filter: last 7 days or top 10 most recent
                filtered = filtered.sortedByDescending { it.updatedAt }.take(10)
            } else {
                filtered = filtered.filter { it.category == category }
            }
        }

        filtered = when (sortOption) {
            SortOption.RECENTLY_UPDATED -> filtered.sortedByDescending { it.updatedAt }
            SortOption.TITLE_AZ -> filtered.sortedBy { it.title.lowercase() }
            SortOption.WORD_COUNT -> filtered.sortedByDescending { it.content.split(Regex("\\s+")).size }
        }
        
        val favorites = allScripts.filter { it.isFavorite }.sortedByDescending { it.updatedAt }.take(5)
        val recentScripts = allScripts.sortedByDescending { it.updatedAt }.take(5)

        if (filtered.isEmpty() && query.isBlank() && category == "All") {
            _uiState.value = HomeUiState.Empty
        } else {
            val currentState = _uiState.value as? HomeUiState.Success
            _uiState.value = HomeUiState.Success(
                scripts = filtered,
                favorites = favorites,
                recentScripts = recentScripts,
                searchQuery = query,
                isSearchActive = currentState?.isSearchActive ?: false,
                selectedCategory = category,
                sortOption = sortOption,
                isBulkSelectionMode = currentState?.isBulkSelectionMode ?: false,
                selectedScriptIds = currentState?.selectedScriptIds ?: emptySet()
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
            val sortOption = currentState?.sortOption ?: SortOption.RECENTLY_UPDATED
            filterScripts(query, category, sortOption)
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
