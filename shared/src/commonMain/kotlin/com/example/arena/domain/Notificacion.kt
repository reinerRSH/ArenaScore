package com.example.arena.domain

import kotlinx.serialization.Serializable

@Serializable
data class Notificacion(
    override val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "INFO", // "RESERVA_CONFIRMADA", "PAGO_RECIBIDO", "INFO"
    val relatedId: String = "", // e.g., reservaId
    val isRead: Boolean = false,
    val createdAt: Long = 0L
) : ArenaModel {
    override fun withId(id: String): Notificacion = this.copy(id = id)
}
