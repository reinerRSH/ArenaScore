package com.example.arena.ui.booking

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject

data class BookingScheduleState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: String? = null,
    val sedeId: String = "",
    val canchaId: String = "",
    val tipoReserva: String = ""
)

@HiltViewModel
class BookingScheduleViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BookingScheduleState())
    val uiState: StateFlow<BookingScheduleState> = _uiState.asStateFlow()

    fun init(sedeId: String, canchaId: String, tipoReserva: String) {
        _uiState.value = _uiState.value.copy(
            sedeId = sedeId,
            canchaId = canchaId,
            tipoReserva = tipoReserva
        )
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun onTimeSelected(time: String) {
        _uiState.value = _uiState.value.copy(selectedTime = time)
    }

    fun onBookingConfirmed(onComplete: (String) -> Unit) {
        val state = _uiState.value
        val message = "Reservando ${state.tipoReserva} en ${state.canchaId} para el ${state.selectedDate} a las ${state.selectedTime}"
        onComplete(message)
    }
}
