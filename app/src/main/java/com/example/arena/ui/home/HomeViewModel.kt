package com.example.arena.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.Model.Di.Daos.UserDao
import com.example.arena.Model.Di.Data.FacilityRepository
import com.example.arena.Model.Di.Domain.Sede
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Estados posibles para la navegación y carga desde el Home.
 */
sealed interface HomeNavigationState {
    object Idle : HomeNavigationState
    object Loading : HomeNavigationState
    data class Success(
        val facilities: List<Sede>, 
        val sport: String,
        val role: String,
        val authorizedSedes: List<String>
    ) : HomeNavigationState
    data class Error(val message: String) : HomeNavigationState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val facilityRepository: FacilityRepository,
    private val userDao: UserDao,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeNavigationState>(HomeNavigationState.Idle)
    val uiState: StateFlow<HomeNavigationState> = _uiState.asStateFlow()

    /**
     * Selecciona un deporte y carga las sedes autorizadas para el usuario actual.
     */
    fun selectSport(sport: String) {
        viewModelScope.launch {
            _uiState.value = HomeNavigationState.Loading
            
            try {
                // 1. Obtenemos el usuario de la DB local
                val userEntity = userDao.getUSer() 
                    ?: throw Exception("Usuario no encontrado en sesión")
                
                var role = userEntity.role
                var authorizedSedes = emptyList<String>()

                // 2. Si es STAFF, necesitamos sus sedes autorizadas desde Firestore
                if (role == "STAFF") {
                    val staffDoc = db.collection("staff").document(userEntity.uid).get().await()
                    authorizedSedes = staffDoc.get("sedesAutorizadas") as? List<String> ?: emptyList()
                    // Actualizamos el rol por si acaso
                    role = staffDoc.getString("role") ?: "STAFF"
                }

                // 3. Consultamos el repositorio
                val facilities = facilityRepository.getFacilities(
                    role = role,
                    authorizedSedes = authorizedSedes,
                    sportType = sport
                )
                
                if (facilities.isEmpty()) {
                    _uiState.value = HomeNavigationState.Error("No se encontraron sedes para $sport")
                } else {
                    _uiState.value = HomeNavigationState.Success(facilities, sport, role, authorizedSedes)
                }
            } catch (e: Exception) {
                _uiState.value = HomeNavigationState.Error(e.localizedMessage ?: "Error al cargar sedes")
            }
        }
    }

    /**
     * Resetea el estado para permitir nuevas selecciones.
     */
    fun resetState() {
        _uiState.value = HomeNavigationState.Idle
    }
}
