package com.example.arena.Model.Di.Domain

import android.util.Log

data class UserMapper(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val lastName: String,
    val phone : String = "",
    val registrationDate: Long = System.currentTimeMillis()
)
