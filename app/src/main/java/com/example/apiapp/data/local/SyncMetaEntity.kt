package com.example.apiapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_meta")
data class SyncMetaEntity(
    @PrimaryKey val id: Int = 0,
    val lastSyncedAt: Long
)
