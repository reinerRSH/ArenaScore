package com.example.arena.ui.screens

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arena.ui.theme.*
import com.example.arena.ui.components.OutlinedTextFieldShared
import com.example.arena.ui.components.ScanlineEffectShared

enum class LoginModeShared { ATHLETE, STAFF }

@Composable
fun LoginArenaContentShared(
    isLoading: Boolean,
    errorMessage: String?,
    backgroundPainter: Painter? = null,
    onLogin: (String, String, Boolean) -> Unit,
    onLoginGoogle: () -> Unit,
    onAuthenticateStaff: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var currentMode by remember { mutableStateOf(LoginModeShared.ATHLETE) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var clearanceId by remember { mutableStateOf("") }
    var accessCode by remember { mutableStateOf("") }
    var accessCodeVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize().background(ArenaBackground),
        contentAlignment = Alignment.Center
    ) {
        if (backgroundPainter != null) {
            Image(
                painter = backgroundPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        ScanlineEffectShared()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(scrollState)
                .background(ArenaSurfaceElevated.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ARENA",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = "ACCESO AL CENTRO DE MANDO",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = ArenaTextVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                maxLines = 1
            )

            // Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(ArenaSurfaceBase, RoundedCornerShape(8.dp))
                    .border(1.dp, ArenaStaffBorder, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoginModeButton(
                    modifier = Modifier.weight(1f),
                    text = "ATLETA",
                    isSelected = currentMode == LoginModeShared.ATHLETE,
                    onClick = { currentMode = LoginModeShared.ATHLETE }
                )
                LoginModeButton(
                    modifier = Modifier.weight(1f),
                    text = "PERSONAL",
                    isSelected = currentMode == LoginModeShared.STAFF,
                    onClick = { currentMode = LoginModeShared.STAFF }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (currentMode == LoginModeShared.ATHLETE) {
                AthleteLoginForm(
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    passwordVisible = passwordVisible,
                    onPasswordVisibleChange = { passwordVisible = it },
                    rememberMe = rememberMe,
                    onRememberMeChange = { rememberMe = it },
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onLogin = { onLogin(email, password, rememberMe) },
                    onLoginGoogle = onLoginGoogle,
                    onForgotPassword = onNavigateToForgotPassword,
                    onRegister = onNavigateToRegister
                )
            } else {
                StaffLoginForm(
                    clearanceId = clearanceId,
                    onClearanceIdChange = { clearanceId = it },
                    accessCode = accessCode,
                    onAccessCodeChange = { accessCode = it },
                    accessCodeVisible = accessCodeVisible,
                    onAccessCodeVisibleChange = { accessCodeVisible = it },
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onAuthenticate = { onAuthenticateStaff(clearanceId, accessCode) }
                )
            }
        }
    }
}

@Composable
fun LoginModeButton(modifier: Modifier = Modifier, text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(if (isSelected) ArenaSurfaceElevated else Color.Transparent, RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) ArenaPrimaryContainer else ArenaTextVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AthleteLoginForm(
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean, onPasswordVisibleChange: (Boolean) -> Unit,
    rememberMe: Boolean, onRememberMeChange: (Boolean) -> Unit,
    isLoading: Boolean, errorMessage: String?,
    onLogin: () -> Unit, onLoginGoogle: () -> Unit,
    onForgotPassword: () -> Unit, onRegister: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextFieldShared(value = email, onValueChange = onEmailChange, label = "CORREO ELECTRÓNICO")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextFieldShared(
            value = password, 
            onValueChange = onPasswordChange, 
            label = "CONTRASEÑA",
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordVisibleChange = onPasswordVisibleChange
        )

        if (errorMessage != null) {
            Text(errorMessage, color = ArenaWarning, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onRememberMeChange(!rememberMe) }) {
                Checkbox(checked = rememberMe, onCheckedChange = onRememberMeChange, colors = CheckboxDefaults.colors(checkedColor = ArenaPrimaryContainer))
                Text("Recuérdame", color = ArenaTextVariant, fontSize = 10.sp)
            }
            Text("¿Olvidaste tu contraseña?", color = ArenaPrimaryContainer, fontSize = 10.sp, modifier = Modifier.clickable { onForgotPassword() })
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(color = ArenaPrimaryContainer, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Button(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onLoginGoogle,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                border = BorderStroke(1.dp, ArenaUnfocusedBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Continuar con Google", color = Color.White, fontSize = 10.sp, maxLines = 1)
            }
        }
        
        Text(
            "¿No tienes una cuenta? Regístrate",
            color = ArenaTextVariant,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp).clickable { onRegister() },
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StaffLoginForm(
    clearanceId: String, onClearanceIdChange: (String) -> Unit,
    accessCode: String, onAccessCodeChange: (String) -> Unit,
    accessCodeVisible: Boolean, onAccessCodeVisibleChange: (Boolean) -> Unit,
    isLoading: Boolean, errorMessage: String?,
    onAuthenticate: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(ArenaWarning.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).border(1.dp, ArenaWarning.copy(alpha = 0.3f), RoundedCornerShape(6.dp)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = ArenaWarning, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Área de acceso restringido. Se requiere autorización.", color = ArenaTextVariant, fontSize = 9.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextFieldShared(value = clearanceId, onValueChange = onClearanceIdChange, label = "ID DE AUTORIZACIÓN")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextFieldShared(
            value = accessCode, 
            onValueChange = onAccessCodeChange, 
            label = "CÓDIGO DE ACCESO",
            isPassword = true,
            passwordVisible = accessCodeVisible,
            onPasswordVisibleChange = onAccessCodeVisibleChange
        )

        if (errorMessage != null) {
            Text(errorMessage, color = ArenaWarning, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(color = ArenaPrimaryContainer, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            OutlinedButton(
                onClick = onAuthenticate,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                border = BorderStroke(1.dp, ArenaPrimaryContainer),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ArenaPrimaryContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("AUTENTICAR", fontWeight = FontWeight.Bold)
            }
        }
    }
}
