package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.core.model.UserRole
import com.example.kdyaimap.domain.repository.UserRepository

class UpdateUserRoleUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Long, newRole: UserRole): Result<User> {
        return try {
            userRepository.updateUserRole(userId, newRole)
            Result.success(User(id = userId, username = "", passwordHash = "", role = newRole, contactInfo = byteArrayOf(), iv = byteArrayOf()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}