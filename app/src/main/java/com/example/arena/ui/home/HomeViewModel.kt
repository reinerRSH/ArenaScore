package com.example.arena.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.Model.Di.Daos.UserDao
import com.example.arena.repository.FacilityRepository
import com.example.arena.domain.Sede
import com.example.arena.domain.Reserva
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Estados posibles para la navegación y carga desde el Home.
 */
sealed interface HomeNavigationState {
    object Idle : HomeNavigationState
    object Loading : HomeNavigationState
    object Logout : HomeNavigationState
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
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeNavigationState>(HomeNavigationState.Idle)
    val uiState: StateFlow<HomeNavigationState> = _uiState.asStateFlow()

    private val _notifications = MutableStateFlow<List<Reserva>>(emptyList())
    val notifications: StateFlow<List<Reserva>> = _notifications.asStateFlow()

    init {
        observeUserNotifications()
    }

    private fun observeUserNotifications() {
        viewModelScope.launch {
            val user = userDao.getUser() ?: return@launch
            
            db.collection("reservas")
                .whereEqualTo("usuarioid", user.uid)
                .whereEqualTo("leida", false)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("HomeViewModel", "Error observando notificaciones", e)
                        return@addSnapshotListener
                    }
                    
                    val unreadReservas = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Reserva::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                    
                    _notifications.value = unreadReservas
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userDao.deleteUser()
            auth.signOut()
            _uiState.value = HomeNavigationState.Logout
        }
    }

    /**
     * Selecciona un deporte y carga las sedes autorizadas para el usuario actual.
     */
    fun selectSport(sport: String) {
        viewModelScope.launch {
            _uiState.value = HomeNavigationState.Loading
            
            try {
                // 1. Obtenemos el usuario de la DB local
                val userEntity = userDao.getUser() 
                
                Log.d("HomeViewModel", "User session: ${userEntity}")

                if (userEntity == null) {
                    throw Exception("Usuario no encontrado en sesión")
                }
                
                var role = userEntity.role
                var authorizedSedes = emptyList<String>()

                Log.d("HomeViewModel", "Role: $role, UID: ${userEntity.uid}")

                // 2. Si es STAFF, necesitamos sus sedes autorizadas desde Firestore
                if (role == "STAFF") {
                    // Consultamos el documento admin2026 en la colección staff
                    val staffDoc = db.collection("staff").document(userEntity.uid).get().await()
                    
                    if (staffDoc.exists()) {
                        // Verificamos que el rol en Firestore sea efectivamente STAFF
                        val firestoreRole = staffDoc.getString("role")
                        if (firestoreRole == "STAFF" || firestoreRole == "ADMIN") {
                            authorizedSedes = staffDoc.get("sedesAutorizadas") as? List<String> ?: emptyList()
                            role = "STAFF"
                        } else {
                            throw Exception("El usuario no tiene permisos de Staff en el servidor")
                        }
                    } else {
                        throw Exception("No se encontró el registro de Staff para el ID: ${userEntity.uid}")
                    }
                }

                // 3. Consultamos el repositorio
                val rawFacilities = facilityRepository.getFacilities(
                    role = role,
                    authorizedSedes = authorizedSedes,
                    sportType = sport
                )

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
                
                if (facilities.isEmpty() && role != "STAFF") {
                    Log.w("HomeViewModel", "No facilities found for role: $role, sport: $sport, authorized: $authorizedSedes")
                    _uiState.value = HomeNavigationState.Error("No se encontraron sedes para $sport")
                } else {
                    Log.d("HomeViewModel", "Emitting Success. Role: $role, Facilities: ${facilities.size}, Authorized count: ${authorizedSedes.size}")
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

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            try {
                val user = userDao.getUser() ?: return@launch
                val snapshot = db.collection("reservas")
                    .whereEqualTo("usuarioid", user.uid)
                    .whereEqualTo("leida", false)
                    .get()
                    .await()
                
                if (snapshot.isEmpty) return@launch

                val batch = db.batch()
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "leida", true)
                }
                batch.commit().await()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error marking notifications as read", e)
            }
        }
    }
}