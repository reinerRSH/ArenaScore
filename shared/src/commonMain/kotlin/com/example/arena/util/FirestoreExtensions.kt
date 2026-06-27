package com.example.arena.util

import com.example.arena.domain.ArenaModel
import dev.gitlive.firebase.firestore.DocumentSnapshot

/**
 * Automatiza la conversión de un snapshot a un modelo inyectando el ID del documento.
 */
inline fun <reified T : ArenaModel> DocumentSnapshot.toModel(): T {
    val model: T = this.data()
    @Suppress("UNCHECKED_CAST")
    return model.withId(this.id) as T
}

/**
 * Automatiza la conversión de una lista de documentos.
 */
inline fun <reified T : ArenaModel> List<DocumentSnapshot>.toModels(): List<T> {
    return this.map { it.toModel<T>() }
}