package com.example.arena.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.Model.Di.Daos.UserDao
import com.example.arena.domain.Sede
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class StaffSetupUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val setupComplete: Boolean = false,
    val generatedSedeId: String? = null
)

@HiltViewModel
class StaffSetupViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffSetupUiState())
    val uiState: StateFlow<StaffSetupUiState> = _uiState.asStateFlow()

    fun setupVenue(
        venueName: String,
        numCourts: Int,
        extras: List<String>,
        servicios: List<String> = emptyList(),
        sportType: String = "PADEL" // Default for setup
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                val user = userDao.getUser() ?: throw Exception("Usuario no encontrado")
                
                val batch = db.batch()
                
                // 1. Create Sede
                val sedeRef = db.collection("sede").document()
                val sedeId = sedeRef.id
                val newSede = mapOf(
                    "id" to sedeId,
                    "nombreSede" to venueName,
                    "tipo" to sportType,
                    "totalCanchas" to numCourts,
                    "canchasDisponibles" to numCourts,
                    "ubicacion" to "Por definir",
                    "tags" to extras,
                    "servicios" to servicios,
                    "calificacion" to 5.0,
                    "suscripcionStatus" to "ACTIVA",
                    "precioBase" to 45.0
                )
                batch.set(sedeRef, newSede)
                
                // 2. Create Courts (Canchas)
                for (i in 1..numCourts) {
                    val canchaRef = db.collection("canchas").document()
                    val newCancha = mapOf(
                        "id" to canchaRef.id,
                        "sedeid" to sedeId,
                        "nombre" to "Cancha $i",
                        "tipo" to sportType,
                        "estado" to "ACTIVA",
                        "imageUrl" to "",
                        "patrocinador" to "Arena"
                    )
                    batch.set(canchaRef, newCancha)
                }
                
                // 3. Update Staff document
                val staffRef = db.collection("staff").document(user.uid)
                batch.update(staffRef, "sedesAutorizadas", com.google.firebase.firestore.FieldValue.arrayUnion(sedeId))
                
                batch.commit().await()
                
                _uiState.value = _uiState.value.copy(
                    isSaving = false, 
                    setupComplete = true,
                    generatedSedeId = sedeId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.localizedMessage)
            }
        }
    }
}
