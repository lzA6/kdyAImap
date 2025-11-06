package com.example.kdyaimap.domain.repository

import com.example.kdyaimap.core.model.UserHistory
import kotlinx.coroutines.flow.Flow

interface UserHistoryRepository {
    suspend fun insertHistory(history: UserHistory)
    fun getHistoryByUserId(userId: Long): Flow<List<UserHistory>>
}