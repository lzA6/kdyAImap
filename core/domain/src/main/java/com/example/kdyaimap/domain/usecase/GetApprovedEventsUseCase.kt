package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.domain.repository.CampusEventRepository
import kotlinx.coroutines.flow.Flow

class GetApprovedEventsUseCase(
    private val campusEventRepository: CampusEventRepository
) {
    operator fun invoke(): Flow<List<CampusEvent>> {
        return campusEventRepository.getAllApprovedEvents()
    }
}