package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kdyaimap.core.model.CampusEvent
import com.example.kdyaimap.core.model.EventStatus
import com.example.kdyaimap.core.model.User
import com.example.kdyaimap.core.model.UserRole
import com.example.kdyaimap.domain.usecase.GetAllUsersUseCase
import com.example.kdyaimap.domain.usecase.GetPendingEventsUseCase
import com.example.kdyaimap.domain.usecase.UpdateEventStatusUseCase
import com.example.kdyaimap.domain.usecase.UpdateUserRoleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val getPendingEventsUseCase: GetPendingEventsUseCase,
    private val updateEventStatusUseCase: UpdateEventStatusUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val updateUserRoleUseCase: UpdateUserRoleUseCase
) : ViewModel() {

    private val _adminState = MutableStateFlow<AdminState>(AdminState.Loading)
    val adminState: StateFlow<AdminState> = _adminState.asStateFlow()

    init {
        loadPendingEvents()
        loadAllUsers()
    }

    private fun loadPendingEvents() {
        viewModelScope.launch {
            getPendingEventsUseCase()
                .catch { e ->
                    _adminState.value = AdminState.Error(e.message ?: "An unknown error occurred")
                }
                .collect { events ->
                    val currentState = _adminState.value
                    if (currentState is AdminState.Success) {
                        _adminState.value = currentState.copy(pendingEvents = events)
                    } else {
                        _adminState.value = AdminState.Success(pendingEvents = events, users = emptyList())
                    }
                }
        }
    }

    private fun loadAllUsers() {
        viewModelScope.launch {
            getAllUsersUseCase()
                .catch { e ->
                    _adminState.value = AdminState.Error(e.message ?: "An unknown error occurred")
                }
                .collect { users ->
                    val currentState = _adminState.value
                    if (currentState is AdminState.Success) {
                        _adminState.value = currentState.copy(users = users)
                    } else {
                        _adminState.value = AdminState.Success(pendingEvents = emptyList(), users = users)
                    }
                }
        }
    }

    fun approveEvent(eventId: Long) {
        updateStatus(eventId, EventStatus.APPROVED)
    }

    fun rejectEvent(eventId: Long) {
        updateStatus(eventId, EventStatus.REJECTED)
    }

    private fun updateStatus(eventId: Long, newStatus: EventStatus) {
        viewModelScope.launch {
            try {
                updateEventStatusUseCase(eventId, newStatus)
            } catch (e: Exception) {
                // Handle error appropriately
            }
        }
    }

    fun updateUserRole(userId: Long, newRole: UserRole) {
        viewModelScope.launch {
            try {
                updateUserRoleUseCase(userId, newRole)
            } catch (e: Exception) {
                // Handle error appropriately
            }
        }
    }
}

sealed class AdminState {
    object Loading : AdminState()
    data class Success(val pendingEvents: List<CampusEvent>, val users: List<User>) : AdminState()
    data class Error(val message: String) : AdminState()
}