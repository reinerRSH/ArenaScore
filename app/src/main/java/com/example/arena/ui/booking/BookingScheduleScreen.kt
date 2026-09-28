package com.example.arena.ui.booking

import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.arena.R
import com.example.arena.View.ui.theme.EliteAthleteOSTheme
import com.example.arena.ui.screens.BookingScheduleContentShared
import com.example.arena.ui.screens.BookingScheduleUiStateShared
import com.example.arena.ui.screens.ReceiptModalShared
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun BookingScheduleScreen(
    sedeId: String,
    canchaId: String,
    tipoReserva: String,
    canchaName: String,
    viewModel: BookingScheduleViewModel,
    notifications: List<com.example.arena.domain.Reserva> = emptyList(),
    onNavigateBack: () -> Unit,
    onChangeCourt: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedReceipt by remember { mutableStateOf<com.example.arena.domain.Reserva?>(null) }

    LaunchedEffect(sedeId, canchaId, tipoReserva, canchaName) {
        viewModel.init(sedeId, canchaId, tipoReserva, canchaName)
    }

    LaunchedEffect(uiState.paymentStatus) {
        if (uiState.paymentStatus == PaymentStatus.Verified) {
            selectedReceipt = uiState.lastReserva
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    val sharedUiState = BookingScheduleUiStateShared(
        selectedDate = uiState.selectedDate.toString(),
        selectedTime = uiState.selectedTime,
        reservedSlots = uiState.reservedSlots,
        instructores = uiState.instructores,
        selectedInstructorId = uiState.selectedInstructorId,
        instructorSlots = uiState.instructorSlots,
        canchaName = uiState.canchaName,
        tipoReserva = uiState.tipoReserva,
        selectedSede = uiState.selectedSede,
        selectedExtras = uiState.selectedExtras,
        totalPrice = uiState.totalPrice,
        paymentReference = uiState.paymentReference,
        isPaymentSheetVisible = uiState.isPaymentSheetVisible,
        isConfirmationVisible = uiState.isConfirmationVisible,
        isLoading = uiState.isLoading,
        canchaImageUrl = uiState.canchaImageUrl,
        lastReserva = uiState.lastReserva
    )

    BookingScheduleContentShared(
        uiState = sharedUiState,
        notifications = notifications,
        onNavigateBack = onNavigateBack,
        onChangeCourt = onChangeCourt,
        onBookClick = { viewModel.handleIntent(BookingIntent.ShowPaymentSheet) },
        onDismissConfirmation = { viewModel.hideConfirmation() },
        onTimeSelected = { viewModel.onTimeSelected(it) },
        onInstructorSelected = { viewModel.onInstructorSelected(it) },
        onToggleExtra = { name, price -> viewModel.handleIntent(BookingIntent.ToggleExtra(name, price)) },
        onUpdatePaymentRef = { viewModel.handleIntent(BookingIntent.UpdateReference(it)) },
        onConfirmPayment = { viewModel.handleIntent(BookingIntent.ConfirmPayment) },
        onDismissPayment = { viewModel.handleIntent(BookingIntent.HidePaymentSheet) },
        onCopyPaymentData = {
            val banco = uiState.selectedSede?.datosPago?.get("banco") ?: "Banesco"
            val telefono = uiState.selectedSede?.datosPago?.get("telefono") ?: "04122168050"
            val rif = uiState.selectedSede?.datosPago?.get("rif") ?: "23849761"
            val allData = "Banco: $banco\nTeléfono: $telefono\nRIF: $rif"
            clipboardManager.setText(AnnotatedString(allData))
            Toast.makeText(context, "Datos de pago copiados", Toast.LENGTH_SHORT).show()
        },
        onNotificationClick = {
            if (notifications.isNotEmpty()) {
                selectedReceipt = notifications.last()
            }
            onNotificationClick()
        },
        onHistoryClick = onHistoryClick,
        courtImageProvider = { url, modifier ->
            androidx.compose.runtime.key(url) {
                AsyncImage(
                    model = if (url.isNotEmpty()) url else R.drawable.cancha,
                    placeholder = painterResource(R.drawable.cancha),
                    error = painterResource(R.drawable.cancha),
                    contentDescription = null,
                    modifier = modifier,
                    contentScale = ContentScale.Crop
                )
            }
        },
        dateSelectionContent = {
            DateSelectionRow(
                selectedDate = uiState.selectedDate,
                onDateSelected = { viewModel.onDateSelected(it) }
            )
        }
    )

    if (selectedReceipt != null) {
        ReceiptModalShared(
            reserva = selectedReceipt,
            onDismiss = { selectedReceipt = null },
            onDownload = {
                selectedReceipt?.let { reserva ->
                    try {
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            val date = LocalDate.parse(reserva.fecha)
                            val startTime = LocalTime.parse(reserva.horaInicio)
                            val endTime = LocalTime.parse(reserva.horaFin)
                            
                            val startMillis = LocalDateTime.of(date, startTime)
                                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            val endMillis = LocalDateTime.of(date, endTime)
                                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            
                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                            putExtra(CalendarContract.Events.TITLE, "Reserva: ${uiState.canchaName}")
                            putExtra(CalendarContract.Events.DESCRIPTION, "Reserva de cancha en Arena. Ref: ${reserva.referenciaPago}")
                            putExtra(CalendarContract.Events.EVENT_LOCATION, "Arena Sports Center")
                            putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al abrir el calendario", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onShare = {
                selectedReceipt?.let { reserva ->
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Mi reserva en Arena: ${uiState.canchaName} el ${reserva.fecha} a las ${reserva.horaInicio}. Ref: ${reserva.referenciaPago}")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Compartir Reserva"))
                }
            }
        )
    }
}

@Composable
fun DateSelectionRow(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val dates = remember { (0..6).map { LocalDate.now().plusDays(it.toLong()) } }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF141820).copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF2D3748).copy(alpha = 0.1f))
    ) {
        LazyRow(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(dates) { date ->
                val isSelected = date == selectedDate
                Surface(
                    modifier = Modifier
                        .width(70.dp)
                        .clickable { onDateSelected(date) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF00F5FF) else Color(0xFF141820).copy(alpha = 0.3f),
                    border = if (isSelected) BorderStroke(2.dp, Color(0xFF00F5FF)) else null
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color(0xFFB9CACA)
                        )
                        Text(
                            text = date.dayOfMonth.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Booking Schedule Preview")
@Composable
fun BookingSchedulePreview() {
    val mockUiState = BookingScheduleUiStateShared(
        canchaName = "Cancha Central",
        tipoReserva = "JUEGO",
        selectedDate = "2024-05-20",
        totalPrice = 45.0,
        selectedSede = com.example.arena.domain.Sede(nombreSede = "Elite Padel Arena")
    )

    EliteAthleteOSTheme {
        BookingScheduleContentShared(
            uiState = mockUiState,
            onNavigateBack = {},
            onChangeCourt = {},
            onBookClick = {},
            onDismissConfirmation = {},
            onTimeSelected = {},
            onToggleExtra = { _, _ -> },
            onUpdatePaymentRef = {},
            onConfirmPayment = {},
            onDismissPayment = {},
            onCopyPaymentData = {},
            courtImageProvider = { _, _ -> }
        )
    }
}
