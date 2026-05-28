package com.example.arena.Model.Di.Entitys

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String?,
    val name: String,
    val lastName: String,
    val role: String,
    val isRemenbered: Boolean

)
