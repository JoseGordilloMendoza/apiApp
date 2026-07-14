package com.example.apiapp.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncMetaDao {
    @Upsert
    suspend fun upsert(meta: SyncMetaEntity)

    @Query("SELECT lastSyncedAt FROM sync_meta WHERE id = 0")
    fun observeLastSyncedAt(): Flow<Long?>
}
