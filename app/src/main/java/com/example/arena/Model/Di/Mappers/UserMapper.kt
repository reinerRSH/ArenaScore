package com.example.arena.Model.Di.Mappers

import com.example.arena.Model.Di.Entitys.UserEntity
import com.example.arena.Model.Di.Domain.UserMapper


fun UserEntity.ToDomain(): UserMapper{
    return UserMapper(
        id = this.uid,
        email = this.email ?: "",
        lastName= this.lastName,
        name = this.name,
        role = this.role
    )
}


fun UserMapper.ToEntity(isRemebered: Boolean): UserEntity{

    return UserEntity(
        uid = this.id,
        email = this.email,
        name = this.name,
        lastName = this.lastName,
        role = this.role,
        isRemenbered = isRemebered
    )
}