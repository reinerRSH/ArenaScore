package com.example.arena.domain

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val lastName: String,
    val phone : String = "",
    val registrationDate: Long = 0L
)