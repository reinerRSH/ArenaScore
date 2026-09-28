package com.example.arena.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Sede(
    override val id: String = "",
    @SerialName("nombreSede")
    val nombreSede: String = "",
    @SerialName("ubicacion")
    val ubicacion: String = "",
    @SerialName("tipo")
    val tipo: String = "",
    @SerialName("canchasDisponibles")
    val courtsAvailable: Int = 0,
    @SerialName("calificacion")
    val starRating: Double = 0.0,
    @SerialName("imageUrl")
    val imageUrl: String = "",
    @SerialName("suscripcionStatus")
    val suscripcionStatus: String = "",
    @SerialName("totalCanchas")
    val totalCanchas: Int = 0,
    @SerialName("tags")
    val tags: List<String> = emptyList(),
    @SerialName("extras")
    val extras: Map<String, Double> = emptyMap(),
    @SerialName("precioBase")
    val precioBase: Double = 45.0,
    @SerialName("datosPago")
    val datosPago: Map<String, String> = emptyMap(),
    @SerialName("horaApertura")
    val horaApertura: String = "06:00",
    @SerialName("horaCierre")
    val horaCierre: String = "01:00",
    @SerialName("servicios")
    val servicios: List<String> = emptyList()
) : ArenaModel {
    override fun withId(id: String) = this.copy(id = id)
}