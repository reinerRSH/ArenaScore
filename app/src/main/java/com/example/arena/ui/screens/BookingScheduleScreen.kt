package com.example.arena.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.arena.domain.Reserva
import com.example.arena.navigation.Screen
import com.example.arena.ui.booking.BookingScheduleViewModel
import com.example.arena.ui.home.HomeViewModel

@Composable
fun BookingScheduleScreen(
    navController: NavController,
    sedeId: String,
    canchaId: String,
    tipoReserva: String,
    viewModel: BookingScheduleViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val notifications by homeViewModel.notifications.collectAsState()
    val context = LocalContext.current
    var selectedReceipt by remember { mutableStateOf<Reserva?>(null) }

    LaunchedEffect(sedeId, canchaId, tipoReserva) {
        viewModel.initBooking(sedeId, canchaId, tipoReserva)
    }

    if (selectedReceipt != null) {
        ReceiptModalShared(
            reserva = selectedReceipt,
            onDismiss = { selectedReceipt = null },
            onDownload = { Toast.makeText(context, "Guardado", Toast.LENGTH_SHORT).show() },
            onShare = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Comprobante: ${selectedReceipt!!.referenciaPago}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Compartir"))
            }
        )
    }

    BookingScheduleContentShared(
        uiState = uiState,
        notifications = notifications,
        onNavigateBack = { navController.popBackStack() },
        onChangeCourt = { navController.popBackStack() },
        onBookClick = { viewModel.preparePayment() },
        onDismissConfirmation = { viewModel.dismissConfirmation() },
        onTimeSelected = { viewModel.selectTime(it) },
        onToggleExtra = { name, price -> viewModel.toggleExtra(name, price) },
        onUpdatePaymentRef = { viewModel.updatePaymentReference(it) },
        onConfirmPayment = { viewModel.confirmBooking() },
        onDismissPayment = { viewModel.cancelPayment() },
        onCopyPaymentData = { /* Lógica de copiado ya está en el Shared usualmente */ },
        onNotificationClick = {
            if (notifications.isNotEmpty()) selectedReceipt = notifications.last()
        },
        dateSelectionContent = {
            // Aquí iría el DatePicker o selector de fecha que ya tienes
        }
    )
}
