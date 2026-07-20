package com.example.apiapp.data.repository

import com.example.apiapp.data.local.CharacterDao
import com.example.apiapp.data.local.SyncMetaDao
import com.example.apiapp.data.local.SyncMetaEntity
import com.example.apiapp.data.local.toDomain
import com.example.apiapp.data.local.toEntity
import com.example.apiapp.data.model.Character
import com.example.apiapp.data.network.RickAndMortyApiService
import com.example.apiapp.data.network.dto.EpisodeDto
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepository @Inject constructor(
    private val api: RickAndMortyApiService,
    private val dao: CharacterDao,
    private val syncMetaDao: SyncMetaDao,
    private val gson: Gson
) {
    val characters: Flow<List<Character>> =
        dao.observeCharacters().map { entities -> entities.map { it.toDomain() } }

    val lastSyncedAt: Flow<Long?> = syncMetaDao.observeLastSyncedAt()
    val totalCount: Flow<Int?> = syncMetaDao.observeTotalCount()

    private suspend fun markSynced(totalCount: Int) {
        syncMetaDao.upsert(SyncMetaEntity(lastSyncedAt = System.currentTimeMillis(), totalCount = totalCount))
    }

    /** Fetches [page] from the network and upserts it into Room. Returns whether more pages remain. */
    suspend fun refresh(page: Int): Result<Boolean> = try {
        val response = api.getCharacters(page)
        val favoriteIds = dao.getFavoriteIds().toSet()
        val startOrder = (page - 1) * PAGE_SIZE
        val entities = response.results.mapIndexed { index, dto ->
            dto.toEntity(page = page, sortOrder = startOrder + index, isFavorite = dto.id in favoriteIds)
        }
        dao.upsertAll(entities)
        markSynced(response.info.count)
        Result.success(response.info.next != null)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Re-fetches every page currently cached, to pick up upstream changes. Returns the new total row count. */
    suspend fun refreshCachedPages(): Result<Int> = try {
        val lastPage = (dao.getLastCachedPage() ?: 0).coerceAtLeast(1)
        val favoriteIds = dao.getFavoriteIds().toSet()
        var lastCount = 0
        for (page in 1..lastPage) {
            val response = api.getCharacters(page)
            lastCount = response.info.count
            val startOrder = (page - 1) * PAGE_SIZE
            val entities = response.results.mapIndexed { index, dto ->
                dto.toEntity(page = page, sortOrder = startOrder + index, isFavorite = dto.id in favoriteIds)
            }
            dao.upsertAll(entities)
        }
        markSynced(lastCount)
        Result.success(dao.count())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun toggleFavorite(id: Int) = dao.toggleFavorite(id)

    suspend fun getEpisode(episodeId: Int): Result<EpisodeDto> = try {
        Result.success(api.getEpisode(episodeId))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** La API devuelve un objeto para un solo id y un arreglo para varios; se normaliza aqui. */
    suspend fun getEpisodes(episodeIds: List<Int>): Result<List<EpisodeDto>> {
        if (episodeIds.isEmpty()) return Result.success(emptyList())
        return try {
            val raw = api.getEpisodesRaw(episodeIds.joinToString(","))
            val episodes = if (raw.isJsonArray) {
                raw.asJsonArray.map { gson.fromJson(it, EpisodeDto::class.java) }
            } else {
                listOf(gson.fromJson(raw, EpisodeDto::class.java))
            }
            Result.success(episodes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasCachedData(): Boolean = dao.count() > 0

    companion object {
        const val PAGE_SIZE = 20
    }
}
