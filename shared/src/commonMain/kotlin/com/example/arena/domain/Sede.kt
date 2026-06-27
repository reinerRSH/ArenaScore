package com.example.arena.domain

import kotlinx.serialization.Serializable

@Serializable
data class Sede(
    val id: String = "",
    val nombreSede: String = "",
    val ubicacion: String = "",
    val tipo: String = "",
    val courtsAvailable: Int = 0,
    val starRating: Double = 0.0,
    val imageUrl: String = "",
    val suscripcionStatus: String = "",
    val totalCanchas: Int = 0,
    val tags: List<String> = emptyList(),
    val extras: Map<String, Double> = emptyMap(),
    val precioBase: Double = 45.0,
    val datosPago: Map<String, String> = emptyMap()
)