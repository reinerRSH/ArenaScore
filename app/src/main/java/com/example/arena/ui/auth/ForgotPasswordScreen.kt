package com.example.arena.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arena.R
import com.example.arena.View.ui.theme.ArenaUnfocusedBorder
import com.example.arena.View.ui.theme.CardRegister
import com.example.arena.View.ui.theme.EliteAthleteOSTheme
import com.example.arena.ui.auth.login.LoginViewModel

@Composable
fun ResetPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    ResetPasswordContent(
        onResetClick = { email ->
            isLoading = true
            viewModel.resetPassword(email) { success, message ->
                isLoading = false
                statusMessage = message ?: ""
            }
        },
        onNavigateBack = onNavigateBack,
        isLoading = isLoading,
        statusMessage = statusMessage
    )
}

@Composable
fun ResetPasswordContent(
    onResetClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    isLoading: Boolean,
    statusMessage: String
) {
    var email by remember { mutableStateOf("") }

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
                .background(
                    CardRegister, // Usando el color oscuro con transparencia del registro
                    shape = RoundedCornerShape(12.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.reset_access),
                fontSize = 32.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White, // Forzado a Blanco puro como pediste
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { 
                    Text(
                        stringResource(R.string.registered_email),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onResetClick(email) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = if (isLoading) stringResource(R.string.processing) else stringResource(R.string.send_reset_link),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    color = Color.White,
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.back_to_login),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ResetPasswordPreview() {
    EliteAthleteOSTheme { // Corregido para que la previsualización use el tema oscuro
        ResetPasswordContent(
            onResetClick = {},
            onNavigateBack = {},
            isLoading = false,
            statusMessage = "Simulación de envío de correo"
        )
    }
}
