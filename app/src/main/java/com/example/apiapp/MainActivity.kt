package com.example.apiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apiapp.data.network.NetworkModule
import com.example.apiapp.data.repository.TodoRepository
import com.example.apiapp.ui.TodoScreen
import com.example.apiapp.ui.TodoViewModel
import com.example.apiapp.ui.theme.ApiAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val repository = TodoRepository(NetworkModule.apiService)
        
        setContent {
            ApiAppTheme {
                val viewModel: TodoViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return TodoViewModel(repository) as T
                        }
                    }
                )
                TodoScreen(viewModel = viewModel)
            }
        }
    }
}