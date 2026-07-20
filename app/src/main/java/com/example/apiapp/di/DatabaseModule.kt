package com.example.apiapp.di

import android.content.Context
import androidx.room.Room
import com.example.apiapp.data.local.AppDatabase
import com.example.apiapp.data.local.CharacterDao
import com.example.apiapp.data.local.MIGRATION_1_2
import com.example.apiapp.data.local.MIGRATION_2_3
import com.example.apiapp.data.local.MIGRATION_3_4
import com.example.apiapp.data.local.MIGRATION_4_5
import com.example.apiapp.data.local.SyncMetaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "apiapp.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()

    @Provides
    fun provideCharacterDao(database: AppDatabase): CharacterDao = database.characterDao()

    @Provides
    fun provideSyncMetaDao(database: AppDatabase): SyncMetaDao = database.syncMetaDao()
}
