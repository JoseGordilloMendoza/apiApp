package com.example.apiapp.data.network

import com.example.apiapp.data.network.dto.CharacterResponse
import com.example.apiapp.data.network.dto.EpisodeDto
import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RickAndMortyApiService {
    @GET("character")
    suspend fun getCharacters(
        @Query("page") page: Int
    ): CharacterResponse

    @GET("episode/{id}")
    suspend fun getEpisode(
        @Path("id") id: Int
    ): EpisodeDto

    /**
     * La API de Rick and Morty devuelve un objeto cuando se pide un solo episodio
     * y un arreglo cuando se piden varios separados por coma; por eso se recibe
     * como JsonElement crudo y se normaliza en el repositorio.
     */
    @GET("episode/{ids}")
    suspend fun getEpisodesRaw(
        @Path("ids") ids: String
    ): JsonElement
}
