package com.example.arena.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.Model.Di.Data.FacilityRepository
import com.example.arena.Model.Di.Domain.Reserva
import com.example.arena.Model.Di.Domain.Sede
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val selectedSede: Sede? = null,
    val pendingPayments: List<Reserva> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val repository: FacilityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    fun loadAdminData(sedeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val sede = repository.getSedeById(sedeId)
            val pending = repository.getPagosPendientes(sedeId)
            _uiState.update { it.copy(selectedSede = sede, pendingPayments = pending, isLoading = false) }
        }
    }

    fun verifyPayment(reservaId: String, approve: Boolean) {
        viewModelScope.launch {
            val status = if (approve) Reserva.STATUS_ACTIVA else Reserva.STATUS_CANCELADA
            repository.actualizarEstadoReserva(reservaId, status)
            // Refresh
            _uiState.value.selectedSede?.id?.let { loadAdminData(it) }
        }
    }

    fun updatePrice(newPrice: Double) {
        val sedeId = _uiState.value.selectedSede?.id ?: return
        viewModelScope.launch {
            repository.actualizarConfiguracionSede(sedeId, mapOf("precioBase" to newPrice))
            loadAdminData(sedeId)
        }
    }
}