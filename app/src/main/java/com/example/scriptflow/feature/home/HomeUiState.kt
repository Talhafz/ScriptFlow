package com.example.scriptflow.feature.home

import com.example.scriptflow.domain.model.Script

enum class SortOption {
    RECENTLY_UPDATED, TITLE_AZ, WORD_COUNT
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Empty : HomeUiState()
    data class Success(
        val scripts: List<Script>,
        val favorites: List<Script> = emptyList(),
        val recentScripts: List<Script> = emptyList(),
        val searchQuery: String = "",
        val isSearchActive: Boolean = false,
        val selectedCategory: String = "All",
        val sortOption: SortOption = SortOption.RECENTLY_UPDATED,
        val isBulkSelectionMode: Boolean = false,
        val selectedScriptIds: Set<Long> = emptySet()
    ) : HomeUiState()
}
