package com.example.arena.ui.court

enum class CourtStatus {
    AVAILABLE,
    RESERVED,
    BUSY,
    MAINTENANCE
}

data class Court(
    val id: String,
    val name: String,
    val type: String, // e.g., "INDOOR PANORAMIC"
    val isAvailable: Boolean,
    val status: CourtStatus,
    val idTechnical: String,
    val duration: String = "90 MIN",
    val imageUrl: String = ""
)
