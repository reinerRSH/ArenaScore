package com.example.arena.ui.auth.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.Model.Di.Daos.UserDao
import com.example.arena.Model.Di.Domain.UserMapper
import com.example.arena.Model.Di.Mappers.ToEntity
import com.example.arena.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject




sealed interface LoginState{
    data object Idle : LoginState
    data object Loading: LoginState
    data object Success: LoginState
    data class  Error(val message: String): LoginState
}
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context,
    private val userDao: UserDao

) : ViewModel() {


    var uiState: LoginState by mutableStateOf(LoginState.Idle)
        private set

    init {
        checkActiveSession()
    }

    private fun checkActiveSession() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Podríamos validar si el usuario está en la DB local si quisiéramos persistencia total
            uiState = LoginState.Success
        }
    }

    fun loginUsuario(email: String, password: String, rememberMe: Boolean){
        if (email.isBlank() || password.isBlank()){
            uiState = LoginState.Error(context.getString(R.string.campo_vacio))
            return
        }

        uiState = LoginState.Loading

        viewModelScope.launch {
            try {
                delay(2000)
                // 🎯 El métod o signInWithEmailAndPassword de Firebase se encarga de verificar
                // que el email y la contraseña coincidan con los datos registrados.
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    // Si el login es exitoso en Firebase, mapeamos los datos básicos
                    // NOTA: Para obtener name/lastName reales, se debería consultar Firestore o Realtime DB
                    val loggedUser = UserMapper(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: email,
                        name = firebaseUser.displayName ?: "Athlete",
                        lastName = "",
                        role = "ATHLETE",
                    )

                    withContext(Dispatchers.IO) {
                        if (rememberMe) {
                            // Guardamos localmente solo si "Remember Me" está activo
                            userDao.insertUser(loggedUser.ToEntity(isRemebered = true))
                        } else {
                            // Si no, nos aseguramos de que no haya basura de sesiones anteriores
                            userDao.deleteUser()
                        }
                    }
                    uiState = LoginState.Success
                } else {
                    uiState = LoginState.Error(context.getString(R.string.autenticacion_fallida))
                }
            } catch (e: Exception) {
                // Si Firebase devuelve error (ej. credenciales incorrectas), lo capturamos aquí
                uiState = LoginState.Error(e.localizedMessage ?: context.getString(R.string.autenticacion_fallida))
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        uiState = LoginState.Loading

        viewModelScope.launch {
            try {
                delay(2000)
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    val displayName = firebaseUser.displayName ?: "Arena Athlete"
                    val nameParts = displayName.split(" ", limit = 2)
                    val firstName = nameParts.getOrNull(0) ?: displayName
                    val lastName = nameParts.getOrNull(1) ?: ""

                    val loggedUser = UserMapper(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        name = firstName.uppercase(),
                        lastName = lastName.uppercase(),
                        role = "ATHLETE",
                    )

                    withContext(Dispatchers.IO) {
                        // Por defecto con Google solemos recordar al usuario
                        userDao.insertUser(loggedUser.ToEntity(isRemebered = true))
                    }
                    uiState = LoginState.Success
                } else {
                    uiState = LoginState.Error("Error al obtener el usuario de Google")
                }
            } catch (e: Exception) {
                uiState = LoginState.Error(e.localizedMessage ?: "Error en la autenticación con Google")
            }
        }
    }


fun resetLoginState(){
    uiState = LoginState.Idle
}



}