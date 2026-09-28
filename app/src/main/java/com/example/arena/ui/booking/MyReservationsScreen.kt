package com.example.arena.ui.booking

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arena.domain.Reserva
import com.example.arena.ui.components.ArenaTopBarShared
import com.example.arena.ui.screens.ReceiptModalShared
import com.example.arena.ui.theme.*

@Composable
fun MyReservationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: MyReservationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedReceipt by remember { mutableStateOf<Reserva?>(null) }

    Scaffold(
        topBar = {
            ArenaTopBarShared(
                title = "MIS RESERVAS",
                onNavigateBack = onNavigateBack,
                showBack = true
            )
        },
        containerColor = ArenaSurfaceBase
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ArenaPrimaryContainer)
            }
        } else if (uiState.reservations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes reservas aún", color = ArenaTextVariant, fontFamily = FontFamily.Monospace)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.reservations) { reserva ->
                    ReservationCard(
                        reserva = reserva,
                        onClick = { selectedReceipt = reserva }
                    )
                }
            }
        }
    }

    if (selectedReceipt != null) {
        ReceiptModalShared(
            reserva = selectedReceipt!!,
            onDismiss = { selectedReceipt = null },
            onDownload = { Toast.makeText(context, "Descargando...", Toast.LENGTH_SHORT).show() },
            onShare = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Reserva Arena: ${selectedReceipt!!.fecha} ${selectedReceipt!!.horaInicio}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Compartir"))
            }
        )
    }
}

@Composable
fun ReservationCard(reserva: Reserva, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
        border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reserva.fecha,
                    color = ArenaPrimaryContainer,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
                Text(
                    "${reserva.horaInicio} - ${reserva.horaFin}",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    "Cancha: ${reserva.canchaid}",
                    color = ArenaTextVariant,
                    fontSize = 11.sp
                )
            }
            
            Surface(
                color = when (reserva.estado) {
                    Reserva.STATUS_ACTIVA -> ArenaSuccess.copy(alpha = 0.1f)
                    Reserva.STATUS_PENDIENTE -> ArenaWarning.copy(alpha = 0.1f)
                    else -> Color.Gray.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, when (reserva.estado) {
                    Reserva.STATUS_ACTIVA -> ArenaSuccess
                    Reserva.STATUS_PENDIENTE -> ArenaWarning
                    else -> Color.Gray
                })
            ) {
                Text(
                    reserva.estado.replace("_", " "),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (reserva.estado) {
                        Reserva.STATUS_ACTIVA -> ArenaSuccess
                        Reserva.STATUS_PENDIENTE -> ArenaWarning
                        else -> Color.White
                    }
                )
            }
        }
    }
}
