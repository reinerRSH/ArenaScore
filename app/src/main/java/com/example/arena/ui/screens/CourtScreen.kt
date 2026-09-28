package com.example.arena.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.arena.domain.Reserva
import com.example.arena.navigation.Screen
import com.example.arena.ui.court.CourtViewModel
import com.example.arena.ui.home.HomeViewModel

@Composable
fun CourtSelectionScreen(
    navController: NavController,
    sedeId: String,
    viewModel: CourtViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val notifications by homeViewModel.notifications.collectAsState()
    val context = LocalContext.current
    var selectedReceipt by remember { mutableStateOf<Reserva?>(null) }

    LaunchedEffect(sedeId) {
        viewModel.loadCourts(sedeId)
    }

    if (selectedReceipt != null) {
        ReceiptModalShared(
            reserva = selectedReceipt,
            onDismiss = { selectedReceipt = null },
            onDownload = { Toast.makeText(context, "Guardando...", Toast.LENGTH_SHORT).show() },
            onShare = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Validación Arena: ${selectedReceipt!!.referenciaPago}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Validar Reserva"))
            }
        )
    }

    CourtSelectionContentShared(
        sede = uiState.selectedSede,
        canchas = uiState.canchas,
        notifications = notifications,
        onNavigateBack = { navController.popBackStack() },
        onCourtSelected = { cancha ->
            navController.navigate(Screen.BookingSchedule(canchaId = cancha.id, sedeId = sedeId))
        },
        onNotificationClick = {
            if (notifications.isNotEmpty()) selectedReceipt = notifications.last()
        }
    )
}
