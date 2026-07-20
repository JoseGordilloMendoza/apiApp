package com.example.apiapp.ui

import com.example.apiapp.data.network.dto.EpisodeDto

sealed interface EpisodeUiState {
    data object Idle : EpisodeUiState
    data object Loading : EpisodeUiState
    data object Unavailable : EpisodeUiState
    data class Success(val episodes: List<EpisodeDto>) : EpisodeUiState
    data class Error(val message: String) : EpisodeUiState
}
