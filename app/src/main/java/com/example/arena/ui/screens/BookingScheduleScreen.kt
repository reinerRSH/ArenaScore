package com.example.arena.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.arena.domain.Reserva
import com.example.arena.ui.booking.BookingIntent
import com.example.arena.ui.booking.BookingScheduleViewModel
import com.example.arena.ui.home.HomeViewModel

@Composable
fun BookingScheduleScreen(
    navController: NavController,
    sedeId: String,
    canchaId: String,
    tipoReserva: String,
    canchaName: String = "",
    viewModel: BookingScheduleViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = { navController.popBackStack() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val notifications by homeViewModel.notifications.collectAsState()
    val context = LocalContext.current
    var selectedReceipt by remember { mutableStateOf<Reserva?>(null) }

    LaunchedEffect(sedeId, canchaId, tipoReserva) {
        viewModel.init(sedeId, canchaId, tipoReserva)
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

    val sharedUiState = BookingScheduleUiStateShared(
        selectedDate = uiState.selectedDate.toString(),
        selectedTime = uiState.selectedTime,
        reservedSlots = uiState.reservedSlots,
        canchaName = uiState.canchaName,
        tipoReserva = uiState.tipoReserva,
        selectedSede = uiState.selectedSede,
        selectedExtras = uiState.selectedExtras,
        totalPrice = uiState.totalPrice,
        paymentReference = uiState.paymentReference,
        isPaymentSheetVisible = uiState.isPaymentSheetVisible,
        isConfirmationVisible = uiState.isConfirmationVisible,
        isLoading = uiState.isLoading
    )

    BookingScheduleContentShared(
        uiState = sharedUiState,
        notifications = notifications,
        onNavigateBack = { navController.popBackStack() },
        onChangeCourt = { navController.popBackStack() },
        onBookClick = { viewModel.handleIntent(BookingIntent.ShowPaymentSheet) },
        onDismissConfirmation = { viewModel.hideConfirmation() },
        onTimeSelected = { viewModel.onTimeSelected(it) },
        onToggleExtra = { name, price -> viewModel.handleIntent(BookingIntent.ToggleExtra(name, price)) },
        onUpdatePaymentRef = { viewModel.handleIntent(BookingIntent.UpdateReference(it)) },
        onConfirmPayment = { viewModel.handleIntent(BookingIntent.ConfirmPayment) },
        onDismissPayment = { viewModel.handleIntent(BookingIntent.HidePaymentSheet) },
        onCopyPaymentData = { /* Clipboard logic */ },
        onNotificationClick = {
            if (notifications.isNotEmpty()) selectedReceipt = notifications.last()
        }
    )
}
