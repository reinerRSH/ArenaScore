package com.example.arena.ui.facility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.Model.Di.Data.FacilityRepository
import com.example.arena.Model.Di.Domain.Sede
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
     */
    fun loadFacilities(sport: String, role: String, authorizedSedes: List<String>) {
        viewModelScope.launch {
            _uiState.value = FacilityUiState.Loading
            try {
                val facilities = facilityRepository.getFacilities(
                    role = role,
                    authorizedSedes = authorizedSedes,
                    sportType = sport
                )
                
                if (facilities.isEmpty()) {
                    _uiState.value = FacilityUiState.Error("No se encontraron sedes disponibles para $sport")
                } else {
                    _uiState.value = FacilityUiState.Success(facilities)
                }
            } catch (e: Exception) {
                _uiState.value = FacilityUiState.Error(e.localizedMessage ?: "Error desconocido al cargar sedes")
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
