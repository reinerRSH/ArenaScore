package com.example.arena.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arena.ui.components.ArenaTopBarShared
import com.example.arena.ui.components.OutlinedTextFieldShared
import com.example.arena.ui.theme.*
import com.example.arena.util.servicesList

@Composable
fun StaffSetupScreen(
    onSetupComplete: (String) -> Unit,
    viewModel: StaffSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var venueName by remember { mutableStateOf("") }
    var numCourts by remember { mutableStateOf("1") }
    var extras by remember { mutableStateOf("") }
    val selectedServices = remember { mutableStateListOf<String>() }

    LaunchedEffect(uiState.setupComplete) {
        if (uiState.setupComplete && uiState.generatedSedeId != null) {
            onSetupComplete(uiState.generatedSedeId!!)
        }
    }

    Scaffold(
        topBar = {
            ArenaTopBarShared(
                title = "CONFIGURACIÓN STAFF",
                showBack = false
            )
        },
        containerColor = ArenaSurfaceBase
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Bienvenido, configura tu sede para comenzar.",
                color = ArenaTextVariant,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextFieldShared(
                value = venueName,
                onValueChange = { venueName = it },
                label = "NOMBRE DE LA SEDE"
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextFieldShared(
                value = numCourts,
                onValueChange = { if (it.all { char -> char.isDigit() }) numCourts = it },
                label = "NÚMERO DE CANCHAS"
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextFieldShared(
                value = extras,
                onValueChange = { extras = it },
                label = "EXTRAS (E.J. PALAS, AGUA, SEPARADOS POR COMA)"
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "SERVICIOS DISPONIBLES",
                color = ArenaPrimaryContainer,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            servicesList.forEach { service ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedServices.contains(service.name),
                        onCheckedChange = { 
                            if (it) selectedServices.add(service.name)
                            else selectedServices.remove(service.name)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ArenaPrimaryContainer,
                            uncheckedColor = ArenaTextVariant
                        )
                    )
                    Text(
                        text = service.name.uppercase(),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    val courts = numCourts.toIntOrNull() ?: 1
                    val extrasList = extras.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    viewModel.setupVenue(venueName, courts, extrasList, selectedServices.toList())
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isSaving && venueName.isNotEmpty()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(color = ArenaOnPrimaryFixed, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "CREAR SEDE",
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
