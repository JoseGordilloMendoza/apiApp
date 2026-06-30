package com.example.apiapp.data.network

import com.example.apiapp.data.model.Todo
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("todos")
    suspend fun getTodos(
        @Query("_start") start: Int,
        @Query("_limit") limit: Int = 5
    ): List<Todo>
}
