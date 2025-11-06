package com.example.kdyaimap.domain.usecase

import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.EventStatus
import com.example.kdyaimap.core.model.EventType
import com.example.kdyaimap.domain.repository.CampusEventRepository
import kotlinx.coroutines.flow.Flow

class GetFilteredEventsUseCase(
    private val campusEventRepository: CampusEventRepository
) {
    operator fun invoke(
        eventType: EventType?,
        eventStatus: EventStatus?,
        startTime: Long?,
        endTime: Long?
    ): Flow<List<CampusEvent>> {
        return campusEventRepository.getFilteredEvents(eventType, eventStatus, startTime, endTime)
    }
}