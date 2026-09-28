package com.example.arena.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.arena.domain.Reserva
import com.example.arena.domain.Sede
import com.example.arena.ui.theme.*
import com.example.arena.ui.components.OutlinedTextFieldShared

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScheduleContentShared(
    uiState: BookingScheduleUiStateShared,
    onNavigateBack: () -> Unit,
    onChangeCourt: () -> Unit,
    onBookClick: () -> Unit,
    onDismissConfirmation: () -> Unit,
    onDateSelected: (String) -> Unit, // Using String for simplicity in KMP for now
    onTimeSelected: (String) -> Unit,
    onToggleExtra: (String, Double) -> Unit,
    onUpdatePaymentRef: (String) -> Unit,
    onConfirmPayment: () -> Unit,
    onDismissPayment: () -> Unit,
    onCopyPaymentData: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("ARENA", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = ArenaPrimaryContainer)) } },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ArenaPrimaryContainer) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BookingBottomBarShared(
                totalPrice = "$${uiState.totalPrice}",
                onBookClick = onBookClick,
                enabled = uiState.selectedTime != null && !uiState.isLoading,
                isLoading = uiState.isLoading
            )
        },
        containerColor = ArenaSurfaceBase
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isPaymentSheetVisible) {
                PaymentBottomSheetShared(
                    state = uiState,
                    onReferenceChanged = onUpdatePaymentRef,
                    onConfirm = onConfirmPayment,
                    onDismiss = onDismissPayment,
                    onCopyData = onCopyPaymentData
                )
            }

            if (uiState.isConfirmationVisible) {
                BookingConfirmationModalShared(state = uiState, onDismiss = onDismissConfirmation)
            }

            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Text("RESERVAR SESIÓN", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("Selecciona fecha y hora para asegurar tu reserva.", fontSize = 12.sp, color = ArenaTextVariant)
                Spacer(modifier = Modifier.height(24.dp))
                FacilitySummaryCardShared(canchaName = uiState.canchaName, tipo = uiState.tipoReserva, onChangeClick = onChangeCourt)
                Spacer(modifier = Modifier.height(24.dp))
                ExtrasSectionShared(availableExtras = uiState.selectedSede?.extras ?: emptyMap(), selectedExtras = uiState.selectedExtras, onToggleExtra = onToggleExtra)
                Spacer(modifier = Modifier.height(24.dp))
                Text("FECHA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ArenaTextVariant, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(12.dp))
                
                // Simplified Date Row for now
                Surface(modifier = Modifier.fillMaxWidth(), color = ArenaSurfaceElevated.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.1f))) {
                   Text("Selector de Fecha (Multiplataforma)", modifier = Modifier.padding(16.dp), color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Surface(modifier = Modifier.fillMaxWidth(), color = ArenaSurfaceElevated.copy(alpha = 0.8f), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.2f))) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("TURNOS DISPONIBLES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(20.dp))
                        TimeSlotsSectionShared(selectedTime = uiState.selectedTime, reservedSlots = uiState.reservedSlots, onTimeSelected = onTimeSelected)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun BookingBottomBarShared(totalPrice: String, onBookClick: () -> Unit, enabled: Boolean, isLoading: Boolean) {
    Surface(color = ArenaSurfaceElevated, tonalElevation = 8.dp, border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.1f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("TOTAL", fontSize = 10.sp, color = ArenaTextVariant, fontWeight = FontWeight.Bold)
                Text(totalPrice, fontSize = 24.sp, fontWeight = FontWeight.Black, color = ArenaPrimaryContainer)
            }
            Button(onClick = onBookClick, enabled = enabled, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer, contentColor = Color.Black)) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                else Text("RESERVAR AHORA", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun FacilitySummaryCardShared(canchaName: String, tipo: String, onChangeClick: () -> Unit) {
    Surface(color = ArenaSurfaceElevated.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.1f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).background(Color.Gray, RoundedCornerShape(8.dp)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(canchaName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Tipo: $tipo", fontSize = 12.sp, color = ArenaTextVariant)
            }
            TextButton(onClick = onChangeClick) { Text("CAMBIAR", color = ArenaPrimaryContainer, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun ExtrasSectionShared(availableExtras: Map<String, Double>, selectedExtras: Map<String, Double>, onToggleExtra: (String, Double) -> Unit) {
    if (availableExtras.isEmpty()) return
    Column {
        Text("SERVICIOS ADICIONALES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ArenaTextVariant, fontFamily = FontFamily.Monospace)
        availableExtras.forEach { (name, price) ->
            val isSelected = selectedExtras.containsKey(name)
            Surface(color = if (isSelected) ArenaPrimaryContainer.copy(alpha = 0.1f) else Color.Transparent, border = BorderStroke(1.dp, if (isSelected) ArenaPrimaryContainer else ArenaStaffBorder.copy(alpha = 0.2f)), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onToggleExtra(name, price) }) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isSelected, onCheckedChange = { onToggleExtra(name, price) }, colors = CheckboxDefaults.colors(checkedColor = ArenaPrimaryContainer))
                    Text(name, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text("+$${price}", color = ArenaPrimaryContainer, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TimeSlotsSectionShared(selectedTime: String?, reservedSlots: List<String>, onTimeSelected: (String) -> Unit) {
    val slots = listOf("08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        slots.chunked(3).forEach { rowSlots ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowSlots.forEach { time ->
                    val isSelected = time == selectedTime
                    val isReserved = reservedSlots.contains(time)
                    Surface(
                        modifier = Modifier.weight(1f).then(if (!isReserved) Modifier.clickable { onTimeSelected(time) } else Modifier),
                        shape = RoundedCornerShape(8.dp),
                        color = when { isSelected -> ArenaPrimaryContainer; isReserved -> Color.Gray.copy(alpha = 0.1f); else -> ArenaSurfaceElevated.copy(alpha = 0.2f) },
                        border = BorderStroke(1.dp, if (isSelected) ArenaPrimaryContainer else ArenaStaffBorder.copy(alpha = 0.1f))
                    ) {
                        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(time, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.Black else if (isReserved) Color.Gray else Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheetShared(state: BookingScheduleUiStateShared, onReferenceChanged: (String) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit, onCopyData: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ArenaSurfaceElevated, contentColor = Color.White) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Text("CONFIRMAR PAGO MÓVIL", fontSize = 20.sp, fontWeight = FontWeight.Black, color = ArenaPrimaryContainer)
            Spacer(modifier = Modifier.height(16.dp))
            Surface(color = Color.Black.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Destinatario: ARENA SPORTS CENTER", fontSize = 10.sp, color = Color.Gray)
                    Text("Banco: ${state.selectedSede?.datosPago?.get("banco") ?: "Banesco"}", fontWeight = FontWeight.Bold)
                    Text("Teléfono: ${state.selectedSede?.datosPago?.get("telefono") ?: "04122168050"}", fontWeight = FontWeight.Bold)
                    Text("RIF: ${state.selectedSede?.datosPago?.get("rif") ?: "23849761"}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onCopyData, modifier = Modifier.height(32.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer.copy(alpha = 0.1f), contentColor = ArenaPrimaryContainer), border = BorderStroke(1.dp, ArenaPrimaryContainer.copy(alpha = 0.5f))) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COPIAR DATOS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextFieldShared(value = state.paymentReference, onValueChange = onReferenceChanged, label = "Número de Referencia")
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), enabled = state.paymentReference.isNotEmpty() && !state.isLoading) {
                if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                else Text("CONFIRMAR PAGO", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun BookingConfirmationModalShared(state: BookingScheduleUiStateShared, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = true)) {
        Surface(modifier = Modifier.fillMaxWidth().wrapContentHeight(), shape = RoundedCornerShape(28.dp), color = ArenaSurfaceElevated, border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.2f))) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AccessTime, null, tint = ArenaPrimaryContainer, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("PAGO EN VERIFICACIÓN", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Text("El administrador validará tu pago en breve.", color = ArenaTextVariant, fontSize = 12.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Cerrar", color = Color.White) }
            }
        }
    }
}

data class BookingScheduleUiStateShared(
    val selectedDate: String = "",
    val selectedTime: String? = null,
    val reservedSlots: List<String> = emptyList(),
    val canchaName: String = "",
    val tipoReserva: String = "",
    val selectedSede: Sede? = null,
    val selectedExtras: Map<String, Double> = emptyMap(),
    val totalPrice: Double = 0.0,
    val paymentReference: String = "",
    val isPaymentSheetVisible: Boolean = false,
    val isConfirmationVisible: Boolean = false,
    val isLoading: Boolean = false
)
