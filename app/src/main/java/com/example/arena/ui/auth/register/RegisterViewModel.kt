package com.example.arena.ui.auth.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.Model.Di.Daos.UserDao
import com.example.arena.Model.Di.Entitys.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userDao: UserDao
) : ViewModel() {


    var uiState: RegisterState by mutableStateOf<RegisterState>(RegisterState.idle)
        private set

    fun setError(message: String) {
        uiState = RegisterState.Error(message)
    }


    fun registerUsuario(name: String, lastName: String, email: String, password: String) {
        val validationState = when {
            name.isBlank() || email.isBlank() || password.isBlank() -> {
                RegisterState.Error("Los campos no pueden estar vacíos")
            }

            password.length < 6 -> {
                RegisterState.Error("La contraseña debe tener al menos 6 caracteres")
            }

            else -> null
        }

        if (validationState != null) {
            uiState = validationState
            return
        }

        uiState = RegisterState.loading

        viewModelScope.launch {
            try {
                delay(2000)
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid =
                    authResult.user?.uid ?: throw Exception("Incapaz de obtener el UID del usuario")
                
                val fullName = "$name $lastName".trim()
                val profileUpdate = userProfileChangeRequest {
                    displayName = fullName
                }

                authResult.user?.updateProfile(profileUpdate)?.await()

                withContext(Dispatchers.IO) {
                    val athleteEntity = UserEntity(
                        uid = uid,
                        name = name,
                        lastName = lastName,
                        email = email,
                        role = "ATHLETE",
                        isRemenbered = true
                    )

                    userDao.insertUser(athleteEntity)
                }
                uiState = RegisterState.success
            } catch (e: Exception) {
                uiState = RegisterState.Error(e.localizedMessage ?: "Registro fallido")
            }
        }
    }

    fun registerWithGoogle(idToken: String) {
        uiState = RegisterState.loading

        viewModelScope.launch {
            try {
                delay(2000)
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser =
                    authResult.user ?: throw Exception("La autenticación de Google no devolvió un usuario válido.")

                val displayName = firebaseUser.displayName ?: "Atleta de Arena"
                val nameParts = displayName.split(" ", limit = 2)
                val firstName = nameParts.getOrNull(0) ?: displayName
                val lastName = nameParts.getOrNull(1) ?: ""


                withContext(Dispatchers.IO) {
                    val athleteEntity = UserEntity(
                        uid = firebaseUser.uid,
                        name = firstName.uppercase(),
                        lastName = lastName.uppercase(),
                        email = firebaseUser.email,
                        role = "ATHLETE",
                        isRemenbered = true
                        )

                    userDao.insertUser(athleteEntity)
                }

                uiState = RegisterState.success


            } catch (e: Exception) {
                uiState = RegisterState.Error(e.localizedMessage ?: "Error en la autenticación con Google")
            }
        }

    }


}