package com.example.apiapp.data.local

import com.example.apiapp.data.model.Character
import com.example.apiapp.data.network.dto.CharacterDto

fun CharacterDto.toEntity(page: Int, sortOrder: Int, isFavorite: Boolean = false): CharacterEntity {
    val episodeIdList = episode.mapNotNull { it.substringAfterLast("/").toIntOrNull() }
    return CharacterEntity(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        imageUrl = image,
        locationName = location.name,
        originName = origin.name,
        episodeCount = episode.size,
        page = page,
        sortOrder = sortOrder,
        isFavorite = isFavorite,
        firstEpisodeId = episodeIdList.firstOrNull(),
        episodeIds = episodeIdList.joinToString(",")
    )
}

fun CharacterEntity.toDomain(): Character = Character(
    id = id,
    name = name,
    status = status,
    species = species,
    type = type,
    gender = gender,
    imageUrl = imageUrl,
    locationName = locationName,
    originName = originName,
    episodeCount = episodeCount,
    isFavorite = isFavorite,
    firstEpisodeId = firstEpisodeId,
    episodeIds = episodeIds.split(",").mapNotNull { it.toIntOrNull() }
)
