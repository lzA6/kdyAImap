package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.domain.repository.UserRepository

class LoginUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        return userRepository.login(username, password)
    }
}