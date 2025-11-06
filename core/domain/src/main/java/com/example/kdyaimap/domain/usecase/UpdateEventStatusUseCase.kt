package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.EventStatus
import com.example.kdyaimap.domain.repository.CampusEventRepository

class UpdateEventStatusUseCase(
    private val campusEventRepository: CampusEventRepository
) {
    suspend operator fun invoke(eventId: Long, newStatus: EventStatus) {
        campusEventRepository.updateEventStatus(eventId, newStatus)
    }
}