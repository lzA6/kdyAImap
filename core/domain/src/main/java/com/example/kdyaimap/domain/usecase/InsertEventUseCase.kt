package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.domain.repository.CampusEventRepository

class InsertEventUseCase(
    private val campusEventRepository: CampusEventRepository
) {
    suspend operator fun invoke(event: CampusEvent) {
        campusEventRepository.insertEvent(event)
    }
}