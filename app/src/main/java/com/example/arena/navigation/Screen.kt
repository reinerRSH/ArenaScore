package com.example.arena.navigation

import kotlinx.serialization.Serializable

/**
 * Definición de rutas Type-Safe para la navegación de la app.
 */
sealed interface Screen {
    
    @Serializable
    object Login : Screen

    @Serializable
    object Register : Screen

    @Serializable
    object Home : Screen

    @Serializable
    object ForgotPassword : Screen

    @Serializable
    data class FacilityList(val sport: String) : Screen

    @Serializable
    data class SelectCourt(val sedeId: String) : Screen
}
