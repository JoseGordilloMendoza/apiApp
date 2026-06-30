package com.example.apiapp.ui

import com.example.apiapp.data.model.Todo

sealed interface UiState {
    data object Loading : UiState
    data class Success(val todos: List<Todo>, val hasMore: Boolean) : UiState
    data class Error(val message: String) : UiState
}
