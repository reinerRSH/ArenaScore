package com.example.arena.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.Model.Di.Daos.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@HiltViewModel
class NavigationViewModel @Inject constructor(private val userDao: UserDao) : ViewModel() {

    var starDestination: String? by mutableStateOf(null)
        private set

    init {
        checkAutoLogin()
    }

    private fun checkAutoLogin() {

        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                userDao.getRemenberedUser()
            }

            starDestination = if (user != null && user.isRemenbered) {
                "home_Screen" // Reemplaza "home_Screen" con la ruta real de tu pantalla de inicio
            } else {
                "login_Screen" // Reemplaza "login_Screen" con la ruta real de tu pantalla de inicio de sesión
            }
        }
    }
}
