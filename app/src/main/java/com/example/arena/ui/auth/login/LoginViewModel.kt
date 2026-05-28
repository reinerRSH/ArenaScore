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

    fun loginUsuario(email: String, password: String, rememberMe: Boolean){
        if (email.isBlank() || password.isBlank()){
            uiState = LoginState.Error(context.getString(R.string.campo_vacio))
            return

        }

        uiState = LoginState.Loading

        viewModelScope.launch {

            try {
                delay(2000)
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                when (firebaseUser) {
                    null -> uiState = LoginState.Error(context.getString(R.string.autenticacion_fallida))
                    else -> {
                        val loggedUser = UserMapper(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: email,
                            name = "Athlete",
                            lastName = "Athlete",
                            role = "ATHLETE",
                        )

                        withContext(Dispatchers.IO) {
                            when (rememberMe) {
                                true -> userDao.insertUser(loggedUser.ToEntity(isRemebered = true))
                                false -> userDao.deleteUser()
                            }
                        }
                        uiState = LoginState.Success
                    }
                }
            } catch (e: Exception) {
                uiState = LoginState.Error(e.message ?: context.getString(R.string.autenticacion_fallida))
            }
        }
    }


fun resetLoginState(){
    uiState = LoginState.Idle
}



}