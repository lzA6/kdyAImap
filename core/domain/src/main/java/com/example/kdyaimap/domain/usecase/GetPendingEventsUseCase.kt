package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.domain.repository.CampusEventRepository
import kotlinx.coroutines.flow.Flow

class GetPendingEventsUseCase(
    private val campusEventRepository: CampusEventRepository
) {
    operator fun invoke(): Flow<List<CampusEvent>> {
        return campusEventRepository.getPendingEvents()
    }
}