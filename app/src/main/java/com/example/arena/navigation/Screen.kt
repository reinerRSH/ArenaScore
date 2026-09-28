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

    @Serializable
    data class SelectGymClass(val sedeId: String, val sedeName: String = "Centro de Entrenamiento") : Screen

    @Serializable
    data class BookingType(val sedeId: String, val canchaId: String, val canchaName: String = "") : Screen

    @Serializable
    data class BookingSchedule(val sedeId: String, val canchaId: String, val tipoReserva: String, val canchaName: String = "") : Screen

    @Serializable
    data class GymBookingSchedule(val sedeId: String, val classId: String, val className: String, val tokenCost: Int = 1) : Screen

    @Serializable
    data class AdminDashboard(val sedeId: String, val initialTab: Int = 0) : Screen

    @Serializable
    object Profile : Screen

    @Serializable
    object MyReservations : Screen

    @Serializable
    object StaffSetup : Screen
}
