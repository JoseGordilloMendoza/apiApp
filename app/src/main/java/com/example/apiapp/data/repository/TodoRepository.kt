package com.example.apiapp.data.repository

import com.example.apiapp.data.model.Todo
import com.example.apiapp.data.network.ApiService

class TodoRepository(private val apiService: ApiService) {
    suspend fun getTodos(start: Int, limit: Int = 5): Result<List<Todo>> {
        return try {
            val response = apiService.getTodos(start, limit)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
