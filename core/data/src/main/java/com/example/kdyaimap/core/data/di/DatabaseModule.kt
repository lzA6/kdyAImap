package com.example.kdyaimap.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.kdyaimap.core.data.db.AppDatabase
import com.example.kdyaimap.core.data.db.DatabaseMigrations
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
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "kdyaimap_database"
        )
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            .fallbackToDestructiveMigration()
            .build()
    }
}