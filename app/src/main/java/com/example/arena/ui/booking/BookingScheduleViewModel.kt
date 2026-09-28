package com.example.arena.ui.booking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.repository.FacilityRepository
import com.example.arena.repository.UserRepository
import com.example.arena.domain.Reserva
import com.example.arena.domain.Sede
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// MVI State
data class BookingScheduleState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: String? = null,
    val reservedSlots: List<String> = emptyList(),
    val instructores: List<com.example.arena.domain.Instructor> = emptyList(),
    val selectedInstructorId: String? = null,
    val sedeId: String = "",
    val canchaId: String = "",
    val canchaName: String = "",
    val tipoReserva: String = "",
    val selectedSede: Sede? = null,
    
    // Payment Logic
    val selectedExtras: Map<String, Double> = emptyMap(),
    val totalPrice: Double = 0.0,
    val paymentReference: String = "",
    val isPaymentSheetVisible: Boolean = false,
    val paymentStatus: PaymentStatus = PaymentStatus.Idle,
    
    val isConfirmationVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val canchaImageUrl: String = "",
    val lastReserva: Reserva? = null,
    val instructorSlots: Map<String, String> = emptyMap(), // Map of Hour to Mode (V/A)
    val slotsRemaining: Map<String, Int> = emptyMap()
)

sealed interface PaymentStatus {
    object Idle : PaymentStatus
    object Processing : PaymentStatus
    object Verified : PaymentStatus
    data class Failed(val message: String) : PaymentStatus
}

// MVI Intents
sealed class BookingIntent {
    data class ToggleExtra(val name: String, val price: Double) : BookingIntent()
    data class UpdateReference(val ref: String) : BookingIntent()
    object ShowPaymentSheet : BookingIntent()
    object HidePaymentSheet : BookingIntent()
    object ConfirmPayment : BookingIntent()
}

@HiltViewModel
class BookingScheduleViewModel @Inject constructor(
    private val repository: FacilityRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingScheduleState())
    val uiState: StateFlow<BookingScheduleState> = _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        limpiarReservasAntiguas()
    }

    fun handleIntent(intent: BookingIntent) {
        when (intent) {
            is BookingIntent.ToggleExtra -> toggleExtra(intent.name, intent.price)
            is BookingIntent.UpdateReference -> _uiState.update { it.copy(paymentReference = intent.ref) }
            BookingIntent.ShowPaymentSheet -> {
                if (_uiState.value.tipoReserva == "CLASES") {
                    processPayment()
                } else {
                    _uiState.update { it.copy(isPaymentSheetVisible = true) }
                }
            }
            BookingIntent.HidePaymentSheet -> _uiState.update { it.copy(isPaymentSheetVisible = false) }
            BookingIntent.ConfirmPayment -> processPayment()
        }
    }

    fun init(sedeId: String, canchaId: String, tipoReserva: String, canchaName: String = "") {
        viewModelScope.launch {
            val sede = repository.getSedeById(sedeId)
            val cancha = repository.getCanchaById(canchaId)
            _uiState.update { it.copy(
                sedeId = sedeId,
                canchaId = canchaId,
                tipoReserva = tipoReserva,
                canchaName = canchaName.ifEmpty { it.canchaName },
                canchaImageUrl = cancha?.imageUrl ?: "",
                selectedSede = sede,
                totalPrice = if (tipoReserva == "CLASES") 0.0 else (sede?.precioBase ?: 45.0)
            ) }
            
            if (tipoReserva == "CLASES") {
                launch {
                    repository.observeInstructoresBySede(sedeId).collect { list ->
                        _uiState.update { it.copy(instructores = list) }
                    }
                }
            }

            loadReservedSlots()
        }
    }

    fun onInstructorSelected(id: String) {
        val instructor = _uiState.value.instructores.find { it.id == id }
        val slotsMap = instructor?.horario?.associate { 
            val parts = it.split("_")
            if (parts.size == 2) parts[1] to parts[0] else it to "V"
        } ?: emptyMap()
        
        _uiState.update { it.copy(selectedInstructorId = id, instructorSlots = slotsMap, selectedTime = null) }
        loadReservedSlots() // Recargar slots al cambiar de instructor
    }

    private fun toggleExtra(name: String, price: Double) {
        _uiState.update { state ->
            val newExtras = state.selectedExtras.toMutableMap()
            if (newExtras.containsKey(name)) {
                newExtras.remove(name)
            } else {
                newExtras[name] = price
            }
            val base = state.selectedSede?.precioBase ?: 45.0
            val total = base + newExtras.values.sum()
            state.copy(selectedExtras = newExtras, totalPrice = total)
        }
    }

    private fun processPayment() {
        val state = _uiState.value
        val isClass = state.tipoReserva == "CLASES"
        
        if (!isClass && state.paymentReference.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Ingrese el número de referencia") }
            return
        }

        _uiState.update { it.copy(paymentStatus = PaymentStatus.Processing, isLoading = true) }

        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                val userId = user?.uid ?: FirebaseAuth.getInstance().currentUser?.uid ?: "uid-generico"
                val userName = user?.let { "${it.name} ${it.lastName}" } ?: "Usuario Anónimo"
                val userPhone = user?.phone ?: ""

                val fechaFormatted = state.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val startTime = LocalTime.parse(state.selectedTime)
                val endTime = startTime.plusHours(1)

                // Obtener imagen de la cancha actual para guardarla en la reserva
                val currentCancha = repository.getCanchaById(state.canchaId)
                val canchaImageUrl = currentCancha?.imageUrl ?: ""

                val reserva = Reserva(
                    canchaid = state.canchaId,
                    estado = if (isClass) Reserva.STATUS_ACTIVA else Reserva.STATUS_PENDIENTE,
                    fecha = fechaFormatted,
                    horaInicio = startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    horaFin = endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    sedeid = state.sedeId,
                    usuarioid = userId,
                    nombreUsuario = userName,
                    telefonoUsuario = userPhone,
                    extras = if (isClass) emptyMap() else state.selectedExtras,
                    referenciaPago = if (isClass) "CLASE_PREPAGADA" else state.paymentReference,
                    montoTotal = if (isClass) 0.0 else state.totalPrice,
                    canchaImageUrl = canchaImageUrl,
                    instructorId = state.selectedInstructorId ?: ""
                )

                val result = repository.crearReserva(reserva)
                if (result.isSuccess) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        paymentStatus = PaymentStatus.Verified,
                        isPaymentSheetVisible = false,
                        isConfirmationVisible = true,
                        selectedTime = null,
                        lastReserva = reserva
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        paymentStatus = PaymentStatus.Failed(result.exceptionOrNull()?.message ?: "Error"),
                        errorMessage = "Error en la reserva"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, paymentStatus = PaymentStatus.Failed(e.message ?: "")) }
            }
        }
    }

    private fun loadReservedSlots() {
        val state = _uiState.value
        if (state.canchaId.isEmpty()) return

        observationJob?.cancel()
        
        val dateStr = state.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val isToday = state.selectedDate == LocalDate.now()
        val closingTime = normalizeTime(state.selectedSede?.horaCierre ?: "01:00")
        val openingTime = normalizeTime(state.selectedSede?.horaApertura ?: "06:00")
        val pastSlots = if (isToday) obtenerSlotsPasados(openingTime, closingTime) else emptyList()

        val courtReservasFlow = repository.observeReservasByCanchaYFecha(state.canchaId, dateStr)
        val instructoresFlow = repository.observeInstructoresBySede(state.sedeId)
        val selectedInstructorFlow = if (state.selectedInstructorId != null) {
            repository.observeReservasByInstructorYFecha(state.selectedInstructorId, dateStr)
        } else flowOf(emptyList<Reserva>())

        observationJob = combine(courtReservasFlow, instructoresFlow, selectedInstructorFlow) { courtReservas, instructores, instructorReservas ->
            val blockedByJuego = courtReservas
                .filter { it.estado != Reserva.STATUS_CANCELADA && it.instructorId.isEmpty() }
                .map { it.horaInicio }

            val reservedList = mutableListOf<String>()
            val remainingMap = mutableMapOf<String, Int>()

            if (state.tipoReserva == "CLASES" && state.selectedInstructorId != null) {
                val currentInstructor = instructores.find { it.id == state.selectedInstructorId }
                val max = currentInstructor?.maxAlumnos ?: 5

                // Agrupar reservas del instructor por hora
                val counts = instructorReservas
                    .filter { it.estado != Reserva.STATUS_CANCELADA }
                    .groupBy { it.horaInicio }
                    .mapValues { it.value.size }

                // Para cada slot del horario del instructor
                currentInstructor?.horario?.forEach { slotEntry ->
                    val time = slotEntry.split("_").last()
                    val count = counts[time] ?: 0
                    val remaining = max - count
                    remainingMap[time] = if (remaining < 0) 0 else remaining
                    
                    if (remaining <= 0 || blockedByJuego.contains(time)) {
                        reservedList.add(time)
                    }
                }
            } else {
                // Modo JUEGO o sin instructor seleccionado
                reservedList.addAll(blockedByJuego)
                
                if (state.tipoReserva == "JUEGO") {
                    val occupiedByClasses = instructores
                        .filter { it.canchaid == state.canchaId }
                        .flatMap { it.horario }
                        .map { it.split("_").last() }
                    reservedList.addAll(occupiedByClasses)
                }
            }

            reservedList.addAll(pastSlots)
            Pair(reservedList.distinct(), remainingMap)
        }
        .onEach { (list, remaining) ->
            _uiState.update { it.copy(reservedSlots = list, slotsRemaining = remaining) }
        }
        .launchIn(viewModelScope)
    }

    /**
     * Devuelve los turnos disponibles filtrados por el instructor seleccionado.
     */
    fun getAvailableInstructorSlots(instructorId: String): List<String> {
        val instructor = _uiState.value.instructores.find { it.id == instructorId } ?: return emptyList()
        // Solo mostramos las horas que el Admin marcó como "Disponibles" (Prefijo V)
        return instructor.horario
            .filter { it.startsWith("V_") }
            .map { it.split("_").last() }
            .filter { !(_uiState.value.reservedSlots.contains(it)) } // No reservadas ya
    }

    private fun normalizeTime(time: String): String {
        return try {
            if (time.length == 4) "0$time" else time
        } catch (e: Exception) {
            time
        }
    }

    /**
     * Lógica mejorada para detectar slots pasados en un ciclo operativo.
     */
    private fun obtenerSlotsPasados(openingTime: String, closingTime: String): List<String> {
        val now = LocalTime.now()
        val allPossibleSlots = listOf(
            "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
            "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
            "18:00", "19:00", "20:00", "21:00", "22:00", "23:00",
            "00:00", "01:00"
        )
        
        val scale = mapOf("06:00" to 6, "07:00" to 7, "08:00" to 8, "09:00" to 9, "10:00" to 10, "11:00" to 11, "12:00" to 12, "13:00" to 13, "14:00" to 14, "15:00" to 15, "16:00" to 16, "17:00" to 17, "18:00" to 18, "19:00" to 19, "20:00" to 20, "21:00" to 21, "22:00" to 22, "23:00" to 23, "00:00" to 24, "01:00" to 25)
        
        val openingScale = scale[openingTime] ?: 6
        val closingScale = scale[closingTime] ?: 25
        
        val validSlots = allPossibleSlots.filter { 
            val s = scale[it] ?: 0
            s >= openingScale && s <= closingScale 
        }

        val nowHour = now.hour
        val nowScale = if (nowHour < 6) nowHour + 24 else nowHour

        return validSlots.filter { slot ->
            val slotScale = scale[slot] ?: 0
            slotScale <= nowScale
        }
    }

    private fun limpiarReservasAntiguas() {
        viewModelScope.launch {
            val activas = repository.getReservasActivas()
            val ahora = LocalDateTime.now()
            activas.forEach { reserva ->
                try {
                    val finReserva = LocalDateTime.of(LocalDate.parse(reserva.fecha), LocalTime.parse(reserva.horaFin))
                    if (finReserva.isBefore(ahora)) repository.actualizarEstadoReserva(reserva.id, Reserva.STATUS_FINALIZADA)
                } catch (e: Exception) {
                    Log.e("BookingScheduleViewModel", "Error procesando reserva antigua: ${reserva.id}", e)
                }
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, selectedTime = null) }
        loadReservedSlots()
    }

    fun onTimeSelected(time: String) {
        _uiState.update { it.copy(selectedTime = time) }
    }

    fun hideConfirmation() {
        _uiState.update { it.copy(isConfirmationVisible = false, selectedTime = null) }
    }

    fun reset() {
        _uiState.value = BookingScheduleState()
    }
}