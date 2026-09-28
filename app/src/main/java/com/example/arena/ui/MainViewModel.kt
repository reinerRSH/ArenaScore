package com.example.arena.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.Model.Di.Daos.UserDao
import com.example.arena.domain.Reserva
import com.example.arena.repository.FacilityRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userDao: UserDao,
    private val db: FirebaseFirestore,
    private val repository: FacilityRepository
) : ViewModel() {

    private val _isNotificationModalOpen = MutableStateFlow(false)
    val isNotificationModalOpen = _isNotificationModalOpen.asStateFlow()

    private val _notifications = MutableStateFlow<List<Reserva>>(emptyList())
    val notifications = _notifications.asStateFlow()

    init {
        observeUserNotifications()
    }

    fun setNotificationModalOpen(open: Boolean) {
        _isNotificationModalOpen.value = open
    }

    private fun observeUserNotifications() {
        viewModelScope.launch {
            val user = userDao.getUSer() ?: return@launch
            
            db.collection("reservas")
                .whereEqualTo("usuarioid", user.uid)
                .whereEqualTo("leida", false)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("MainViewModel", "Error observando notificaciones", e)
                        return@addSnapshotListener
                    }
                    
                    val unreadReservas = snapshot?.documents?.mapNotNull { doc ->
                        // Inyectar ID manualmente si no viene en el mapeo
                        doc.toObject(Reserva::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                    
                    _notifications.value = unreadReservas
                }
        }
    }

    fun handleNotificationClick(reserva: Reserva, onNavigate: () -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Marcar como leída en Firestore
                db.collection("reservas").document(reserva.id)
                    .update("leida", true)
                    .await()

                // 2. Cerrar modal global
                _isNotificationModalOpen.value = false

                // 3. Ejecutar navegación (hacia Mis Reservas)
                onNavigate()
                
                Log.d("MainViewModel", "Notificación procesada: ${reserva.id}")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error procesando click de notificación", e)
            }
        }
    }
}
