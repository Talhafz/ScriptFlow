package com.example.scriptflow.feature.home

import com.example.scriptflow.domain.model.Script

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Empty : HomeUiState()
    data class Success(
        val scripts: List<Script>,
        val searchQuery: String = ""
    ) : HomeUiState()
}
