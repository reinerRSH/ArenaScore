package com.example.arena.domain

import kotlinx.serialization.Serializable

@Serializable
data class Reserva(
    val id: String = "",
    val canchaid: String = "",
    val estado: String = "ACTIVA",
    val fecha: String = "",
    val horaFin: String = "",
    val horaInicio: String = "",
    val sedeid: String = "",
    val usuarioid: String = "",
    val extras: Map<String, Double> = emptyMap(),
    val referenciaPago: String = "",
    val montoTotal: Double = 0.0
) {
    companion object {
        const val STATUS_ACTIVA = "ACTIVA"
        const val STATUS_FINALIZADA = "FINALIZADA"
        const val STATUS_CANCELADA = "CANCELADA"
        const val STATUS_PENDIENTE = "PENDIENTE_VERIFICACION"
    }
}