package com.example.arena.Model.Di.Domain

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Reserva(
    val canchaid: String = "",
    val estado: String = "RESERVADO",
    val fecha: String = "",
    val horaFin: String = "",
    val horaInicio: String = "",
    val sedeid: String = "",
    val usuarioid: String = ""
)
