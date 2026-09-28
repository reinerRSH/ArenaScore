package com.example.arena.ui.admin

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.R
import com.example.arena.Model.Di.Daos.UserDao
import com.example.arena.repository.FacilityRepository
import com.example.arena.domain.Cancha
import com.example.arena.domain.Reserva
import com.example.arena.domain.Sede
import com.example.arena.domain.Instructor
import com.example.arena.ui.screens.ActiveMatchShared
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * Representa el estado de la UI para el panel de administración.
 */
data class AdminUiState(
    val selectedSede: Sede? = null,
    val canchas: List<Cancha> = emptyList(),
    val instructores: List<Instructor> = emptyList(),
    val pendingPayments: List<Reserva> = emptyList(),
    val activeMatches: List<ActiveMatchShared> = emptyList(),
    val notifications: List<Reserva> = mutableListOf(),
    val selectedReceipt: Reserva? = null,
    val isLoading: Boolean = false,
    val isImageUploading: Boolean = false,
    val isLogout: Boolean = false,
    val message: String? = null,
    val dailyReservations: List<Reserva> = emptyList(),
    val currentTimeScale: Int = 0
)

/**
 * ViewModel para el Panel de Administración de Arena.
 * Utiliza exclusivamente el Repositorio para interactuar con Firestore.
 */
@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val repository: FacilityRepository,
    private val userDao: UserDao,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    private var canchasJob: Job? = null
    private var sedeJob: Job? = null
    private var instructoresJob: Job? = null
    private var pagosJob: Job? = null
    private var reservationsJob: Job? = null
    private var timerJob: Job? = null

    init {
        startTimer()
    }

    private var lastDate: String = java.time.LocalDate.now().toString()

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val now = java.time.LocalTime.now()
                val today = java.time.LocalDate.now().toString()
                
                // Si cambió el día, recargamos todo para limpiar el horario
                if (today != lastDate) {
                    lastDate = today
                    _uiState.value.selectedSede?.id?.let { loadAdminData(it) }
                }

                val h = now.hour
                val scale = if (h < 6) h + 24 else h
                _uiState.update { it.copy(currentTimeScale = scale) }
                kotlinx.coroutines.delay(60000) // Update every minute
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                userDao.deleteUser()
                auth.signOut()
                _uiState.update { it.copy(isLogout = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLogout = true) }
            }
        }
    }

    fun updateSelectedCourtForSchedule(canchaId: String) {
        val sedeId = _uiState.value.selectedSede?.id ?: return
        
        reservationsJob?.cancel()
        reservationsJob = repository.observeReservasPorSede(sedeId)
            .catch { e -> Log.e("AdminDashboardViewModel", "Error observando reservas", e) }
            .onEach { list ->
                _uiState.update { it.copy(dailyReservations = list) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Carga los datos de la sede y sus canchas en tiempo real con manejo de errores de flujo.
     */
    fun loadAdminData(sedeId: String) {
        viewModelScope.launch {
            val now = java.time.LocalTime.now()
            val h = now.hour
            val scale = if (h < 6) h + 24 else h
            
            _uiState.update { it.copy(
                isLoading = true, 
                message = "Sincronizando con el servidor...",
                currentTimeScale = scale
            ) }
            try {
                val localUser = userDao.getUser()
                val userId = if (localUser?.role == "STAFF") localUser.uid else (localUser?.uid ?: auth.currentUser?.uid ?: throw Exception("Sesión inválida"))
                
                // Validación de seguridad (Repository es el encargado de verificar el documento staff)
                if (!repository.esStaff(userId)) {
                    _uiState.update { it.copy(isLoading = false, message = "ACCESO DENEGADO: No eres personal autorizado.") }
                    return@launch
                }

                // Observar SEDE con manejo de errores
                sedeJob?.cancel()
                sedeJob = repository.observeSedeById(sedeId)
                    .catch { e -> _uiState.update { it.copy(message = "Error en sede: ${e.message}") } }
                    .onEach { sede ->
                        if (sede != null) {
                            _uiState.update { it.copy(selectedSede = sede, isLoading = false, message = null) }
                        }
                    }
                    .launchIn(viewModelScope)

                // Observar CANCHAS con manejo de errores
                canchasJob?.cancel()
                canchasJob = repository.observeCanchasBySede(sedeId)
                    .catch { e -> _uiState.update { it.copy(message = "Error en canchas: ${e.message}") } }
                    .onEach { list ->
                        _uiState.update { it.copy(canchas = list) }
                        calculateActiveMatches(list)
                    }
                    .launchIn(viewModelScope)

                // Observar INSTRUCTORES con manejo de errores
                instructoresJob?.cancel()
                instructoresJob = repository.observeInstructoresBySede(sedeId)
                    .catch { e -> _uiState.update { it.copy(message = "Error en instructores: ${e.message}") } }
                    .onEach { list ->
                        _uiState.update { it.copy(instructores = list) }
                    }
                    .launchIn(viewModelScope)

                // Observar PAGOS PENDIENTES en tiempo real
                pagosJob?.cancel()
                pagosJob = repository.observePagosPendientes(sedeId)
                    .onEach { list ->
                        _uiState.update { it.copy(pendingPayments = list) }
                    }
                    .launchIn(viewModelScope)

                // Observar TODAS LAS RESERVAS DE LA SEDE en tiempo real (todas las canchas y fechas)
                reservationsJob?.cancel()
                reservationsJob = repository.observeReservasPorSede(sedeId)
                    .catch { e -> Log.e("AdminDashboardViewModel", "Error observando reservas de la sede", e) }
                    .onEach { list ->
                        _uiState.update { it.copy(dailyReservations = list) }
                    }
                    .launchIn(viewModelScope)

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, message = "Fallo de conexión: ${e.localizedMessage}") }
            }
        }
    }

    private fun calculateActiveMatches(canchas: List<Cancha>) {
        if (canchas.isEmpty()) return
        viewModelScope.launch {
            try {
                val today = LocalDate.now().toString()
                val now = LocalTime.now()
                
                val activeList = coroutineScope {
                    canchas.map { cancha ->
                        async {
                            val snapshots = repository.getReservasByCanchaYFecha(cancha.id, today)
                            val match = snapshots.find { it.estado == Reserva.STATUS_ACTIVA && isNowInInterval(it.horaInicio, it.horaFin, now) }
                            if (match != null) ActiveMatchShared(cancha.id, cancha.nombre, match.horaInicio, match.horaFin) else null
                        }
                    }.awaitAll().filterNotNull()
                }
                
                _uiState.update { it.copy(activeMatches = activeList) }
            } catch (e: Exception) {
                Log.e("AdminDashboardViewModel", "Error calculando partidos activos", e)
            }
        }
    }

    private fun isNowInInterval(start: String, end: String, now: LocalTime): Boolean {
        return try {
            val s = LocalTime.parse(start)
            val e = LocalTime.parse(end)
            !now.isBefore(s) && now.isBefore(e)
        } catch (e: Exception) { false }
    }

    /**
     * Toglea el estado de una cancha usando el repositorio.
     */
    fun toggleCanchaStatus(canchaId: String, status: String) {
        viewModelScope.launch {
            // Optimistic Update: Actualizamos localmente para feedback inmediato y evitar flickers
            _uiState.update { state ->
                state.copy(canchas = state.canchas.map { if (it.id == canchaId) it.copy(estado = status) else it })
            }
            
            // Persistencia en Firebase mediante el repositorio
            val result = repository.actualizarEstadoCancha(canchaId, status)
            if (result.isFailure) {
                _uiState.update { it.copy(message = "Fallo al actualizar: Permiso Denegado") }
            }
        }
    }

    fun verifyPayment(reservaId: String, approve: Boolean) {
        viewModelScope.launch {
            val status = if (approve) Reserva.STATUS_ACTIVA else Reserva.STATUS_CANCELADA
            val result = repository.actualizarEstadoReserva(reservaId, status)
            if (result.isFailure) {
                _uiState.update { it.copy(message = "Error al validar pago") }
            }
        }
    }

    fun createManualBookings(canchaId: String, horas: List<String>, fecha: String = LocalDate.now().toString()) {
        if (horas.isEmpty()) return
        viewModelScope.launch {
            try {
                val sedeId = _uiState.value.selectedSede?.id ?: return@launch
                val targetDate = fecha.ifEmpty { LocalDate.now().toString() }
                
                val newBookings = mutableListOf<Reserva>()
                horas.forEach { horaInicio ->
                    val start = java.time.LocalTime.parse(horaInicio)
                    val end = start.plusHours(1)
                    
                    val manualReserva = Reserva(
                        canchaid = canchaId,
                        sedeid = sedeId,
                        usuarioid = "MANUAL_ADMIN",
                        nombreUsuario = "RESERVA MANUAL (ADMIN)",
                        fecha = targetDate,
                        horaInicio = horaInicio,
                        horaFin = end.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                        estado = Reserva.STATUS_ACTIVA,
                        referenciaPago = "MANUAL",
                        montoTotal = 0.0,
                        leida = true,
                        instructorId = ""
                    )
                    newBookings.add(manualReserva)
                    repository.crearReserva(manualReserva)
                }
                
                // Optimistic UI Update: Añadir localmente para feedback instantáneo
                _uiState.update { state ->
                    state.copy(
                        dailyReservations = (state.dailyReservations + newBookings).distinctBy { it.horaInicio },
                        message = "${horas.size} turnos ocupados"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Error: ${e.localizedMessage}") }
            }
        }
    }

    fun cancelReservation(reservaId: String) {
        viewModelScope.launch {
            repository.actualizarEstadoReserva(reservaId, Reserva.STATUS_CANCELADA)
        }
    }

    fun updatePrice(newPrice: Double) {
        val id = _uiState.value.selectedSede?.id ?: return
        viewModelScope.launch {
            repository.actualizarConfiguracionSede(id, mapOf("precioBase" to newPrice))
        }
    }

    fun updateServicios(tags: List<String>) {
        val id = _uiState.value.selectedSede?.id ?: return
        viewModelScope.launch {
            repository.actualizarConfiguracionSede(id, mapOf("tags" to tags))
        }
    }

    fun updateExtras(extras: Map<String, Double>) {
        val id = _uiState.value.selectedSede?.id ?: return
        viewModelScope.launch {
            repository.actualizarConfiguracionSede(id, mapOf("extras" to extras))
        }
    }

    fun updatePagoMovil(banco: String, doc: String, tel: String) {
        val id = _uiState.value.selectedSede?.id ?: return
        viewModelScope.launch {
            repository.actualizarDatosPago(id, mapOf("banco" to banco, "documento" to doc, "telefono" to tel))
        }
    }

    fun saveInstructor(instructor: Instructor) {
        val id = _uiState.value.selectedSede?.id ?: return
        viewModelScope.launch {
            repository.upsertInstructor(id, instructor)
        }
    }

    fun removeInstructor(instrId: String) {
        val id = _uiState.value.selectedSede?.id ?: return
        viewModelScope.launch {
            repository.eliminarInstructor(id, instrId)
        }
    }

    fun showReceipt(r: Reserva) { _uiState.update { it.copy(selectedReceipt = r) } }
    fun dismissReceipt() { _uiState.update { it.copy(selectedReceipt = null) } }

    fun updateCancha(id: String, name: String, img: String, sponsor: String, type: String) {
        viewModelScope.launch {
            repository.actualizarCancha(id, mapOf("nombre" to name, "imageUrl" to img, "patrocinador" to sponsor, "tipo" to type))
        }
    }

    fun uploadCourtImage(id: String, uri: android.net.Uri) {
        _uiState.update { it.copy(isImageUploading = true) }
        viewModelScope.launch {
            try {
                val path = "canchas/${id}_${System.currentTimeMillis()}.jpg"
                val ref = storage.reference.child(path)
                ref.putFile(uri).await()
                val url = ref.downloadUrl.await().toString()
                repository.actualizarCancha(id, mapOf("imageUrl" to url))
                _uiState.update { it.copy(isImageUploading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isImageUploading = false, message = "Error al subir imagen") }
            }
        }
    }
}
