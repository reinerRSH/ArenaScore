package com.example.arena.domain

import kotlinx.serialization.Serializable

@Serializable
data class Cancha(
    override val id: String = "",
    val nombre: String = "",
    val sedeid: String = "",
    val estado: String = "",
    val tipo: String = "",
    val imageUrl: String = "",
    val patrocinador: String = ""
) : ArenaModel {
    override fun withId(id: String) = this.copy(id = id)
}