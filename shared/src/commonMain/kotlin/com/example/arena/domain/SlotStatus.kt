package com.example.arena.domain

import androidx.compose.ui.graphics.Color

enum class SlotStatus {
    LIBRE,
    OCUPADO,            // Reserva de usuario o manual (ROJO)
    OCUPADO_INSTRUCTOR, // Clase asignada (DORADO/AMARILLO)
    PASADO,             // Tiempo ya transcurrido (GRIS)
    SELECCIONADO        // Estado local de la UI al elegir para guardar (CIAN)
}

object BookingLogicShared {
    /**
     * Fuente de Verdad Única para determinar el estado de un turno.
     * Utilizada tanto por el Administrador como por el Usuario.
     */
    fun getEstadoHorario(
        time: String,
        currentTimeScale: Int,
        reservas: List<Reserva>,
        instructores: List<Instructor>,
        canchaId: String,
        targetDate: String? = null
    ): SlotStatus {
        if (canchaId.isEmpty()) return SlotStatus.LIBRE

        // 1. Verificar si hay una reserva activa (Usuario o Manual) para la cancha y fecha seleccionadas
        val tieneReserva = reservas.any { 
            it.canchaid == canchaId && 
            (targetDate.isNullOrEmpty() || it.fecha == targetDate) && 
            it.horaInicio == time && 
            it.estado != Reserva.STATUS_CANCELADA 
        }
        if (tieneReserva) return SlotStatus.OCUPADO

        // 2. Verificar si el horario está asignado a un instructor (Clase)
        val esClase = instructores.any { 
            it.canchaid == canchaId && 
            it.horario.any { hSlot -> hSlot.endsWith(time) } 
        }
        if (esClase) return SlotStatus.OCUPADO_INSTRUCTOR

        // 3. Verificar si el tiempo ya pasó (solo aplica para hoy)
        val parts = time.split(":")
        val h = parts[0].toIntOrNull() ?: 0
        val slotScale = if (h < 6) h + 24 else h
        
        // Si no se especifica fecha, se asume hoy
        if (targetDate.isNullOrEmpty() && slotScale <= currentTimeScale) return SlotStatus.PASADO

        // 4. De lo contrario, está libre
        return SlotStatus.LIBRE
    }
}
