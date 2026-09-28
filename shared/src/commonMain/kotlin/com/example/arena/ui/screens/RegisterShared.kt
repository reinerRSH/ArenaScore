package com.example.arena.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arena.ui.theme.*
import com.example.arena.ui.components.OutlinedTextFieldShared

@Composable
fun RegisterArenaContentShared(
    isLoading: Boolean,
    errorMessage: String?,
    backgroundPainter: Painter? = null,
    onRegister: (String, String, String, String, String) -> Unit,
    onRegisterGoogle: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val scrollState = rememberScrollState()
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ÚNETE A ARENA",
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = PrimaryNeon,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp,
                maxLines = 1
            )
            Text(
                text = "RENDIMIENTO INSTITUCIONAL Y ANALÍTICA",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = ArenaTextVariant,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ArenaSurfaceElevated.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .border(1.dp, ArenaUnfocusedBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(24.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextFieldShared(value = firstName, onValueChange = { firstName = it.uppercase() }, label = "NOMBRE")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextFieldShared(value = lastName, onValueChange = { lastName = it.uppercase() }, label = "APELLIDO")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextFieldShared(value = email, onValueChange = { email = it }, label = "CORREO ELECTRÓNICO")
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextFieldShared(
                    value = password, 
                    onValueChange = { password = it }, 
                    label = "CONTRASEÑA",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibleChange = { passwordVisible = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextFieldShared(
                    value = confirmPassword, 
                    onValueChange = { confirmPassword = it }, 
                    label = "CONFIRMAR CREDENCIALES",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibleChange = { passwordVisible = it }
                )

                if (errorMessage != null) {
                    Text(errorMessage, color = ArenaWarning, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

            Button(
                    onClick = { onRegister(firstName, lastName, email, password, confirmPassword) },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ArenaOnPrimaryFixed)
                    } else {
                        Text("REGISTRARSE", fontWeight = FontWeight.Bold, color = ArenaOnPrimaryFixed, fontSize = 11.sp, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onRegisterGoogle,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    border = BorderStroke(1.dp, ArenaUnfocusedBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Continuar con Google", color = Color.White, fontSize = 10.sp, maxLines = 1)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("¿Ya tienes una cuenta? ", color = ArenaTextVariant, fontSize = 11.sp)
                    Text("Iniciar sesión", color = PrimaryNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onBackToLogin() } )
                }
            }
        }
    }
}
