package com.example.arena.ui.auth.login

import android.widget.Toast
import android.widget.Toast.makeText
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.arena.R
import com.example.arena.View.ui.theme.EliteAthleteOSTheme
import com.example.arena.navigation.Screen
import com.example.arena.ui.screens.LoginArenaContentShared
import kotlinx.coroutines.launch

@Composable
fun LoginArenaScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginState.Success -> {
                makeText(context, context.getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home) {
                    popUpTo(Screen.Login) { this.inclusive = true }
                }
            }
            is LoginState.StaffSuccess -> {
                makeText(context, "Acceso de Personal concedido", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home) {
                    popUpTo(Screen.Login) { inclusive = true }
                }
            }
            is LoginState.Error -> {
                makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    LoginArenaContentShared(
        isLoading = uiState is LoginState.Loading,
        errorMessage = (uiState as? LoginState.Error)?.message,
        backgroundPainter = painterResource(R.drawable.logo_branding),
        onLogin = { email, password, rememberMe ->
            viewModel.loginUsuario(email, password, rememberMe)
        },
        onLoginGoogle = {
            coroutineScope.launch {
                try {
                    val credentialManager = androidx.credentials.CredentialManager.create(context)
                    val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId("87579931946-tr0gevk4piu4iup332f6b42dpnda1gb3.apps.googleusercontent.com")
                        .setAutoSelectEnabled(false)
                        .build()
                    val getCredentialRequest = androidx.credentials.GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()
                    val result = credentialManager.getCredential(context, getCredentialRequest)
                    val credential = result.credential
                    if (credential is androidx.credentials.CustomCredential && credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                        viewModel.loginWithGoogle(googleIdTokenCredential.idToken)
                    }
                } catch (e: Exception) {
                    makeText(context, "Error con Google: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onAuthenticateStaff = { id, code ->
            viewModel.authenticateStaff(id, code) { _, message ->
                if (message != null && uiState !is LoginState.StaffSuccess) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        },
        onNavigateToRegister = { navController.navigate(Screen.Register) },
        onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword) }
    )
}

@Preview(showBackground = true, name = "Arena Login Completo")
@Composable
fun ArenaLoginPreview() {
    EliteAthleteOSTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoginArenaContentShared(
                isLoading = false,
                errorMessage = null,
                backgroundPainter = null,
                onLogin = { _, _, _ -> },
                onLoginGoogle = {},
                onAuthenticateStaff = { _, _ -> },
                onNavigateToRegister = {},
                onNavigateToForgotPassword = {}
            )
        }
    }
}
