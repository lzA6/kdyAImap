package com.example.kdyaimap.core.data.repository

import com.example.kdyaimap.core.data.db.UserHistoryDao
import com.example.kdyaimap.core.model.UserHistory
import com.example.kdyaimap.core.data.di.IoDispatcher
import com.example.kdyaimap.domain.repository.UserHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class UserHistoryRepositoryImpl @Inject constructor(
    private val userHistoryDao: UserHistoryDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UserHistoryRepository {

    override suspend fun insertHistory(history: UserHistory) {
        userHistoryDao.insertHistory(history)
    }

    override fun getHistoryByUserId(userId: Long): Flow<List<UserHistory>> {
        return userHistoryDao.getHistoryByUserId(userId).flowOn(dispatcher)
    }
}