package com.example.arena.ui.facility

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.repository.FacilityRepository
import com.example.arena.domain.Sede
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State para la lista de sedes.
 */
sealed interface FacilityUiState {
    object Loading : FacilityUiState
    data class Success(val facilities: List<Sede>) : FacilityUiState
    data class Error(val message: String) : FacilityUiState
}

@HiltViewModel
class FacilityViewModel @Inject constructor(
    private val facilityRepository: FacilityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FacilityUiState>(FacilityUiState.Loading)
    val uiState: StateFlow<FacilityUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow("filter_all")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Carga las sedes filtradas por tipo de deporte y permisos del usuario.
     * Actualiza dinámicamente el contador de canchas activas.
     */
    fun loadFacilities(sport: String, role: String, authorizedSedes: List<String>) {
        viewModelScope.launch {
            _uiState.value = FacilityUiState.Loading
            try {
                // Cambiamos a observación en tiempo real
                facilityRepository.observeFacilities(role, authorizedSedes, sport).collect { rawFacilities ->
                    val mockGymSedes = listOf(
                        Sede(
                            id = "gym_sede_1",
                            nombreSede = "Centro de Entrenamiento Central",
                            ubicacion = "Caracas - Av. Principal de Las Mercedes",
                            totalCanchas = 6,
                            courtsAvailable = 6,
                            tags = listOf("WOD", "Fuerza", "HIIT", "Cardio"),
                            starRating = 4.9,
                            imageUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=600"
                        ),
                        Sede(
                            id = "gym_sede_2",
                            nombreSede = "Elite Gym & Performance Lab",
                            ubicacion = "Caracas - El Hatillo",
                            totalCanchas = 8,
                            courtsAvailable = 8,
                            tags = listOf("Powerlifting", "Spinning", "Calistenia"),
                            starRating = 5.0,
                            imageUrl = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600"
                        )
                    )

                    val facilities = if (rawFacilities.isEmpty() && (sport.equals("CROSSFIT", ignoreCase = true) || sport.equals("GYM", ignoreCase = true))) {
                        mockGymSedes
                    } else {
                        rawFacilities
                    }

                    // Sincronizar dinámicamente el contador de canchas y tipos para filtros
                    val synchronizedFacilities = facilities.map { sede ->
                        try {
                            val canchas = facilityRepository.getCanchasBySede(sede.id)
                            val courtTypes = canchas.map { it.tipo.lowercase() }
                            
                            val dynamicTags = sede.tags.toMutableSet()
                            if (courtTypes.any { it.contains("dentro") || it.contains("interior") }) {
                                dynamicTags.add("interior")
                            }
                            if (courtTypes.any { it.contains("fuera") || it.contains("exterior") }) {
                                dynamicTags.add("exterior")
                            }

                            sede.copy(
                                totalCanchas = canchas.size,
                                courtsAvailable = canchas.count { it.estado == "ACTIVA" },
                                tags = dynamicTags.toList()
                            )
                        } catch (e: Exception) {
                            sede
                        }
                    }
                    _uiState.value = FacilityUiState.Success(synchronizedFacilities)
                }
            } catch (e: Exception) {
                _uiState.value = FacilityUiState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    fun onFilterSelected(filterId: String) {
        _selectedFilter.value = filterId
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}