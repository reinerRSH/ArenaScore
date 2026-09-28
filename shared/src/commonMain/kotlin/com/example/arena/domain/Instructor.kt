package com.example.arena.domain

import kotlinx.serialization.Serializable

@Serializable
data class Instructor(
    override val id: String = "",
    val nombre: String = "",
    val horario: List<String> = emptyList(),
    val sedeid: String = ""
) : ArenaModel {
    override fun withId(id: String) = this.copy(id = id)
}
