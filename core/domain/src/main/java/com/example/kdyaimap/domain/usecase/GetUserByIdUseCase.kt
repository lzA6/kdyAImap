package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.domain.repository.UserRepository

class GetUserByIdUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Long): User? {
        return userRepository.getUserById(userId)
    }
}