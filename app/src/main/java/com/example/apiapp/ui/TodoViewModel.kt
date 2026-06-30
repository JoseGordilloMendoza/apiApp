package com.example.apiapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiapp.data.repository.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var currentStart = 0
    private val limit = 5

    init {
        fetchInitialTodos()
    }

    fun fetchInitialTodos() {
        currentStart = 0
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.getTodos(start = currentStart, limit = limit)
            result.onSuccess { newTodos ->
                val hasMore = newTodos.size == limit
                _uiState.value = UiState.Success(todos = newTodos, hasMore = hasMore)
                currentStart += limit
            }.onFailure { error ->
                _uiState.value = UiState.Error(error.localizedMessage ?: "Ocurrió un error inesperado")
            }
        }
    }

    fun loadMoreTodos() {
        val currentState = _uiState.value
        if (currentState is UiState.Success && currentState.hasMore) {
            // We can show a small loading indicator if needed, but to keep it simple, we just fetch
            viewModelScope.launch {
                val result = repository.getTodos(start = currentStart, limit = limit)
                result.onSuccess { newTodos ->
                    val hasMore = newTodos.isNotEmpty() && newTodos.size == limit
                    val combinedList = currentState.todos + newTodos
                    _uiState.value = UiState.Success(todos = combinedList, hasMore = hasMore)
                    if (newTodos.isNotEmpty()) {
                        currentStart += limit
                    }
                }.onFailure {
                    // If pagination fails, we could just show a toast or keep the current state, 
                    // but for simplicity, we transition to error.
                    _uiState.value = UiState.Error("Fallo al cargar más datos")
                }
            }
        }
    }
}
