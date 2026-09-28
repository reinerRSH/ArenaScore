package com.example.arena.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arena.ui.components.ArenaTopBarShared
import com.example.arena.ui.components.OutlinedTextFieldShared
import com.example.arena.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    notifications: List<com.example.arena.domain.Reserva> = emptyList(),
    onNotificationClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    LaunchedEffect(uiState.user) {
        uiState.user?.let {
            name = it.name
            lastName = it.lastName
            phone = it.phone
            imageUrl = it.imageUrl
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
            viewModel.resetSuccess()
        }
    }

    Scaffold(
        topBar = {
            ArenaTopBarShared(
                title = "MI PERFIL",
                onNavigateBack = onNavigateBack,
                showBack = true,
                notificationCount = notifications.size,
                onNotificationClick = onNotificationClick,
                onHistoryClick = onHistoryClick
            )
        },
        containerColor = ArenaSurfaceBase
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ArenaPrimaryContainer)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Placeholder for Profile Image
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(ArenaSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text("IMG", color = ArenaTextVariant)
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextFieldShared(
                    value = name,
                    onValueChange = { name = it },
                    label = "NOMBRE"
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextFieldShared(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "APELLIDO"
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextFieldShared(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "TELÉFONO"
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextFieldShared(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = "URL IMAGEN"
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.saveProfile(name, lastName, phone, imageUrl) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(color = ArenaOnPrimaryFixed, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            "GUARDAR PERFIL",
                            color = ArenaOnPrimaryFixed,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(uiState.error!!, color = ArenaWarning, fontSize = 12.sp)
                }
            }
        }
    }
}
