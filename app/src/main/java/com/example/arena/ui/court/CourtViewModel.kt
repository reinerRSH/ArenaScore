package com.example.arena.ui.court

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.Model.Di.Data.FacilityRepository
import com.example.arena.Model.Di.Domain.Cancha
import com.example.arena.Model.Di.Domain.Sede
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
                // a) Consultar documento de la sede
                val sede = repository.getSedeById(sedeId)
                    ?: throw Exception("No se encontró la información de la sede")

                // b) Consultar colección de canchas
                val canchas = repository.getCanchasBySede(sedeId)

                // Validación de Integridad
                validateIntegrity(sede, canchas)

                _uiState.value = CourtUiState.Success(
                    CourtSelectionState(
                        selectedSede = sede,
                        canchas = canchas
                    )
                )
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
            val reservas = repository.getReservasByFecha(sedeId, fecha)
            Log.d("CourtViewModel", "Reservas cargadas para $fecha: ${reservas.size}")
            // Aquí iría la lógica para cruzar canchas con reservas
        }
    }
}
