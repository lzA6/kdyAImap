package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class GetAllUsersUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<List<User>> {
        return userRepository.getAllUsers()
    }
}