package com.example.apiapp.data.model

data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val imageUrl: String,
    val locationName: String,
    val originName: String,
    val episodeCount: Int,
    val isFavorite: Boolean,
    val firstEpisodeId: Int?,
    val episodeIds: List<Int>
)
