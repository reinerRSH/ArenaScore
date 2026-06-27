package com.example.arena.domain

import kotlinx.serialization.Serializable

@Serializable
data class Cancha(
    val id: String = "",
    val nombre: String = "",
    val sedeid: String = "",
    val estado: String = "",
    val tipo: String = ""
)