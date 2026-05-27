package com.example.arena.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.View.ui.login.states.RegisterState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.internal.wait


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {


    fun registerUsuario(name: String, email: String, password: String) {
        var uiState = when {
            name.isBlank() || email.isBlank() || password.isBlank() -> {
                RegisterState.Error("Los campos no pueden estar vacios")
            }

            password.length < 6 -> {
                RegisterState.Error("La contraseña debe tener al menos 6 caracteres")
            }

            else -> {
                RegisterState.loading
            }
        }

        if (uiState is RegisterState.Error) return


        viewModelScope.launch {

            try {
                delay(2000)
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val profileUpdate = userProfileChangeRequest {
                    displayName = name
                }

                authResult.user?.updateProfile(profileUpdate)?.wait()
                uiState = RegisterState.success
            } catch (e: Exception) {
                uiState = RegisterState.Error(e.message ?: "Registro fallido")
            }
        }


    }


}