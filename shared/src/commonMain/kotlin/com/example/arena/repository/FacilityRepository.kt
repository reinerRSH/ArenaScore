package com.example.arena.repository

import com.example.arena.domain.Cancha
import com.example.arena.domain.Reserva
import com.example.arena.domain.Sede
import kotlinx.coroutines.flow.Flow

interface FacilityRepository {
    fun observeReservasByCanchaYFecha(canchaId: String, fecha: String): Flow<List<Reserva>>
    suspend fun crearReserva(reserva: Reserva): Result<Unit>
    suspend fun getFacilities(role: String, authorizedSedes: List<String>, sportType: String): List<Sede>
    suspend fun getSedeById(sedeId: String): Sede?
    suspend fun getCanchasBySede(sedeId: String): List<Cancha>
    suspend fun getReservasByCanchaYFecha(canchaId: String, fecha: String): List<Reserva>
    suspend fun actualizarEstadoReserva(reservaId: String, nuevoEstado: String): Result<Unit>
    suspend fun actualizarEstadoCancha(canchaId: String, nuevoEstado: String): Result<Unit>
    suspend fun getPagosPendientes(sedeId: String): List<Reserva>
    suspend fun actualizarConfiguracionSede(sedeId: String, updates: Map<String, Any>): Result<Unit>
    suspend fun getReservasActivas(): List<Reserva>
}