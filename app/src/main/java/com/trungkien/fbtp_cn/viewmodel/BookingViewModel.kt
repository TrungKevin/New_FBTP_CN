package com.trungkien.fbtp_cn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trungkien.fbtp_cn.model.Booking
import com.trungkien.fbtp_cn.model.ServiceLine
import com.trungkien.fbtp_cn.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ListenerRegistration

data class BookingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val myBookings: List<Booking> = emptyList(),
    val lastCreatedId: String? = null
)

sealed class BookingEvent {
    data class LoadMine(val renterId: String): BookingEvent()
    data class Create(
        val renterId: String,
        val ownerId: String,
        val fieldId: String,
        val date: String,
        val consecutiveSlots: List<String>,
        val bookingType: String,
        val hasOpponent: Boolean,
        val opponentId: String?,
        val opponentName: String?,
        val opponentAvatar: String?,
        val basePrice: Long,
        val serviceLines: List<ServiceLine>,
        val notes: String?
    ): BookingEvent()
    object ResetLastCreatedId: BookingEvent()
}

class BookingViewModel(
    private val repository: BookingRepository = BookingRepository()
): ViewModel() {
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState
    private var bookingsListener: ListenerRegistration? = null

    fun handle(event: BookingEvent) {
        when(event) {
            is BookingEvent.LoadMine -> loadMine(event.renterId)
            is BookingEvent.Create -> create(event)
            is BookingEvent.ResetLastCreatedId -> _uiState.value = _uiState.value.copy(lastCreatedId = null)
        }
    }

    private fun loadMine(renterId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            // Stop previous listener
            bookingsListener?.remove()
            bookingsListener = repository.listenBookingsByRenter(
                renterId = renterId,
                onChange = { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, myBookings = list)
                },
                onError = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    private fun create(e: BookingEvent.Create) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, lastCreatedId = null)
            val res = repository.createBooking(
                renterId = e.renterId,
                ownerId = e.ownerId,
                fieldId = e.fieldId,
                date = e.date,
                consecutiveSlots = e.consecutiveSlots,
                bookingType = e.bookingType,
                hasOpponent = e.hasOpponent,
                opponentId = e.opponentId,
                opponentName = e.opponentName,
                opponentAvatar = e.opponentAvatar,
                basePrice = e.basePrice,
                serviceLines = e.serviceLines,
                notes = e.notes
            )
            _uiState.value = res.fold(
                onSuccess = { id -> _uiState.value.copy(isLoading = false, lastCreatedId = id) },
                onFailure = { ex -> _uiState.value.copy(isLoading = false, error = ex.message) }
            )
        }
    }
}


