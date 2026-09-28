package com.example.arena.Model.Di.Daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arena.Model.Di.Entitys.UserEntity


@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)


    @Query("SELECT * FROM user_table LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Query("DELETE FROM user_table")
    suspend fun deleteUser()

    @Query("SELECT * FROM user_table LIMIT 1")
    suspend fun getRememberedUser(): UserEntity?
}