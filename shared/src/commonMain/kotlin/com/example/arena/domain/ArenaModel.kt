package com.example.arena.domain

/**
 * Interfaz base para automatizar el mapeo de IDs de Firestore.
 */
interface ArenaModel {
    val id: String
    fun withId(id: String): ArenaModel
}