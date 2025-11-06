package com.example.kdyaimap.core.data.di

import com.example.kdyaimap.core.data.db.AppDatabase
import com.example.kdyaimap.core.data.repository.MapTagRepositoryImpl
import com.example.kdyaimap.domain.repository.MapTagRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapTagModule {

    @Provides
    @Singleton
    fun provideMapTagDao(database: AppDatabase) = database.mapTagDao()

    @Provides
    @Singleton
    fun provideTaggedLocationDao(database: AppDatabase) = database.taggedLocationDao()

    @Provides
    @Singleton
    fun provideTagSchemeDao(database: AppDatabase) = database.tagSchemeDao()

    @Provides
    @Singleton
    fun provideMapTagRepository(
        mapTagDao: com.example.kdyaimap.core.data.db.MapTagDao,
        taggedLocationDao: com.example.kdyaimap.core.data.db.TaggedLocationDao,
        tagSchemeDao: com.example.kdyaimap.core.data.db.TagSchemeDao,
        gson: Gson
    ): MapTagRepository {
        return MapTagRepositoryImpl(mapTagDao, taggedLocationDao, tagSchemeDao, gson)
    }
}