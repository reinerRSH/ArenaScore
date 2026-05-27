package com.example.arena.ViewModel

import android.app.Application
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
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
    private val application: Application,
    private val userDao: UserDao

) : ViewModel() {


    var uiState: LoginState by mutableStateOf(LoginState.Idle)
        private set

    fun loginUsuario(email: String, password: String, rememberMe: Boolean){
        if (email.isBlank() || password.isBlank()){
            uiState = LoginState.Error(application.getString(R.string.Campo_vacio))
            return

        }

        uiState = LoginState.Loading

        viewModelScope.launch {

            try {
                delay(2000)
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                when (firebaseUser){
                    null -> uiState = LoginState.Error(application.getString(R.string.Autenticacion_fallida))
                    else ->{
                        val loggedUser = UserMapper(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: email,
                            name = "Athlete",
                            role = "ATHLETE"
                        )

                        withContext(Dispatchers.IO){

                            when(rememberMe){
                                true -> userDao.insertUser(loggedUser.ToEntity(isRemebered = true))
                                false -> userDao.deleteUser()
                            }


                        }
                        uiState = LoginState.Success
                    }
                }



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