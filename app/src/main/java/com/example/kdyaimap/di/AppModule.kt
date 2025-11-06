package com.example.kdyaimap.di

import android.content.Context
import androidx.room.Room
import com.example.kdyaimap.core.data.db.AppDatabase
import com.example.kdyaimap.core.data.db.CampusEventDao
import com.example.kdyaimap.core.data.db.UserDao
import com.example.kdyaimap.core.data.db.UserHistoryDao
import com.example.kdyaimap.core.data.repository.CampusEventRepositoryImpl
import com.example.kdyaimap.core.data.repository.UserHistoryRepositoryImpl
import com.example.kdyaimap.core.data.repository.UserPreferencesRepository
import com.example.kdyaimap.core.data.repository.UserRepositoryImpl
import com.example.kdyaimap.domain.repository.CampusEventRepository
import com.example.kdyaimap.domain.repository.UserHistoryRepository
import com.example.kdyaimap.domain.repository.UserRepository
import com.example.kdyaimap.core.data.di.IoDispatcher
import com.example.kdyaimap.core.data.network.NetworkUserRepository
import com.example.kdyaimap.core.data.network.NetworkEventRepository
import com.example.kdyaimap.core.data.network.ApiService
import com.example.kdyaimap.util.CacheManager
import com.example.kdyaimap.util.PerformanceMonitor
import com.example.kdyaimap.domain.usecase.*
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        networkUserRepository: NetworkUserRepository,
        @IoDispatcher dispatcher: CoroutineDispatcher
    ): UserRepository {
        return UserRepositoryImpl(userDao, networkUserRepository, dispatcher)
    }

    @Provides
    @Singleton
    fun provideCampusEventRepository(
        campusEventDao: CampusEventDao,
        apiService: ApiService,
        networkEventRepository: NetworkEventRepository,
        @IoDispatcher dispatcher: CoroutineDispatcher
    ): CampusEventRepository {
        return CampusEventRepositoryImpl(campusEventDao, apiService, networkEventRepository, dispatcher)
    }

    @Provides
    @Singleton
    fun provideUserHistoryRepository(userHistoryDao: UserHistoryDao, @IoDispatcher dispatcher: CoroutineDispatcher): UserHistoryRepository {
        return UserHistoryRepositoryImpl(userHistoryDao, dispatcher)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    fun provideCampusEventDao(appDatabase: AppDatabase): CampusEventDao {
        return appDatabase.campusEventDao()
    }

    @Provides
    fun provideUserHistoryDao(appDatabase: AppDatabase): UserHistoryDao {
        return appDatabase.userHistoryDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideCacheManager(@ApplicationContext context: Context): CacheManager {
        return CacheManager(context)
    }

    @Provides
    @Singleton
    fun providePerformanceMonitor(): PerformanceMonitor {
        return PerformanceMonitor
    }

    // UseCase providers
    @Provides
    @Singleton
    fun provideGetAllUsersUseCase(userRepository: UserRepository): GetAllUsersUseCase {
        return GetAllUsersUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideGetApprovedEventsUseCase(campusEventRepository: CampusEventRepository): GetApprovedEventsUseCase {
        return GetApprovedEventsUseCase(campusEventRepository)
    }

    @Provides
    @Singleton
    fun provideGetPendingEventsUseCase(campusEventRepository: CampusEventRepository): GetPendingEventsUseCase {
        return GetPendingEventsUseCase(campusEventRepository)
    }

    @Provides
    @Singleton
    fun provideLoginUserUseCase(userRepository: UserRepository): LoginUserUseCase {
        return LoginUserUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateUserRoleUseCase(userRepository: UserRepository): UpdateUserRoleUseCase {
        return UpdateUserRoleUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideGetEventByIdUseCase(campusEventRepository: CampusEventRepository): GetEventByIdUseCase {
        return GetEventByIdUseCase(campusEventRepository)
    }

    @Provides
    @Singleton
    fun provideInsertEventUseCase(campusEventRepository: CampusEventRepository): InsertEventUseCase {
        return InsertEventUseCase(campusEventRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateEventUseCase(campusEventRepository: CampusEventRepository): UpdateEventUseCase {
        return UpdateEventUseCase(campusEventRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteEventUseCase(campusEventRepository: CampusEventRepository): DeleteEventUseCase {
        return DeleteEventUseCase(campusEventRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateEventStatusUseCase(campusEventRepository: CampusEventRepository): UpdateEventStatusUseCase {
        return UpdateEventStatusUseCase(campusEventRepository)
    }

    @Provides
    @Singleton
    fun provideGetEventsByTypeUseCase(campusEventRepository: CampusEventRepository): GetEventsByTypeUseCase {
        return GetEventsByTypeUseCase(campusEventRepository)
    }

    @Provides
    @Singleton
    fun provideGetFilteredEventsUseCase(campusEventRepository: CampusEventRepository): GetFilteredEventsUseCase {
        return GetFilteredEventsUseCase(campusEventRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserByIdUseCase(userRepository: UserRepository): GetUserByIdUseCase {
        return GetUserByIdUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserHistoryUseCase(userHistoryRepository: UserHistoryRepository): GetUserHistoryUseCase {
        return GetUserHistoryUseCase(userHistoryRepository)
    }

    @Provides
    @Singleton
    fun provideRegisterUserUseCase(userRepository: UserRepository): RegisterUserUseCase {
        return RegisterUserUseCase(userRepository)
    }
}