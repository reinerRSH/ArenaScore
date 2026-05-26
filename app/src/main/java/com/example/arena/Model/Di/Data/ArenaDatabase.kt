package com.example.arena.Model.Di.Data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.arena.Model.Di.Daos.UserDao
import com.example.arena.Model.Di.Entitys.UserEntity


@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class ArenaDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao

}