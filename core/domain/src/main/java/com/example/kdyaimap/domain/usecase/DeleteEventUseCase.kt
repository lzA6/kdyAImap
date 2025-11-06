package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.domain.repository.CampusEventRepository

class DeleteEventUseCase(
    private val campusEventRepository: CampusEventRepository
) {
    suspend operator fun invoke(eventId: Long) {
        campusEventRepository.deleteEventById(eventId)
    }
}