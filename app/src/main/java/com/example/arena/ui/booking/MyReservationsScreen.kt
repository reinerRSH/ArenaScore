package com.example.arena.ui.booking

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.arena.R
import com.example.arena.domain.Reserva
import com.example.arena.ui.components.ArenaTopBarShared
import com.example.arena.ui.screens.ReceiptModalShared
import com.example.arena.ui.theme.*

@Composable
fun MyReservationsScreen(
    onNavigateBack: () -> Unit,
    notifications: List<Reserva> = emptyList(),
    onNotificationClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    viewModel: MyReservationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedReceipt by remember { mutableStateOf<Reserva?>(null) }
    var reservaToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            ArenaTopBarShared(
                title = "MIS RESERVAS",
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
        } else if (uiState.reservations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes reservas aún", color = ArenaTextVariant)
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
                        onClick = { selectedReceipt = reserva },
                        onDelete = { reservaToDelete = reserva.id }
                    )
                }
            }
        }
    }

    if (reservaToDelete != null) {
        AlertDialog(
            onDismissRequest = { reservaToDelete = null },
            containerColor = ArenaSurfaceElevated,
            titleContentColor = Color.White,
            textContentColor = ArenaTextVariant,
            title = { Text("BORRAR RESERVA", fontWeight = FontWeight.Black) },
            text = { Text("¿Estás seguro de que deseas eliminar esta reserva de tu historial?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteReservation(reservaToDelete!!)
                    reservaToDelete = null
                }) {
                    Text("BORRAR", color = ArenaWarning, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { reservaToDelete = null }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
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
fun ReservationCard(reserva: Reserva, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
        border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Miniatura de la cancha
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = ArenaSurfaceBase
                ) {
                    AsyncImage(
                        model = if (reserva.canchaImageUrl.isNotEmpty()) reserva.canchaImageUrl else R.drawable.cancha,
                        placeholder = painterResource(R.drawable.cancha),
                        error = painterResource(R.drawable.cancha),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        reserva.fecha,
                        color = ArenaPrimaryContainer,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    Text(
                        "${reserva.horaInicio} - ${reserva.horaFin}",
                        color = Color.White,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                    Text(
                        "Cancha: ${reserva.canchaid}",
                        color = ArenaTextVariant,
                        fontSize = 8.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp)
            ) {
                Icon(Icons.Default.Delete, "Borrar", tint = ArenaWarning.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
        }
    }
}
