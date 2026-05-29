package com.example.arena.ui.auth.login

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.arena.R
import com.example.arena.View.ui.theme.ArenaStaffBorder
import com.example.arena.View.ui.theme.ArenaUnfocusedBorder
import com.example.arena.View.ui.theme.ArenaWarning
import com.example.arena.View.ui.theme.EliteAthleteOSTheme
import androidx.compose.ui.text.buildAnnotatedString
import com.example.arena.navigation.Screen
import com.example.arena.LoginMode
import androidx.compose.runtime.rememberCoroutineScope
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
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) {
                        this.inclusive = true
                    }
                }
            }
            is LoginState.Error -> {
                makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    LoginArenaContent(
        uiState = uiState,
        navController = navController,
        onLogin = { email, password, rememberMe ->
            viewModel.loginUsuario(email, password, rememberMe)
        },
        onLoginGoogle = {
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
                        viewModel.loginWithGoogle(idToken)
                    } else {
                        makeText(context, "No se pudo obtener una credencial válida de Google", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    makeText(context, "Google Sign-In failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}

@Composable
fun LoginArenaContent(
    uiState: LoginState,
    onLogin: (String, String, Boolean) -> Unit,
    onLoginGoogle: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    var currentMode by remember { mutableStateOf(LoginMode.ATHLETE) }
    var email by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var clearanceId by remember { mutableStateOf("") }
    var accessCode by remember { mutableStateOf("") }
    var accessCodeVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val isError = uiState is LoginState.Error
    val errorMessage = (uiState as? LoginState.Error)?.message

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_branding),
            contentDescription = stringResource(R.string.description_login_background),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(scrollState)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.titulo_app),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.command_center_access),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // 3. BARRA SELECTORA DE MODO (ATHLETE vs STAFF)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .border(1.dp, ArenaStaffBorder, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isAthlete = currentMode == LoginMode.ATHLETE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (isAthlete) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { currentMode = LoginMode.ATHLETE },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.mode_athlete),
                        color = if (isAthlete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                val isStaff = currentMode == LoginMode.STAFF
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (isStaff) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { currentMode = LoginMode.STAFF },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.mode_staff),
                        color = if (isStaff) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (currentMode == LoginMode.ATHLETE) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        isError = isError && email.isBlank(),
                        label = {
                            Text(
                                stringResource(R.string.label_email),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        placeholder = { Text(stringResource(R.string.placeholder_email), color = Color.Gray) },
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        isError = isError && password.isBlank(),
                        label = {
                            Text(
                                stringResource(R.string.label_password),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) stringResource(R.string.description_hide_password) else stringResource(R.string.description_show_password)
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

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { rememberMe = !rememberMe },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                            Text(
                                stringResource(R.string.remember_me),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = stringResource(R.string.forgot_password),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable { }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState is LoginState.Loading) {
                        LoadingIndicator()
                    } else {
                        Button(
                            onClick = {
                                onLogin(email, password, rememberMe)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(stringResource(R.string.btn_login), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = { onLoginGoogle() },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ArenaUnfocusedBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(stringResource(R.string.continue_with_google), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                val dontHaveAccountStr = stringResource(R.string.dont_have_account)
                val registerStr = stringResource(R.string.Register)
                val registerText = buildAnnotatedString {
                    withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )) {
                        append(dontHaveAccountStr)
                    }
                    pushStringAnnotation(tag = "register", annotation = "register")
                    withStyle(
                        style = MaterialTheme.typography.bodyMedium.toSpanStyle()
                            .copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                    ) {
                        append(registerStr)
                    }
                    pop()
                }

                ClickableText(
                    text = registerText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    ),
                    onClick = { offset ->
                        registerText.getStringAnnotations(tag = "register", start = offset, end = offset)
                            .firstOrNull()?.let {
                                navController.navigate(Screen.Register.route)
                            }
                    }
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ArenaWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.staff_restricted_access),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = clearanceId,
                        onValueChange = { clearanceId = it },
                        isError = isError && clearanceId.isBlank(),
                        label = {
                            Text(
                                stringResource(R.string.label_clearance_id),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        placeholder = { Text(stringResource(R.string.placeholder_staff_id), color = Color.Gray) },
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
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = accessCode,
                        onValueChange = { accessCode = it },
                        isError = isError && accessCode.isBlank(),
                        label = {
                            Text(
                                stringResource(R.string.label_access_code),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        visualTransformation = if (accessCodeVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { accessCodeVisible = !accessCodeVisible }) {
                                Icon(
                                    imageVector = if (accessCodeVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (accessCodeVisible) stringResource(R.string.description_hide_password) else stringResource(R.string.description_show_password)
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

                    Text(
                        text = stringResource(R.string.request_access_reset),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState is LoginState.Loading) {
                        LoadingIndicator()
                    } else {
                        OutlinedButton(
                            onClick = { onLogin(clearanceId, accessCode, false) },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(stringResource(R.string.btn_authenticate), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Preview(showBackground = true, name = "Arena Login Completo")
@Composable
fun ArenaLoginPreview() {
    EliteAthleteOSTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoginArenaContent(
                uiState = LoginState.Idle,
                onLogin = { _, _, _ -> },
                onLoginGoogle = {},
                navController = NavController(LocalContext.current)
            )
        }
    }
}