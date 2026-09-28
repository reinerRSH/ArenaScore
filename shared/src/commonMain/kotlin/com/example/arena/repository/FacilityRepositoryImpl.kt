package com.example.arena.repository

import com.example.arena.domain.Cancha
import com.example.arena.domain.Reserva
import com.example.arena.domain.Sede
import com.example.arena.util.toModel
import com.example.arena.util.toModels
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
            .map { it.documents.toModels() }
    }

    override fun observeReservasPorSede(sedeId: String): Flow<List<Reserva>> {
        return firestore.collection("reservas")
            .where { "sedeid" equalTo sedeId }
            .snapshots
            .map { it.documents.toModels() }
    }

    override fun observeReservasPorSedeYFecha(sedeId: String, fecha: String): Flow<List<Reserva>> {
        return firestore.collection("reservas")
            .where { "sedeid" equalTo sedeId }
            .where { "fecha" equalTo fecha }
            .snapshots
            .map { it.documents.toModels() }
    }

    override fun observeReservasByInstructorYFecha(instructorId: String, fecha: String): Flow<List<Reserva>> {
        return firestore.collection("reservas")
            .where { "instructorId" equalTo instructorId }
            .where { "fecha" equalTo fecha }
            .snapshots
            .map { it.documents.toModels() }
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
            val query = firestore.collection("sede").where { "tipo" equalTo sportType }
            val snapshot = query.get()
            
            val allFacilities: List<Sede> = snapshot.documents.toModels()
            
            if (role == "STAFF") {
                if (authorizedSedes.isEmpty()) emptyList() 
                else allFacilities.filter { authorizedSedes.contains(it.id) }
            } else {
                allFacilities
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun observeFacilities(role: String, authorizedSedes: List<String>, sportType: String): Flow<List<Sede>> {
        return firestore.collection("sede")
            .where { "tipo" equalTo sportType }
            .snapshots
            .map { snapshot ->
                val all: List<Sede> = snapshot.documents.toModels()
                if (role == "STAFF") {
                    if (authorizedSedes.isEmpty()) emptyList()
                    else all.filter { authorizedSedes.contains(it.id) }
                } else {
                    all
                }
            }
    }

    override suspend fun getSedeById(sedeId: String): Sede? {
        return try {
            val doc = firestore.collection("sede").document(sedeId).get()
            doc.toModel()
        } catch (e: Exception) {
            null
        }
    }

    override fun observeSedeById(sedeId: String): Flow<Sede?> {
        return firestore.collection("sede").document(sedeId)
            .snapshots
            .map { it.toModel<Sede>() }
    }

    override suspend fun getCanchasBySede(sedeId: String): List<Cancha> {
        return try {
            val snapshot = firestore.collection("canchas")
                .where { "sedeid" equalTo sedeId }
                .get()
            snapshot.documents.toModels()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun observeCanchasBySede(sedeId: String): Flow<List<Cancha>> {
        return firestore.collection("canchas")
            .where { "sedeid" equalTo sedeId }
            .snapshots
            .map { it.documents.toModels() }
    }

    override suspend fun getReservasByCanchaYFecha(canchaId: String, fecha: String): List<Reserva> {
        return try {
            val snapshot = firestore.collection("reservas")
                .where { "canchaid" equalTo canchaId }
                .where { "fecha" equalTo fecha }
                .get()
            snapshot.documents.toModels()
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
            snapshot.documents.toModels()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun observePagosPendientes(sedeId: String): Flow<List<Reserva>> {
        return firestore.collection("reservas")
            .where { "sedeid" equalTo sedeId }
            .where { "estado" equalTo Reserva.STATUS_PENDIENTE }
            .snapshots
            .map { it.documents.toModels() }
    }

    override suspend fun getReservasByUsuario(usuarioId: String): List<Reserva> {
        return try {
            val snapshot = firestore.collection("reservas")
                .where { "usuarioid" equalTo usuarioId }
                .get()
            snapshot.documents.toModels()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun eliminarReserva(reservaId: String): Result<Unit> {
        return try {
            firestore.collection("reservas").document(reservaId).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarConfiguracionSede(sedeId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val docRef = firestore.collection("sede").document(sedeId)
            docRef.update(updates)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarCancha(canchaId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val docRef = firestore.collection("canchas").document(canchaId)
            // Gitlive Firebase supports passing a Map directly to update
            docRef.update(updates)
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
            snapshot.documents.toModels()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCanchaById(canchaId: String): Cancha? {
        return try {
            val doc = firestore.collection("canchas").document(canchaId).get()
            doc.toModel()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun esStaff(uid: String): Boolean {
        return try {
            val doc = firestore.collection("staff").document(uid).get()
            doc.exists && (doc.get<Boolean>("isActive") ?: false)
        } catch (e: Exception) {
            false
        }
    }

    override fun observeInstructoresBySede(sedeId: String): Flow<List<com.example.arena.domain.Instructor>> {
        return firestore.collection("sede")
            .document(sedeId)
            .collection("instructores")
            .snapshots
            .map { it.documents.toModels() }
    }

    override suspend fun actualizarDatosPago(sedeId: String, datos: Map<String, String>): Result<Unit> {
        return try {
            firestore.collection("sede").document(sedeId).update("datosPago" to datos)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun upsertInstructor(sedeId: String, instructor: com.example.arena.domain.Instructor): Result<Unit> {
        return try {
            val collection = firestore.collection("sede").document(sedeId).collection("instructores")
            if (instructor.id.isEmpty()) {
                collection.add(instructor)
            } else {
                collection.document(instructor.id).set(instructor)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarInstructor(sedeId: String, instructorId: String): Result<Unit> {
        return try {
            firestore.collection("sede").document(sedeId).collection("instructores").document(instructorId).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}