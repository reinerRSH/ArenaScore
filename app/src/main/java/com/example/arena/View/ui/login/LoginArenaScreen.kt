package com.example.arena.View.ui.login

import android.R.attr.onClick
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
import androidx.compose.ui.text.buildAnnotatedString
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
import com.example.arena.View.ui.theme.EliteAthleteOSTheme
import com.example.arena.ViewModel.LoginViewModel
import com.example.arena.ViewModel.LoginState


@Composable
fun LoginArenaScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginState.Success -> {
                navController.navigate("home_Screen") {
                    popUpTo("login_Screen") {
                        inclusive = true
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
        onLogin = { email, password ->
            viewModel.loginUsuario(email, password, rememberMe = false)

        }
    )
}

@Composable
fun LoginArenaContent(
    uiState: LoginState,
    onLogin: (String, String) -> Unit,
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
            contentDescription = "fondo login",
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
                text = "ARENA",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "COMMAND CENTER ACCESS",
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
                    .border(1.dp, Color(0xFF2D3748), RoundedCornerShape(8.dp))
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
                        text = "ATHLETE",
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
                        text = "STAFF",
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
                                "EMAIL",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        placeholder = { Text("athlete@arena.com", color = Color.Gray) },
                        shape = RoundedCornerShape(8.dp),
                        colors = outlinedTextFieldColorsCustom(),
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
                                "PASSWORD",
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
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = outlinedTextFieldColorsCustom(),
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
                                "Remember me",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = "Forgot Password?",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable { makeText(context, "Forgot Password", Toast.LENGTH_SHORT).show()}
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState is LoginState.Loading) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                onLogin(email, password)
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
                            Text("LOGIN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                val registerText = buildAnnotatedString {
                    withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )) {
                        append("Don't have an account? ")
                    }
                    pushStringAnnotation(tag = "register", annotation = "register")
                    withStyle(
                        style = MaterialTheme.typography.bodyMedium.toSpanStyle()
                            .copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                    ) {
                        append("Register")
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
                                makeText(context, "Accediendo al registro...", Toast.LENGTH_SHORT).show()
                                // Aquí puedes añadir la navegación real, por ejemplo:
                                // navController.navigate("register_screen")
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
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Restricted access area. Valid clearance required.",
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
                                "CLEARANCE ID / EMAIL",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        placeholder = { Text("STAFF-ID-001", color = Color.Gray) },
                        shape = RoundedCornerShape(8.dp),
                        colors = outlinedTextFieldColorsCustom(),
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
                                "ACCESS CODE",
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
                                    contentDescription = if (accessCodeVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = outlinedTextFieldColorsCustom(),
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
                        text = "Request Access Reset",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState is LoginState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(8.dp)
                        )
                    } else {
                        OutlinedButton(
                            onClick = { onLogin(clearanceId, accessCode) },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("AUTHENTICATE", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun outlinedTextFieldColorsCustom() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = Color(0xFF2D2D30),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)

@Preview(showBackground = true, name = "Arena Login Completo")
@Composable
fun ArenaLoginPreview() {
    EliteAthleteOSTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoginArenaContent(
                uiState = LoginState.Idle,
                onLogin = { _, _ -> },
                navController = NavController(LocalContext.current)
            )
        }
    }
}