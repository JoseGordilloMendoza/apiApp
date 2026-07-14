package com.example.apiapp.ui

import com.example.apiapp.data.model.Character

sealed interface UiState {
    data object Loading : UiState
    data class Success(
        val characters: List<Character>,
        val hasMore: Boolean,
        val isLoadingMore: Boolean = false
    ) : UiState
    data class Error(val message: String) : UiState
}
