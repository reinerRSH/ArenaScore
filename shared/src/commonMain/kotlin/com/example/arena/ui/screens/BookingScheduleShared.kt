package com.example.arena.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.arena.domain.Reserva
import com.example.arena.domain.Sede
import com.example.arena.ui.theme.*
import com.example.arena.ui.components.OutlinedTextFieldShared
import com.example.arena.ui.components.ArenaTopBarShared

fun formatTo12hShared(time24h: String): String {
    return try {
        val parts = time24h.split(":")
        var hour = parts[0].toInt()
        val minutes = parts[1]
        val suffix = if (hour >= 12 && hour < 24) "PM" else "AM"
        
        if (hour == 0) hour = 12
        else if (hour > 12) hour -= 12
        
        "$hour:$minutes $suffix"
    } catch (e: Exception) {
        time24h
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScheduleContentShared(
    uiState: BookingScheduleUiStateShared,
    notifications: List<Reserva> = emptyList(),
    onNavigateBack: () -> Unit,
    onChangeCourt: () -> Unit,
    onBookClick: () -> Unit,
    onDismissConfirmation: () -> Unit,
    onTimeSelected: (String) -> Unit,
    onInstructorSelected: (String) -> Unit = {},
    onToggleExtra: (String, Double) -> Unit,
    onUpdatePaymentRef: (String) -> Unit,
    onConfirmPayment: () -> Unit,
    onDismissPayment: () -> Unit,
    onCopyPaymentData: () -> Unit,
    onNotificationClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    courtImageProvider: @Composable (String, Modifier) -> Unit = { _, _ -> },
    dateSelectionContent: @Composable () -> Unit = {}
) {
    val masterSlots = listOf("06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00", "00:00", "01:00")
    val scale = mapOf("06:00" to 6, "07:00" to 7, "08:00" to 8, "09:00" to 9, "10:00" to 10, "11:00" to 11, "12:00" to 12, "13:00" to 13, "14:00" to 14, "15:00" to 15, "16:00" to 16, "17:00" to 17, "18:00" to 18, "19:00" to 19, "20:00" to 20, "21:00" to 21, "22:00" to 22, "23:00" to 23, "00:00" to 24, "01:00" to 25)
    
    val normalize = { t: String -> if (t.length == 4) "0$t" else t }
    val openingScale = scale[normalize(uiState.selectedSede?.horaApertura ?: "06:00")] ?: 6
    val closingScale = scale[normalize(uiState.selectedSede?.horaCierre ?: "01:00")] ?: 25
    val availableSlots = masterSlots.filter { val s = scale[it] ?: 0; s >= openingScale && s <= closingScale }

    Scaffold(
        topBar = {
            ArenaTopBarShared(
                onNavigateBack = onNavigateBack,
                notificationCount = notifications.size,
                onNotificationClick = onNotificationClick,
                onHistoryClick = onHistoryClick
            )
        },
        bottomBar = {
            BookingBottomBarShared(
                totalPrice = "$${uiState.totalPrice}",
                onBookClick = onBookClick,
                enabled = uiState.selectedTime != null && !uiState.isLoading,
                isLoading = uiState.isLoading,
                isClassMode = uiState.tipoReserva == "CLASES"
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
                    onCopyData = onCopyPaymentData,
                    onToggleExtra = onToggleExtra
                )
            }

            if (uiState.isConfirmationVisible) {
                BookingConfirmationModalShared(
                    reserva = uiState.lastReserva,
                    onDismiss = onDismissConfirmation
                )
            }

            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Text("RESERVAR SESIÓN", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White, maxLines = 1)
                Text("Selecciona fecha y hora para asegurar tu reserva.", fontSize = 9.sp, color = ArenaTextVariant, maxLines = 1)
                Spacer(modifier = Modifier.height(24.dp))
                FacilitySummaryCardShared(
                    canchaName = uiState.canchaName, 
                    tipo = uiState.tipoReserva, 
                    onChangeClick = onChangeCourt, 
                    imageUrl = uiState.canchaImageUrl,
                    imageProvider = courtImageProvider
                )
                
                if (uiState.tipoReserva == "CLASES") {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("INSTRUCTOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ArenaTextVariant, maxLines = 1)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.instructores) { instructor ->
                            val isSelected = instructor.id == uiState.selectedInstructorId
                            Surface(
                                modifier = Modifier.clickable { onInstructorSelected(instructor.id) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) ArenaPrimaryContainer else ArenaSurfaceElevated,
                                border = if (isSelected) null else BorderStroke(1.dp, ArenaStaffBorder)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Person, null, tint = if (isSelected) Color.Black else Color.White)
                                    Text(instructor.nombre, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.Black else Color.White, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text("FECHA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ArenaTextVariant, maxLines = 1)
                Spacer(modifier = Modifier.height(12.dp))
                dateSelectionContent()

                Spacer(modifier = Modifier.height(24.dp))

                Surface(modifier = Modifier.fillMaxWidth(), color = ArenaSurfaceElevated.copy(alpha = 0.8f), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.2f))) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("TURNOS DISPONIBLES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                        Spacer(modifier = Modifier.height(20.dp))
                        TimeSlotsSectionShared(
                            uiState = uiState, 
                            onTimeSelected = onTimeSelected, 
                            allSlots = availableSlots
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun BookingBottomBarShared(totalPrice: String, onBookClick: () -> Unit, enabled: Boolean, isLoading: Boolean, isClassMode: Boolean = false) {
    Surface(color = ArenaSurfaceElevated, tonalElevation = 8.dp, border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.1f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            if (!isClassMode) {
                Column {
                    Text("TOTAL", fontSize = 9.sp, color = ArenaTextVariant, fontWeight = FontWeight.Bold)
                    Text(totalPrice, fontSize = 20.sp, fontWeight = FontWeight.Black, color = ArenaPrimaryContainer)
                }
            }
            Button(
                onClick = onBookClick, 
                enabled = enabled, 
                modifier = if (isClassMode) Modifier.fillMaxWidth() else Modifier,
                shape = RoundedCornerShape(8.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer, contentColor = Color.Black)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                else Text(if (isClassMode) "RESERVAR CLASE" else "RESERVAR AHORA", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun FacilitySummaryCardShared(
    canchaName: String, 
    tipo: String, 
    onChangeClick: () -> Unit, 
    imageUrl: String = "",
    imageProvider: @Composable (String, Modifier) -> Unit = { _, _ -> }
) {
    Surface(color = ArenaSurfaceElevated.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.1f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray)) {
                imageProvider(imageUrl, Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(canchaName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Tipo: $tipo", fontSize = 10.sp, color = ArenaTextVariant)
            }
            TextButton(onClick = onChangeClick) { Text("CAMBIAR", color = ArenaPrimaryContainer, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun TimeSlotsSectionShared(
    uiState: BookingScheduleUiStateShared,
    onTimeSelected: (String) -> Unit, 
    allSlots: List<String>
) {
    val morningSlots = allSlots.filter { it in listOf("06:00", "07:00", "08:00", "09:00", "10:00", "11:00") }
    val afternoonSlots = allSlots.filter { it in listOf("12:00", "13:00", "14:00", "15:00", "16:00", "17:00") }
    val eveningSlots = allSlots.filter { it in listOf("18:00", "19:00", "20:00", "21:00", "22:00", "23:00", "00:00", "01:00") }
    
    Column {
        if (morningSlots.isNotEmpty()) { 
            TimeGridSectionShared("MAÑANA", morningSlots, uiState, onTimeSelected)
            Spacer(modifier = Modifier.height(20.dp)) 
        }
        if (afternoonSlots.isNotEmpty()) { 
            TimeGridSectionShared("TARDE", afternoonSlots, uiState, onTimeSelected)
            Spacer(modifier = Modifier.height(20.dp)) 
        }
        if (eveningSlots.isNotEmpty()) { 
            TimeGridSectionShared("NOCHE", eveningSlots, uiState, onTimeSelected) 
        }
    }
}

@Composable
fun TimeGridSectionShared(
    title: String, 
    slots: List<String>, 
    uiState: BookingScheduleUiStateShared,
    onTimeSelected: (String) -> Unit
) {
    Column {
        Text(text = title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ArenaTextVariant, maxLines = 1)
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            slots.chunked(3).forEach { rowSlots ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowSlots.forEach { time ->
                        val isSelected = time == uiState.selectedTime
                        val isReserved = uiState.reservedSlots.contains(time)
                        
                        // Lógica de visualización por modo
                        val instructorMode = uiState.instructorSlots[time] // "V" o "A" o null
                        val isClassMode = uiState.tipoReserva == "CLASES"
                        
                        val isClickable = if (isClassMode) {
                            if (uiState.selectedInstructorId == null) !isReserved 
                            else instructorMode == "V" && !isReserved
                        } else {
                            !isReserved
                        }

                        val cellColor = when {
                            isSelected -> ArenaPrimaryContainer
                            isClassMode -> {
                                if (uiState.selectedInstructorId == null) {
                                    if (isReserved) ArenaWarning.copy(alpha = 0.1f) else ArenaSurfaceElevated.copy(alpha = 0.2f)
                                } else {
                                    when (instructorMode) {
                                        "V" -> if (isReserved) ArenaWarning.copy(alpha = 0.1f) else ArenaSuccess.copy(alpha = 0.2f)
                                        "A" -> Color(0xFFFFD700).copy(alpha = 0.2f) 
                                        else -> ArenaWarning.copy(alpha = 0.1f) 
                                    }
                                }
                            }
                            isReserved -> ArenaWarning.copy(alpha = 0.1f)
                            else -> ArenaSurfaceElevated.copy(alpha = 0.2f)
                        }

                        val borderColor = when {
                            isSelected -> ArenaPrimaryContainer
                            isClassMode -> {
                                if (uiState.selectedInstructorId == null) {
                                    if (isReserved) ArenaWarning.copy(alpha = 0.3f) else ArenaPrimaryContainer.copy(alpha = 0.4f)
                                } else {
                                    when (instructorMode) {
                                        "V" -> if (isReserved) ArenaWarning.copy(alpha = 0.3f) else ArenaSuccess.copy(alpha = 0.4f)
                                        "A" -> Color(0xFFFFD700).copy(alpha = 0.4f)
                                        else -> ArenaWarning.copy(alpha = 0.3f)
                                    }
                                }
                            }
                            isReserved -> ArenaWarning.copy(alpha = 0.3f)
                            else -> ArenaPrimaryContainer.copy(alpha = 0.4f)
                        }

                        val textColor = when {
                            isSelected -> Color.Black
                            isClassMode -> {
                                if (uiState.selectedInstructorId == null) {
                                    if (isReserved) ArenaWarning.copy(alpha = 0.5f) else Color.White
                                } else {
                                    when (instructorMode) {
                                        "V" -> if (isReserved) ArenaWarning.copy(alpha = 0.5f) else Color.White
                                        "A" -> Color(0xFFFFD700).copy(alpha = 0.8f)
                                        else -> ArenaWarning.copy(alpha = 0.5f)
                                    }
                                }
                            }
                            isReserved -> ArenaWarning.copy(alpha = 0.5f)
                            else -> Color.White
                        }

                        Surface(
                            modifier = Modifier.weight(1f).then(if (isClickable) Modifier.clickable { onTimeSelected(time) } else Modifier),
                            shape = RoundedCornerShape(8.dp),
                            color = cellColor,
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (isClassMode && uiState.selectedInstructorId != null && instructorMode == "V") {
                                    val remaining = uiState.slotsRemaining[time] ?: 5
                                    Text(
                                        if (remaining > 0) "$remaining CUPOS" else "LLENO",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (remaining > 0) ArenaSuccess else ArenaWarning
                                    )
                                }
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text(text = formatTo12hShared(time), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1)
                                }
                            }
                        }
                    }
                    repeat(3 - rowSlots.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheetShared(
    state: BookingScheduleUiStateShared, 
    onReferenceChanged: (String) -> Unit, 
    onConfirm: () -> Unit, 
    onDismiss: () -> Unit, 
    onCopyData: () -> Unit,
    onToggleExtra: (String, Double) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss, 
        containerColor = ArenaSurfaceElevated, 
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()) // Permite scroll cuando sale el teclado
                .imePadding() // Añade padding dinámico según el teclado
        ) {
            Text("CONFIRMAR PAGO MÓVIL", fontSize = 20.sp, fontWeight = FontWeight.Black, color = ArenaPrimaryContainer)
            Spacer(modifier = Modifier.height(16.dp))
            Surface(color = Color.Black.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Destinatario: ARENA SPORTS CENTER", fontSize = 10.sp, color = Color.Gray)
                    Text("Banco: ${state.selectedSede?.datosPago?.get("banco") ?: "Banesco"}", fontWeight = FontWeight.Bold)
                    Text("Teléfono: ${state.selectedSede?.datosPago?.get("telefono") ?: "04122168050"}", fontWeight = FontWeight.Bold)
                    Text("Documento: ${state.selectedSede?.datosPago?.get("documento") ?: "23849761"}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onCopyData, modifier = Modifier.height(32.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer.copy(alpha = 0.1f), contentColor = ArenaPrimaryContainer), border = BorderStroke(1.dp, ArenaPrimaryContainer.copy(alpha = 0.5f))) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COPIAR DATOS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val availableExtras = state.selectedSede?.extras ?: emptyMap()
            if (availableExtras.isNotEmpty()) {
                Text("¿NECESITAS EXTRAS?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ArenaTextVariant)
                Spacer(modifier = Modifier.height(12.dp))
                availableExtras.forEach { (name, price) ->
                    val isSelected = state.selectedExtras.containsKey(name)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Checkbox(checked = isSelected, onCheckedChange = { onToggleExtra(name, price) }, colors = CheckboxDefaults.colors(checkedColor = ArenaPrimaryContainer))
                        Text(name, modifier = Modifier.weight(1f), fontSize = 14.sp)
                        if (isSelected) {
                           Text("+$${price}", color = ArenaPrimaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("TOTAL: $${state.totalPrice}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ArenaPrimaryContainer)
            Spacer(modifier = Modifier.height(16.dp))
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
fun BookingConfirmationModalShared(reserva: Reserva? = null, onDismiss: () -> Unit) {
    val isClass = reserva?.referenciaPago == "CLASE_PREPAGADA"
    
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = true)) {
        Surface(modifier = Modifier.fillMaxWidth().wrapContentHeight(), shape = RoundedCornerShape(28.dp), color = ArenaSurfaceElevated, border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.2f))) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(if (isClass) Icons.Default.CheckCircle else Icons.Default.Info, null, tint = if (isClass) ArenaSuccess else ArenaPrimaryContainer, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isClass) {
                    Text("CLASE RESERVADA", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 1)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Usuario: ${reserva?.nombreUsuario}", color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Cancha: ${reserva?.canchaid}", color = ArenaTextVariant, fontSize = 12.sp, maxLines = 1)
                    Text("Hora: ${reserva?.horaInicio}", color = ArenaPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                } else {
                    Text("PAGO EN VERIFICACIÓN", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 1)
                    Text("El administrador validará tu pago en breve.", color = ArenaTextVariant, fontSize = 10.sp, textAlign = TextAlign.Center)
                }
                
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
    val instructores: List<com.example.arena.domain.Instructor> = emptyList(),
    val selectedInstructorId: String? = null,
    val instructorSlots: Map<String, String> = emptyMap(),
    val canchaName: String = "",
    val tipoReserva: String = "",
    val selectedSede: Sede? = null,
    val selectedExtras: Map<String, Double> = emptyMap(),
    val totalPrice: Double = 0.0,
    val paymentReference: String = "",
    val isPaymentSheetVisible: Boolean = false,
    val isConfirmationVisible: Boolean = false,
    val isLoading: Boolean = false,
    val canchaImageUrl: String = "",
    val lastReserva: Reserva? = null,
    val slotsRemaining: Map<String, Int> = emptyMap()
)
