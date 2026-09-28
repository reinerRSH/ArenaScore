package com.example.arena.ui.gym

data class GymClass(
    val id: String,
    val name: String,
    val category: String,
    val coachName: String,
    val scheduleTime: String,
    val maxCapacity: Int,
    val currentEnrolled: Int,
    val tokenCost: Int = 1,
    val imageUrl: String,
    val intensityLevel: String, // "ALTA", "MODERADA", "EXTREMA"
    val description: String = ""
)

val mockGymClasses = listOf(
    GymClass(
        id = "class_1",
        name = "Crossfit WOD High-Octane",
        category = "WOD",
        coachName = "Coach Marcos Silva",
        scheduleTime = "07:00 AM - 08:00 AM",
        maxCapacity = 15,
        currentEnrolled = 11,
        tokenCost = 1,
        imageUrl = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600",
        intensityLevel = "EXTREMA",
        description = "Entrenamiento del día de alta intensidad combinando halterofilia, calistenia y trabajo cardiovascular."
    ),
    GymClass(
        id = "class_2",
        name = "Powerlifting & Heavy Strength",
        category = "FUERZA",
        coachName = "Coach Elena Rostova",
        scheduleTime = "09:00 AM - 10:15 AM",
        maxCapacity = 12,
        currentEnrolled = 8,
        tokenCost = 1,
        imageUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=600",
        intensityLevel = "ALTA",
        description = "Desarrollo de fuerza máxima enfocado en sentadilla, press de banca y peso muerto con técnica biomecánica."
    ),
    GymClass(
        id = "class_3",
        name = "HIIT Conditioning Nitro",
        category = "HIIT",
        coachName = "Coach Carlos Méndez",
        scheduleTime = "05:00 PM - 06:00 PM",
        maxCapacity = 20,
        currentEnrolled = 16,
        tokenCost = 1,
        imageUrl = "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=600",
        intensityLevel = "ALTA",
        description = "Intervalos de alta intensidad para quema metabólica acelerada y acondicionamiento aeróbico."
    ),
    GymClass(
        id = "class_4",
        name = "Spinning Cardio Rhythm",
        category = "CARDIO",
        coachName = "Coach Valeria B.",
        scheduleTime = "06:30 PM - 07:30 PM",
        maxCapacity = 18,
        currentEnrolled = 14,
        tokenCost = 1,
        imageUrl = "https://images.unsplash.com/photo-1518310383802-640c2de311b2?w=600",
        intensityLevel = "MODERADA",
        description = "Sesión de ciclismo de interior guiada por música y picos de frecuencia cardíaca objetivo."
    ),
    GymClass(
        id = "class_5",
        name = "Calisthenics & Body Control",
        category = "CALISTENIA",
        coachName = "Coach Yordan K.",
        scheduleTime = "07:30 PM - 08:30 PM",
        maxCapacity = 15,
        currentEnrolled = 9,
        tokenCost = 1,
        imageUrl = "https://images.unsplash.com/photo-1599058945522-28d584b6f0ff?w=600",
        intensityLevel = "ALTA",
        description = "Dominio del peso corporal, barras fijas, elementos gimnásticos y fuerza de agarre."
    )
)
