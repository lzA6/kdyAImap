package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.UserHistory
import com.example.kdyaimap.domain.repository.UserHistoryRepository
import kotlinx.coroutines.flow.Flow

class GetUserHistoryUseCase(
    private val userHistoryRepository: UserHistoryRepository
) {
    operator fun invoke(userId: Long): Flow<List<UserHistory>> {
        return userHistoryRepository.getHistoryByUserId(userId)
    }
}