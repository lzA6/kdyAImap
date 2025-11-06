package com.example.kdyaimap.di

import android.content.Context
import com.example.kdyaimap.location.AMapLocationManager
import com.example.kdyaimap.location.AMapManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideAMapLocationManager(
        @ApplicationContext context: Context
    ): AMapLocationManager {
        return AMapLocationManager(context)
    }

    @Provides
    @Singleton
    fun provideAMapManager(
        @ApplicationContext context: Context
    ): AMapManager {
        return AMapManager(context)
    }
}