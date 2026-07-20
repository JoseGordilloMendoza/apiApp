package com.example.apiapp.data.network.dto

data class CharacterResponse(
    val info: PageInfoDto,
    val results: List<CharacterDto>
)

data class PageInfoDto(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)

data class CharacterDto(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val image: String,
    val origin: LocationDto,
    val location: LocationDto,
    val episode: List<String>
)

data class LocationDto(
    val name: String
)
