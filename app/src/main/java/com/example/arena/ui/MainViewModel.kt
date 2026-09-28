package com.example.arena.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.MainActivity
import com.example.arena.Model.Di.Daos.UserDao
import com.example.arena.R
import com.example.arena.domain.Reserva
import com.example.arena.repository.FacilityRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userDao: UserDao,
    private val db: FirebaseFirestore,
    private val repository: FacilityRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isStaffUser = MutableStateFlow(false)
    val isStaffUser = _isStaffUser.asStateFlow()

    private val _isNotificationModalOpen = MutableStateFlow(false)
    val isNotificationModalOpen = _isNotificationModalOpen.asStateFlow()

    private val _notifications = MutableStateFlow<List<Reserva>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _pendingNavigation = MutableStateFlow<Pair<String, String?>?>(null)
    val pendingNavigation = _pendingNavigation.asStateFlow()

    private val notifiedReservationIds = mutableSetOf<String>()

    init {
        observeUserNotifications()
    }

    fun triggerNavigation(target: String, sedeId: String? = null) {
        _pendingNavigation.value = target to sedeId
    }

    fun consumeNavigation() {
        val nav = _pendingNavigation.value
        _pendingNavigation.value = null
        if (nav != null) {
            markNotificationsAsRead()
        }
    }

    fun setNotificationModalOpen(open: Boolean) {
        _isNotificationModalOpen.value = open
    }

    private fun observeUserNotifications() {
        viewModelScope.launch {
            val user = userDao.getUser() ?: return@launch
            _isStaffUser.value = (user.role == "STAFF")
            
            if (user.role == "STAFF") {
                // Notificaciones para Staff: Pagos Pendientes en sus sedes
                val staffDoc = db.collection("staff").document(user.uid).get().await()
                val authorizedSedes = staffDoc.get("sedesAutorizadas") as? List<String> ?: emptyList()
                
                val query = if (authorizedSedes.isNotEmpty()) {
                    db.collection("reservas")
                        .whereIn("sedeid", authorizedSedes)
                        .whereEqualTo("estado", Reserva.STATUS_PENDIENTE)
                        .whereEqualTo("leida", false)
                } else {
                    db.collection("reservas")
                        .whereEqualTo("estado", Reserva.STATUS_PENDIENTE)
                        .whereEqualTo("leida", false)
                }

                query.addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    val pending = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Reserva::class.java)?.copy(id = doc.id)
                    } ?: emptyList()

                    // Solo notificar nuevos pendientes no notificados antes
                    pending.forEach { reserva ->
                        if (!notifiedReservationIds.contains(reserva.id)) {
                            showLocalNotification(
                                channelId = "admin_alerts",
                                title = "Nuevo Pago Pendiente",
                                body = "Referencia: ${reserva.referenciaPago} por $${reserva.montoTotal}",
                                sedeId = reserva.sedeid,
                                reservaId = reserva.id
                            )
                        }
                    }
                    _notifications.value = pending
                }
            } else {
                // Notificaciones para Atletas: Reservas Propias (Aprobada / Rechazada)
                db.collection("reservas")
                    .whereEqualTo("usuarioid", user.uid)
                    .whereEqualTo("leida", false)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) return@addSnapshotListener
                        val current = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Reserva::class.java)?.copy(id = doc.id)
                        } ?: emptyList()

                        current.forEach { reserva ->
                            if (!notifiedReservationIds.contains(reserva.id) &&
                                (reserva.estado == Reserva.STATUS_ACTIVA || reserva.estado == Reserva.STATUS_CANCELADA)) {
                                val (title, body) = when (reserva.estado) {
                                    Reserva.STATUS_ACTIVA -> Pair(
                                        "Reserva Aprobada",
                                        "Tu reserva para el ${reserva.fecha} ha sido confirmada."
                                    )
                                    Reserva.STATUS_CANCELADA -> Pair(
                                        "Reserva Rechazada",
                                        "Tu reserva para el ${reserva.fecha} ha sido rechazada o cancelada."
                                    )
                                    else -> Pair("Actualización de Reserva", "Tu reserva cambió a ${reserva.estado}.")
                                }
                                showLocalNotification(
                                    channelId = "user_alerts",
                                    title = title,
                                    body = body,
                                    reservaId = reserva.id
                                )
                            }
                        }

                        _notifications.value = current
                    }
            }
        }
    }

    private fun showLocalNotification(
        channelId: String,
        title: String,
        body: String,
        sedeId: String? = null,
        reservaId: String = ""
    ) {
        if (reservaId.isNotEmpty() && notifiedReservationIds.contains(reservaId)) {
            return
        }
        if (reservaId.isNotEmpty()) {
            notifiedReservationIds.add(reservaId)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = if (channelId == "admin_alerts") "Alertas Administrador" else "Alertas Usuario"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = if (reservaId.isNotEmpty()) reservaId.hashCode() else System.currentTimeMillis().toInt()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("target_screen", if (channelId == "admin_alerts") "ADMIN_PAYMENTS" else "USER_RESERVATIONS")
            putExtra("sede_id", sedeId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_padel_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun handleNotificationClick(reserva: Reserva, onNavigate: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val user = userDao.getUser() ?: return@launch
                
                // 1. Marcar como leída en Firestore
                db.collection("reservas").document(reserva.id)
                    .update("leida", true)
                    .await()

                _isNotificationModalOpen.value = false
                _notifications.value = _notifications.value.filter { it.id != reserva.id }

                // Cancelar notificación del sistema
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(reserva.id.hashCode())

                // 2. Navegación basada en rol
                if (user.role == "STAFF") {
                    onNavigate("ADMIN_PAYMENTS")
                } else {
                    onNavigate("USER_RESERVATIONS")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error procesando click", e)
            }
        }
    }

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            try {
                val user = userDao.getUser() ?: return@launch
                val current = _notifications.value
                val snapshot = db.collection("reservas")
                    .whereEqualTo("usuarioid", user.uid)
                    .whereEqualTo("leida", false)
                    .get()
                    .await()
                
                if (!snapshot.isEmpty) {
                    val batch = db.batch()
                    for (doc in snapshot.documents) {
                        batch.update(doc.reference, "leida", true)
                    }
                    batch.commit().await()
                }

                _notifications.value = emptyList()

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                current.forEach { reserva ->
                    notificationManager.cancel(reserva.id.hashCode())
                }
                notificationManager.cancelAll()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error marcando todas las notificaciones como leídas", e)
            }
        }
    }
}
