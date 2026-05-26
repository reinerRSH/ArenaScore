package com.example.arena.ViewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.R
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


sealed interface LoginState{
    object Idle : LoginState
    object Loading: LoginState
    object Success: LoginState
    data class  Error(val message: String): LoginState
}
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val application: Application
) : ViewModel() {


    var uiState: LoginState by mutableStateOf(LoginState.Idle)
        private set

    fun loginUsuario(email: String, password: String){
        if (email.isBlank() || password.isBlank()){
            uiState = LoginState.Error(application.getString(R.string.Campo_vacio))
            return

        }

        uiState = LoginState.Loading

        viewModelScope.launch {
            delay(2000)

            try {
                auth.signInWithEmailAndPassword(email, password).await()

                uiState = LoginState.Success
            }catch (e: Exception){
                uiState = LoginState.Error(e.message ?: application.getString(R.string.Autenticacion_fallida))
            }
        }
    }


fun resetLoginState(){
    uiState = LoginState.Idle
}



}