package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.domain.repository.UserRepository

class RegisterUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User) {
        userRepository.registerUser(user)
    }
}