package com.example.arena.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    sedeId: String,
    viewModel: AdminDashboardViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sedeId) {
        viewModel.loadAdminData(sedeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("ADMIN: ${uiState.selectedSede?.nombreSede ?: ""}") })
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            item {
                Text("CONFIGURACIÓN DE PRECIOS", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Precio Base: $${uiState.selectedSede?.precioBase}")
                // Aquí irían inputs para editar
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text("PAGOS POR VERIFICAR", fontWeight = FontWeight.Bold, color = Color.Yellow)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.pendingPayments.isEmpty()) {
                item { Text("No hay pagos pendientes") }
            }

            items(uiState.pendingPayments) { reserva ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ref: ${reserva.referenciaPago}", fontWeight = FontWeight.Bold)
                        Text("Monto: $${reserva.montoTotal}")
                        Text("Cancha: ${reserva.canchaid}")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { viewModel.verifyPayment(reserva.id, false) }) {
                                Text("RECHAZAR", color = Color.Red)
                            }
                            Button(onClick = { viewModel.verifyPayment(reserva.id, true) }) {
                                Text("APROBAR")
                            }
                        }
                    }
                }
            }
        }
    }
}