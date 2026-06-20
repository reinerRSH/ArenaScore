package com.example.arena.Model.Di.Data

import android.util.Log
import com.example.arena.Model.Di.Domain.Cancha
import com.example.arena.Model.Di.Domain.Sede
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FacilityRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    /**
     * Obtiene las sedes filtradas por tipo de deporte y permisos de Staff si aplica.
     */
    suspend fun getFacilities(
        role: String,
        authorizedSedes: List<String>,
        sportType: String
    ): List<Sede> {
        return try {
            Log.d("FacilityRepository", "AUDITORÍA | Rol recibido: $role")
            Log.d("FacilityRepository", "AUDITORÍA | Deporte: $sportType")
            
            var query = db.collection("sede")
                .whereEqualTo("tipo", sportType)

            // Lógica de filtrado: Atletas ven todo lo público, Staff solo lo autorizado
            if (role == "STAFF") {
                Log.d("FacilityRepository", "AUDITORÍA | Lógica STAFF activada. Sedes autorizadas: $authorizedSedes")
                if (authorizedSedes.isEmpty()) {
                    Log.w("FacilityRepository", "AUDITORÍA | ERROR: Staff sin sedes autorizadas en Firestore")
                    return emptyList()
                }
                query = query.whereIn(FieldPath.documentId(), authorizedSedes)
            } else {
                Log.d("FacilityRepository", "AUDITORÍA | Lógica PÚBLICA (Athlete/User) activada. Sin filtros adicionales.")
            }

            Log.d("FacilityRepository", "AUDITORÍA | Query Firestore: ${query.toString()}")

            val snapshot = query.get().await()
            val facilities = snapshot.toObjects(Sede::class.java)
            
            if (facilities.isEmpty()) {
                Log.w("FacilityRepository", "AUDITORÍA | RESULTADO: La consulta no devolvió documentos. Verifica que existan sedes con tipo='$sportType' en Firestore.")
            } else {
                Log.d("FacilityRepository", "AUDITORÍA | RESULTADO: Se encontraron ${facilities.size} sedes.")
                facilities.forEach { Log.d("FacilityRepository", "AUDITORÍA | Sede encontrada: ${it.nombreSede} (ID: ${it.id})") }
            }
            
            facilities
        } catch (e: Exception) {
            Log.e("FacilityRepository", "AUDITORÍA | EXCEPCIÓN: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Obtiene una sede específica por su ID.
     */
    suspend fun getSedeById(sedeId: String): Sede? {
        return try {
            val doc = db.collection("sede").document(sedeId).get().await()
            doc.toObject(Sede::class.java)
        } catch (e: Exception) {
            Log.e("FacilityRepository", "Error al obtener sede $sedeId: ${e.message}")
            null
        }
    }

    /**
     * Obtiene todas las canchas asociadas a una sede.
     */
    suspend fun getCanchasBySede(sedeId: String): List<Cancha> {
        return try {
            val snapshot = db.collection("canchas")
                .whereEqualTo("sedeid", sedeId)
                .get()
                .await()
            snapshot.toObjects(Cancha::class.java)
        } catch (e: Exception) {
            Log.e("FacilityRepository", "Error al obtener canchas de sede $sedeId: ${e.message}")
            emptyList()
        }
    }

    /**
     * Obtiene las reservas para una fecha específica (Estructura base).
     */
    suspend fun getReservasByFecha(sedeId: String, fecha: String): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection("reservas")
                .whereEqualTo("sedeid", sedeId)
                .whereEqualTo("fecha", fecha)
                .get()
                .await()
            snapshot.documents.map { it.data ?: emptyMap() }
        } catch (e: Exception) {
            Log.e("FacilityRepository", "Error al obtener reservas: ${e.message}")
            emptyList()
        }
    }
}
