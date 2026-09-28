package com.example.arena.repository

import com.example.arena.domain.Cancha
import com.example.arena.domain.Reserva
import com.example.arena.domain.Sede
import kotlinx.coroutines.flow.Flow

interface FacilityRepository {
    fun observeReservasByCanchaYFecha(canchaId: String, fecha: String): Flow<List<Reserva>>
    fun observeReservasPorSede(sedeId: String): Flow<List<Reserva>>
    fun observeReservasPorSedeYFecha(sedeId: String, fecha: String): Flow<List<Reserva>>
    fun observeReservasByInstructorYFecha(instructorId: String, fecha: String): Flow<List<Reserva>>
    suspend fun crearReserva(reserva: Reserva): Result<Unit>
    suspend fun getFacilities(role: String, authorizedSedes: List<String>, sportType: String): List<Sede>
    fun observeFacilities(role: String, authorizedSedes: List<String>, sportType: String): Flow<List<Sede>>
    suspend fun getSedeById(sedeId: String): Sede?
    fun observeSedeById(sedeId: String): Flow<Sede?>
    suspend fun getCanchasBySede(sedeId: String): List<Cancha>
    fun observeCanchasBySede(sedeId: String): Flow<List<Cancha>>
    suspend fun getReservasByCanchaYFecha(canchaId: String, fecha: String): List<Reserva>
    suspend fun actualizarEstadoReserva(reservaId: String, nuevoEstado: String): Result<Unit>
    suspend fun actualizarEstadoCancha(canchaId: String, nuevoEstado: String): Result<Unit>
    suspend fun getPagosPendientes(sedeId: String): List<Reserva>
    fun observePagosPendientes(sedeId: String): Flow<List<Reserva>>
    suspend fun getReservasByUsuario(usuarioId: String): List<Reserva>
    suspend fun eliminarReserva(reservaId: String): Result<Unit>
    suspend fun actualizarConfiguracionSede(sedeId: String, updates: Map<String, Any>): Result<Unit>
    suspend fun actualizarCancha(canchaId: String, updates: Map<String, Any>): Result<Unit>
    suspend fun getReservasActivas(): List<Reserva>
    suspend fun getCanchaById(canchaId: String): Cancha?
    suspend fun esStaff(uid: String): Boolean

    // Nuevas funciones para estructura anidada
    fun observeInstructoresBySede(sedeId: String): Flow<List<com.example.arena.domain.Instructor>>
    suspend fun actualizarDatosPago(sedeId: String, datos: Map<String, String>): Result<Unit>
    suspend fun upsertInstructor(sedeId: String, instructor: com.example.arena.domain.Instructor): Result<Unit>
    suspend fun eliminarInstructor(sedeId: String, instructorId: String): Result<Unit>
}