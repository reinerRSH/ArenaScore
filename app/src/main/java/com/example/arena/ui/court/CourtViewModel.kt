package com.example.arena.ui.court

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.repository.FacilityRepository
import com.example.arena.domain.Cancha
import com.example.arena.domain.Sede
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourtSelectionState(
    val selectedSede: Sede? = null,
    val canchas: List<Cancha> = emptyList()
)

sealed interface CourtUiState {
    object Loading : CourtUiState
    data class Success(val state: CourtSelectionState) : CourtUiState
    data class Error(val message: String) : CourtUiState
}

@HiltViewModel
class CourtViewModel @Inject constructor(
    private val repository: FacilityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CourtUiState>(CourtUiState.Loading)
    val uiState: StateFlow<CourtUiState> = _uiState.asStateFlow()

    /**
     * Inicializa la carga de datos para una sede específica.
     */
    fun loadCourtSelection(sedeId: String) {
        viewModelScope.launch {
            _uiState.value = CourtUiState.Loading
            try {
                // 1. Consultar documento de la sede (One-time fetch is fine here)
                val sede = repository.getSedeById(sedeId)
                    ?: throw Exception("No se encontró la información de la sede")

                // 2. Observar colección de canchas en tiempo real
                repository.observeCanchasBySede(sedeId).collect { canchas ->
                    // Validación de Integridad
                    validateIntegrity(sede, canchas)

                    _uiState.value = CourtUiState.Success(
                        CourtSelectionState(
                            selectedSede = sede,
                            canchas = canchas
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = CourtUiState.Error(e.localizedMessage ?: "Error al cargar canchas")
            }
        }
    }

    private fun validateIntegrity(sede: Sede, canchas: List<Cancha>) {
        if (canchas.size != sede.totalCanchas) {
            Log.w("CourtViewModel", "ADVERTENCIA | Discrepancia detectada en sede ${sede.nombreSede}: " +
                    "Configuradas ${sede.totalCanchas} canchas, pero se encontraron ${canchas.size} documentos.")
        } else {
            Log.d("CourtViewModel", "Integridad validada: ${canchas.size} canchas encontradas.")
        }
    }

    /**
     * Prepara el terreno para filtrar por reservas.
     */
    fun loadReservas(fecha: String, sedeId: String) {
        viewModelScope.launch {
            // repository.getReservasByFecha needs to be in interface if used here
            // val reservas = repository.getReservasByFecha(sedeId, fecha)
            // Log.d("CourtViewModel", "Reservas cargadas para $fecha: ${reservas.size}")
        }
    }
}