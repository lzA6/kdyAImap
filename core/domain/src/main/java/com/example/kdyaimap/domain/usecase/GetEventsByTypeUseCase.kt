package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.EventType
import com.example.kdyaimap.domain.repository.CampusEventRepository
import kotlinx.coroutines.flow.Flow

class GetEventsByTypeUseCase(
    private val campusEventRepository: CampusEventRepository
) {
    operator fun invoke(eventType: EventType): Flow<List<CampusEvent>> {
        return campusEventRepository.getApprovedEventsByType(eventType)
    }
}