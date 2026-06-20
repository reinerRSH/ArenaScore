package com.example.arena.Model.Di.Domain

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Modelo de dominio para las sedes de Arena.
 */
data class Sede(
    @DocumentId
    var id: String = "",
    
    @get:PropertyName("nombreSede")
    @set:PropertyName("nombreSede")
    var nombreSede: String = "",
    
    @get:PropertyName("ubicacion")
    @set:PropertyName("ubicacion")
    var ubicacion: String = "",
    
    @get:PropertyName("tipo")
    @set:PropertyName("tipo")
    var tipo: String = "", // "PADEL" o "CROSSFIT"
    
    @get:PropertyName("canchasDisponibles")
    @set:PropertyName("canchasDisponibles")
    var courtsAvailable: Int = 0,
    
    @get:PropertyName("calificacion")
    @set:PropertyName("calificacion")
    var starRating: Double = 0.0,
    
    @get:PropertyName("imageUrl")
    @set:PropertyName("imageUrl")
    var imageUrl: String = "",
    
    @get:PropertyName("suscripcionStatus")
    @set:PropertyName("suscripcionStatus")
    var suscripcionStatus: String = "",

    @get:PropertyName("totalCanchas")
    @set:PropertyName("totalCanchas")
    var totalCanchas: Int = 0,

    @get:PropertyName("tags")
    @set:PropertyName("tags")
    var tags: List<String> = emptyList()
)
