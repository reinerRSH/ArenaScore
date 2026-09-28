package com.example.arena.repository

import com.example.arena.Model.Di.Daos.UserDao
import com.example.arena.Model.Di.Entitys.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val db: FirebaseFirestore
) {
    suspend fun getCurrentUser(): UserEntity? {
        return userDao.getUSer()
    }

    suspend fun updateUserProfile(uid: String, name: String, lastName: String, phone: String, imageUrl: String) {
        // Update Firestore
        val userUpdates = mapOf(
            "name" to name,
            "lastName" to lastName,
            "phone" to phone,
            "imageUrl" to imageUrl
        )
        db.collection("users").document(uid).update(userUpdates).await()

        // Update local Room database
        val currentUser = userDao.getUSer()
        if (currentUser != null && currentUser.uid == uid) {
            val updatedUser = currentUser.copy(
                name = name,
                lastName = lastName,
                phone = phone,
                imageUrl = imageUrl
            )
            userDao.insertUser(updatedUser)
        }
    }
}
