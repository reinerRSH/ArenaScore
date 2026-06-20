package com.example.arena.Model.Di.Domain

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Cancha(
    @DocumentId
    var id: String = "",

    @get:PropertyName("nombre")
    @set:PropertyName("nombre")
    var nombre: String = "",

    @get:PropertyName("sedeid")
    @set:PropertyName("sedeid")
    var sedeid: String = "",

    @get:PropertyName("estado")
    @set:PropertyName("estado")
    var estado: String = "", // "ACTIVA", "RESERVADA", etc

    @get:PropertyName("tipo")
    @set:PropertyName("tipo")
    var tipo: String = "" // "DENTRO", "FUERA"
)
