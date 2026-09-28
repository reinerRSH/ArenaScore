package com.example.arena.Model.Di.Mappers

import com.example.arena.Model.Di.Entitys.UserEntity
import com.example.arena.domain.User


fun UserEntity.ToDomain(): User {
    return User(
        id = this.uid,
        email = this.email ?: "",
        lastName= this.lastName,
        name = this.name,
        role = this.role
    )
}


fun User.ToEntity(isRemembered: Boolean): UserEntity {

    return UserEntity(
        uid = this.id,
        email = this.email,
        name = this.name,
        lastName = this.lastName,
        role = this.role,
        isRemembered = isRemembered
    )
}