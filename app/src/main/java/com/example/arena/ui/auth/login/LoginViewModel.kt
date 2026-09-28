package com.example.arena.ui.auth.login

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arena.Model.Di.Daos.UserDao
import com.example.arena.domain.User
import com.example.arena.Model.Di.Mappers.ToEntity
import com.example.arena.R
import com.example.arena.util.staffDocument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Estados del flujo de autenticación.
 */
sealed interface LoginState {
    data object Idle : LoginState
    data object Loading : LoginState
    data object Success : LoginState
    data object StaffSuccess : LoginState
    data class Error(val message: String) : LoginState
}

/**
 * ViewModel que gestiona la lógica de inicio de sesión para Atletas y Personal (Staff).
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    @ApplicationContext private val context: Context,
    private val userDao: UserDao
) : ViewModel() {

    var uiState: LoginState by mutableStateOf(LoginState.Idle)
        private set

    init {
        checkActiveSession()
    }

    private fun checkActiveSession() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                userDao.getRememberedUser()
            }
            if (user != null && user.isRemembered) {
                saveFcmToken(user.uid, isStaff = (user.role == "STAFF"))
                if (user.role == "STAFF") {
                    uiState = LoginState.StaffSuccess
                } else {
                    uiState = LoginState.Success
                }
            } else if (auth.currentUser != null) {
                saveFcmToken(auth.currentUser!!.uid, isStaff = false)
                uiState = LoginState.Success
            }
        }
    }

    /**
     * Inicia sesión como Atleta usando Email y Contraseña.
     */
    fun loginUsuario(email: String, password: String, rememberMe: Boolean) {
        resetLoginState()
        if (email.isBlank() || password.isBlank()) {
            uiState = LoginState.Error(context.getString(R.string.campo_vacio))
            return
        }

        uiState = LoginState.Loading

        viewModelScope.launch {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    val loggedUser = User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: email,
                        name = firebaseUser.displayName ?: "Atleta",
                        lastName = "",
                        role = "ATHLETE",
                    )

                    withContext(Dispatchers.IO) {
                        userDao.deleteUser()
                        userDao.insertUser(loggedUser.ToEntity(isRemembered = rememberMe))
                    }
                    saveFcmToken(firebaseUser.uid, isStaff = false)
                    uiState = LoginState.Success
                } else {
                    uiState = LoginState.Error(context.getString(R.string.autenticacion_fallida))
                }
            } catch (e: Exception) {
                uiState = LoginState.Error(e.localizedMessage ?: context.getString(R.string.autenticacion_fallida))
            }
        }
    }

    /**
     * Inicia sesión con Google.
     */
    fun loginWithGoogle(idToken: String) {
        uiState = LoginState.Loading
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    val loggedUser = User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        name = (firebaseUser.displayName ?: "ATLETA").uppercase(),
                        lastName = "",
                        role = "ATHLETE",
                    )

                    withContext(Dispatchers.IO) {
                        userDao.deleteUser()
                        userDao.insertUser(loggedUser.ToEntity(isRemembered = true))
                    }
                    saveFcmToken(firebaseUser.uid, isStaff = false)
                    uiState = LoginState.Success
                }
            } catch (e: Exception) {
                uiState = LoginState.Error(e.localizedMessage ?: "Error con Google")
            }
        }
    }

    /**
     * Autentica a un miembro del Personal (Staff) validando su ID, Código y Rol en Firestore.
     */
    fun authenticateStaff(clearanceID: String, accessCode: String, onResult: (Boolean, String?) -> Unit) {
        resetLoginState()
        if (clearanceID.isBlank() || accessCode.isBlank()) {
            onResult(false, context.getString(R.string.campo_vacio))
            return
        }

        uiState = LoginState.Loading

        // Referencia directa al documento en la colección 'staff' (ej. staff/admin2026)
        db.collection("staff").document(clearanceID).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    uiState = LoginState.Error("ID de administrador no encontrado")
                    onResult(false, "ID no encontrado")
                    return@addOnSuccessListener
                }

                val isActive = document.getBoolean("isActive") ?: false
                val storeCode = document.getString("accessCode")
                val role = document.getString("role") ?: ""
                val expiryDate = document.getString("expireDate") ?: "2099-12-31"
                val today = java.time.LocalDate.now().toString()

                if (!isActive) {
                    uiState = LoginState.Error("Acceso de personal desactivado")
                    onResult(false, "Desactivado")
                } else if (role != "STAFF" && role != "ADMIN") {
                    uiState = LoginState.Error("El usuario no tiene rol de STAFF")
                    onResult(false, "Rol inválido")
                } else if (accessCode != storeCode) {
                    uiState = LoginState.Error("Código de acceso incorrecto")
                    onResult(false, "Código Incorrecto")
                } else if (expiryDate < today) {
                    uiState = LoginState.Error("Acceso expirado")
                    onResult(false, "Expirado")
                } else {
                    viewModelScope.launch {
                        // Creamos el objeto de usuario para la sesión local
                        val staffUser = User(
                            id = clearanceID, // admin2026
                            email = document.getString("email") ?: "staff@arena.com",
                            name = document.getString("nombre") ?: "ADMINISTRADOR",
                            lastName = "",
                            role = "STAFF"
                        )
                        
                        // Guardamos en Room para persistir la sesión
                        withContext(Dispatchers.IO) {
                            userDao.deleteUser()
                            userDao.insertUser(staffUser.ToEntity(isRemembered = true))
                        }
                        
                        saveFcmToken(clearanceID, isStaff = true)
                        uiState = LoginState.StaffSuccess
                        onResult(true, "Bienvenido al Centro de Mando")
                    }
                }
            }
            .addOnFailureListener { e ->
                uiState = LoginState.Error(e.localizedMessage ?: "Error de conexión con el servidor")
                onResult(false, "Error de red")
            }
    }

    /**
     * Valida si un ID de usuario corresponde a un Staff activo en Firestore.
     */
    suspend fun esStaff(uid: String): Boolean {
        return try {
            val doc = db.staffDocument(uid).get().await()
            doc.exists() && (doc.getBoolean("isActive") ?: false)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene el token FCM actual y lo guarda en el documento del usuario (staff o user).
     */
    private fun saveFcmToken(userId: String, isStaff: Boolean) {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                val collection = if (isStaff) "staff" else "user"
                db.collection(collection).document(userId)
                    .update("fcmToken", token)
                    .await()
                Log.d("LoginViewModel", "FCM Token guardado en $collection/$userId")
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error al guardar FCM Token", e)
            }
        }
    }

    fun resetLoginState() {
        uiState = LoginState.Idle
    }

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            onResult(false, "El correo no puede estar vacío")
            return
        }
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Enlace enviado")
                } else {
                    onResult(false, task.exception?.message ?: "Error")
                }
            }
    }
}
