package com.example.apiapp.data.repository

import com.example.apiapp.data.local.CharacterDao
import com.example.apiapp.data.local.SyncMetaDao
import com.example.apiapp.data.local.SyncMetaEntity
import com.example.apiapp.data.local.toDomain
import com.example.apiapp.data.local.toEntity
import com.example.apiapp.data.model.Character
import com.example.apiapp.data.network.RickAndMortyApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepository @Inject constructor(
    private val api: RickAndMortyApiService,
    private val dao: CharacterDao,
    private val syncMetaDao: SyncMetaDao
) {
    val characters: Flow<List<Character>> =
        dao.observeCharacters().map { entities -> entities.map { it.toDomain() } }

    val lastSyncedAt: Flow<Long?> = syncMetaDao.observeLastSyncedAt()

    private suspend fun markSynced() {
        syncMetaDao.upsert(SyncMetaEntity(lastSyncedAt = System.currentTimeMillis()))
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
        markSynced()
        Result.success(response.info.next != null)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Re-fetches every page currently cached, to pick up upstream changes. Returns the new total row count. */
    suspend fun refreshCachedPages(): Result<Int> = try {
        val lastPage = (dao.getLastCachedPage() ?: 0).coerceAtLeast(1)
        val favoriteIds = dao.getFavoriteIds().toSet()
        for (page in 1..lastPage) {
            val response = api.getCharacters(page)
            val startOrder = (page - 1) * PAGE_SIZE
            val entities = response.results.mapIndexed { index, dto ->
                dto.toEntity(page = page, sortOrder = startOrder + index, isFavorite = dto.id in favoriteIds)
            }
            dao.upsertAll(entities)
        }
        markSynced()
        Result.success(dao.count())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun toggleFavorite(id: Int) = dao.toggleFavorite(id)

    suspend fun hasCachedData(): Boolean = dao.count() > 0

    companion object {
        const val PAGE_SIZE = 20
    }
}
