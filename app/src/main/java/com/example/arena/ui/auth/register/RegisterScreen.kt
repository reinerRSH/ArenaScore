package com.example.arena.ui.auth.register

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.arena.View.ui.theme.*
import com.example.arena.LoginMode
import com.example.arena.navigation.Screen
import com.example.arena.R
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
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }
            }
            is RegisterState.Error -> {
                Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaSurfaceBase)
            .windowInsetsPadding(WindowInsets.ime) // Soporte nativo para que la UI suba con el teclado
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo_branding),
            contentDescription = stringResource(R.string.description_login_background),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        RegisterArenaContent(
            uiState = uiState,
            onRegister = { firstName, lastName, email, password, confirmPassword ->
                // 🎯 VALIDACIÓN EN UNA SOLA LÍNEA (Cero ruido de IFs heredados)
                val errorMsg = RegistrarValidator.validarFormulario(context, firstName, lastName, email, password, confirmPassword)

                if (errorMsg != null) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
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
                            .setServerClientId("999434492354-j22e5mba6c668cajqm8dtrfvd4c2864h.apps.googleusercontent.com")
                            .setAutoSelectEnabled(false)
                            .build()

                        val getCredentialRequest = androidx.credentials.GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        val result = credentialManager.getCredential(context, getCredentialRequest)
                        val credential = result.credential

                        if (credential is com.google.android.libraries.identity.googleid.GoogleIdTokenCredential) {
                            val idToken = credential.idToken
                            viewModel.registerWithGoogle(idToken)
                        } else {
                            Toast.makeText(context, "No se pudo obtener una credencial válida de Google", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Google Sign-In failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onBackToLogin = { navController.popBackStack() }
        )
    }
}

@Composable
fun RegisterArenaContent(
    uiState: RegisterState,
    onRegister: (String, String, String, String, String) -> Unit,
    onRegisterGoogle: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedRole by remember { mutableStateOf(LoginMode.ATHLETE) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isError = uiState is RegisterState.Error
    val errorMessage = (uiState as? RegisterState.Error)?.message


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Cambiado a Top para mejor comportamiento con scroll
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // --- HEADER SECTION ---
        Text(
            text = stringResource(R.string.join_the_arena),
            fontSize = 36.sp, // Tamaño ajustado para evitar que se amontone
            lineHeight = 42.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            color = PrimaryNeon,
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp
        )
        Text(
            text = stringResource(R.string.institutional_performance_analytics),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = ArenaTextVariant,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // --- GLASS CARD CONTAINER ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardRegister, RoundedCornerShape(4.dp))
                .border(1.dp, ArenaUnfocusedBorder.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(24.dp)
        ) {
            // --- ROLE SWITCHER ---


            Spacer(modifier = Modifier.height(16.dp))

            // --- FORM: FIRST NAME & LAST NAME ---
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it.uppercase() },
                    label = { Text(stringResource(R.string.label_first_name), fontSize = 10.sp, color = ArenaTextVariant) },
                    placeholder = { Text(stringResource(R.string.placeholder_first_name), color = ArenaTextVariant.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryNeon,
                        unfocusedBorderColor = ArenaUnfocusedBorder,
                        focusedContainerColor = ArenaSurfaceBase,
                        unfocusedContainerColor = ArenaSurfaceBase
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it.uppercase() },
                    label = { Text(stringResource(R.string.label_last_name), fontSize = 10.sp, color = ArenaTextVariant) },
                    placeholder = { Text(stringResource(R.string.placeholder_last_name), color = ArenaTextVariant.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryNeon,
                        unfocusedBorderColor = ArenaUnfocusedBorder,
                        focusedContainerColor = ArenaSurfaceBase,
                        unfocusedContainerColor = ArenaSurfaceBase
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.label_email_address), fontSize = 10.sp, color = ArenaTextVariant) },
                placeholder = { Text(stringResource(R.string.placeholder_email_register), color = ArenaTextVariant.copy(alpha = 0.5f)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryNeon,
                    unfocusedBorderColor = ArenaUnfocusedBorder,
                    focusedContainerColor = ArenaSurfaceBase,
                    unfocusedContainerColor = ArenaSurfaceBase
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                isError = isError && password.isBlank(),
                label = { Text(stringResource(R.string.label_password), fontSize = 10.sp, color = ArenaTextVariant) },
                visualTransformation = if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) stringResource(R.string.description_hide_password) else stringResource(
                                R.string.description_show_password
                            ),
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = ArenaUnfocusedBorder,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            if (isError && errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                isError = isError && confirmPassword.isBlank(),
                label = { Text(stringResource(R.string.label_Confirmpassword), fontSize = 10.sp, color = ArenaTextVariant) },
                visualTransformation = if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) stringResource(R.string.description_hide_password) else stringResource(
                                R.string.description_show_password
                            ),
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = ArenaUnfocusedBorder,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            if (isError && errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onRegister(firstName, lastName, email, password, confirmPassword) },
                enabled = uiState !is RegisterState.loading,
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNeon,
                    disabledContainerColor = PrimaryNeon.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState is RegisterState.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = ArenaOnPrimaryFixed,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.btn_register),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArenaOnPrimaryFixed,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = ArenaStaffBorder.copy(alpha = 0.5f))
                Text(text = stringResource(R.string.or_continue_with), fontSize = 9.sp, color = ArenaTextVariant.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = ArenaStaffBorder.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onRegisterGoogle() },
                shape = RoundedCornerShape(2.dp),
                border = BorderStroke(1.dp, ArenaUnfocusedBorder),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = ArenaSurfaceElevated.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(text = stringResource(R.string.continue_with_google), color = ArenaTextPrimary, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = stringResource(R.string.already_have_account), fontSize = 12.sp, color = ArenaTextVariant)
                Text(
                    text = stringResource(R.string.login_action),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNeon,
                    modifier = Modifier.clickable { onBackToLogin() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Preview(name = "Arena Register - Dark Mode", showBackground = true, apiLevel = 34)
@Composable
fun RegisterArenaPreview() {
    EliteAthleteOSTheme {
        val fakeUiState = RegisterState.idle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ArenaSurfaceBase)
        ) {
            RegisterArenaContent(
                uiState = fakeUiState,
                onRegister = { _, _, _, _, _ -> },
                onRegisterGoogle = {},
                onBackToLogin = {}
            )
        }
    }
}
