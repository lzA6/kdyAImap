package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.domain.repository.CampusEventRepository

class GetEventByIdUseCase(
    private val campusEventRepository: CampusEventRepository
) {
    suspend operator fun invoke(eventId: Long): CampusEvent? {
        return campusEventRepository.getEventById(eventId)
    }
}