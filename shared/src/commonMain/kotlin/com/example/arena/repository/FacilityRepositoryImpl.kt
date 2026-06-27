package com.example.arena.repository

import com.example.arena.domain.Cancha
import com.example.arena.domain.Reserva
import com.example.arena.domain.Sede
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FacilityRepositoryImpl(
    private val firestore: FirebaseFirestore = Firebase.firestore
) : FacilityRepository {

    override fun observeReservasByCanchaYFecha(canchaId: String, fecha: String): Flow<List<Reserva>> {
        return firestore.collection("reservas")
            .where { "canchaid" equalTo canchaId }
            .where { "fecha" equalTo fecha }
            .snapshots
            .map { snapshot -> snapshot.documents.map { it.data() } }
    }

    override suspend fun crearReserva(reserva: Reserva): Result<Unit> {
        return try {
            firestore.collection("reservas").add(reserva)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFacilities(role: String, authorizedSedes: List<String>, sportType: String): List<Sede> {
        return try {
            var query = firestore.collection("sede").where { "tipo" equalTo sportType }
            
            // Note: Dev.GitLive has a different syntax for 'whereIn' or similar depending on version
            // We'll use a simpler filter or post-process for now if it's complex in this version
            val snapshot = query.get()
            val allFacilities = snapshot.documents.map { it.data<Sede>() }
            
            if (role == "STAFF" && authorizedSedes.isNotEmpty()) {
                allFacilities.filter { authorizedSedes.contains(it.id) }
            } else {
                allFacilities
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getSedeById(sedeId: String): Sede? {
        return try {
            val doc = firestore.collection("sede").document(sedeId).get()
            doc.data()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCanchasBySede(sedeId: String): List<Cancha> {
        return try {
            val snapshot = firestore.collection("canchas")
                .where { "sedeid" equalTo sedeId }
                .get()
            snapshot.documents.map { it.data() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getReservasByCanchaYFecha(canchaId: String, fecha: String): List<Reserva> {
        return try {
            val snapshot = firestore.collection("reservas")
                .where { "canchaid" equalTo canchaId }
                .where { "fecha" equalTo fecha }
                .get()
            snapshot.documents.map { it.data() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun actualizarEstadoReserva(reservaId: String, nuevoEstado: String): Result<Unit> {
        return try {
            firestore.collection("reservas").document(reservaId).update("estado" to nuevoEstado)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarEstadoCancha(canchaId: String, nuevoEstado: String): Result<Unit> {
        return try {
            firestore.collection("canchas").document(canchaId).update("estado" to nuevoEstado)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPagosPendientes(sedeId: String): List<Reserva> {
        return try {
            val snapshot = firestore.collection("reservas")
                .where { "sedeid" equalTo sedeId }
                .where { "estado" equalTo Reserva.STATUS_PENDIENTE }
                .get()
            snapshot.documents.map { it.data() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun actualizarConfiguracionSede(sedeId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            // Dev.GitLive update accepts pairs or a map depending on version
            firestore.collection("sede").document(sedeId).update(updates)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReservasActivas(): List<Reserva> {
        return try {
            val snapshot = firestore.collection("reservas")
                .where { "estado" equalTo Reserva.STATUS_ACTIVA }
                .get()
            snapshot.documents.map { it.data() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}