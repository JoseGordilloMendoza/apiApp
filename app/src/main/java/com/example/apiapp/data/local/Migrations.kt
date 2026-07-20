package com.example.apiapp.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE characters ADD COLUMN originName TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE characters ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS sync_meta (id INTEGER NOT NULL PRIMARY KEY, lastSyncedAt INTEGER NOT NULL)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE characters ADD COLUMN firstEpisodeId INTEGER")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE characters ADD COLUMN type TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE characters ADD COLUMN episodeIds TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE sync_meta ADD COLUMN totalCount INTEGER NOT NULL DEFAULT 0")
    }
}
