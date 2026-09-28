package com.example.arena.ui.auth.register

import android.widget.Toast
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
import com.example.arena.ui.screens.RegisterArenaContentShared
import kotlinx.coroutines.launch

@Composable
fun RegisterArenaScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        when (uiState) {
            is RegisterState.success -> {
                Toast.makeText(context, context.getString(R.string.registration_successful), Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home) {
                    popUpTo(Screen.Register) { inclusive = true }
                }
            }
            is RegisterState.Error -> {
                Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    RegisterArenaContentShared(
        isLoading = uiState is RegisterState.loading,
        errorMessage = (uiState as? RegisterState.Error)?.message,
        backgroundPainter = painterResource(R.drawable.logo_branding),
        onRegister = { firstName, lastName, email, password, confirmPassword ->
            val errorMsg = RegistrarValidator.validarFormulario(context, firstName, lastName, email, password, confirmPassword)
            if (errorMsg != null) {
                viewModel.setError(errorMsg)
            } else {
                viewModel.registerUsuario(firstName, lastName, email, password)
            }
        },
        onRegisterGoogle = {
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
                        viewModel.registerWithGoogle(googleIdTokenCredential.idToken)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error con Google: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        },
        onBackToLogin = { navController.popBackStack() }
    )
}

@Preview(name = "Arena Register - Shared", showBackground = true)
@Composable
fun RegisterArenaPreview() {
    EliteAthleteOSTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            RegisterArenaContentShared(
                isLoading = false,
                errorMessage = null,
                backgroundPainter = null,
                onRegister = { _, _, _, _, _ -> },
                onRegisterGoogle = {},
                onBackToLogin = {}
            )
        }
    }
}
