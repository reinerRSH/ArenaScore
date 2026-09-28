package com.example.arena.domain

import androidx.compose.ui.graphics.Color

enum class SlotStatus {
    LIBRE,
    OCUPADO,            // Reserva de usuario o manual (ROJO)
    OCUPADO_INSTRUCTOR, // Clase asignada (DORADO/AMARILLO)
    PASADO,             // Tiempo ya transcurrido (ROJO)
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
        canchaId: String
    ): SlotStatus {
        // 1. Verificar si el tiempo ya pasó
        val parts = time.split(":")
        val h = parts[0].toInt()
        val slotScale = if (h < 6) h + 24 else h
        
        if (slotScale <= currentTimeScale) return SlotStatus.PASADO

        // 2. Verificar si hay una reserva activa (Usuario o Manual)
        val tieneReserva = reservas.any { 
            it.canchaid == canchaId && 
            it.horaInicio == time && 
            it.estado != Reserva.STATUS_CANCELADA 
        }
        if (tieneReserva) return SlotStatus.OCUPADO

        // 3. Verificar si el horario está asignado a un instructor (Clase)
        val esClase = instructores.any { 
            it.canchaid == canchaId && 
            it.horario.any { hSlot -> hSlot.endsWith(time) } 
        }
        if (esClase) return SlotStatus.OCUPADO_INSTRUCTOR

        // 4. De lo contrario, está libre
        return SlotStatus.LIBRE
    }
}
