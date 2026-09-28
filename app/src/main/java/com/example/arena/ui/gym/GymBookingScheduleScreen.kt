package com.example.arena.ui.gym

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.arena.View.ui.theme.*
import com.example.arena.domain.Reserva
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymBookingScheduleScreen(
    sedeId: String,
    classId: String,
    className: String,
    tokenCost: Int = 1,
    onNavigateBack: () -> Unit,
    onBookingConfirmed: (Reserva) -> Unit
) {
    val context = LocalContext.current
    var userTokens by remember { mutableIntStateOf(15) }
    var isProcessing by remember { mutableStateOf(false) }
    var confirmedPass by remember { mutableStateOf<Reserva?>(null) }

    val selectedClass = remember(classId) {
        mockGymClasses.find { it.id == classId } ?: mockGymClasses.first()
    }

    val todayStr = remember { LocalDate.now().toString() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("RESERVA DE CLASE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text(selectedClass.name, color = ArenaTextVariant, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = ArenaPrimaryContainer)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ArenaSurfaceBase)
            )
        },
        containerColor = ArenaSurfaceBase
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Card de Membresía & Saldo de Tokens
                Surface(
                    color = ArenaSurfaceElevated,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ArenaSuccess.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, null, tint = ArenaSuccess, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("MEMBRESÍA VIP CENTRO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                            Surface(color = ArenaSuccess.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                Text("MENSUALIDAD AL DÍA", color = ArenaSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = ArenaStaffBorder)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("SALDO DE TOKENS", color = ArenaTextVariant, fontSize = 10.sp)
                                Text("🪙 $userTokens TOKENS", color = ArenaPrimaryContainer, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("COSTO DE RESERVA", color = ArenaTextVariant, fontSize = 10.sp)
                                Text("- $tokenCost TOKEN", color = ArenaWarning, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detalle de la Clase Seleccionada
                Surface(
                    color = ArenaSurfaceElevated,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DETALLE DE LA SESIÓN", color = ArenaPrimaryContainer, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        DetailRow("Clase", selectedClass.name)
                        DetailRow("Categoría", selectedClass.category)
                        DetailRow("Horario", selectedClass.scheduleTime, color = ArenaSuccess)
                        DetailRow("Entrenador", selectedClass.coachName)
                        DetailRow("Fecha", todayStr)
                        DetailRow("Validación Admin", "SIN ESPERA (INSTANTÁNEO)", color = ArenaSuccess)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Garantía de Confirmación Instantánea
                Surface(
                    color = ArenaSuccess.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ArenaSuccess.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Bolt, null, tint = ArenaSuccess, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Al contar con membresía activa, tu cupo en la clase se confirma de forma instantánea al descontar tu token.",
                            color = Color.White,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm Action Button
                Button(
                    onClick = {
                        if (userTokens < tokenCost) {
                            Toast.makeText(context, "Saldo de tokens insuficiente", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isProcessing = true
                        userTokens -= tokenCost

                        val createdReserva = Reserva(
                            id = "gym_res_${System.currentTimeMillis()}",
                            canchaid = selectedClass.name,
                            estado = Reserva.STATUS_ACTIVA, // INSTANTÁNEO SIN ESPERA DE ADMIN
                            fecha = todayStr,
                            horaInicio = selectedClass.scheduleTime.split(" - ").firstOrNull() ?: "07:00 AM",
                            horaFin = selectedClass.scheduleTime.split(" - ").lastOrNull() ?: "08:00 AM",
                            sedeid = sedeId,
                            usuarioid = "user_current",
                            nombreUsuario = "ATLETA VIP",
                            referenciaPago = "TOKEN_MEMBRESIA",
                            montoTotal = 0.0,
                            canchaImageUrl = selectedClass.imageUrl,
                            instructorId = selectedClass.coachName
                        )

                        isProcessing = false
                        confirmedPass = createdReserva
                        onBookingConfirmed(createdReserva)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !isProcessing && userTokens >= tokenCost,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArenaSuccess, contentColor = Color.Black)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CONFIRMAR RESERVA CON TOKENS", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }

            // Modal de Pase Digital Instantáneo
            if (confirmedPass != null) {
                val pass = confirmedPass!!
                Dialog(onDismissRequest = { confirmedPass = null }) {
                    Surface(
                        color = ArenaSurfaceElevated,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, ArenaSuccess),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.TaskAlt, null, tint = ArenaSuccess, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("¡CLASE CONFIRMADA!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text("Pase de Acceso Digital Generado", color = ArenaSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = ArenaStaffBorder)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Clase: ${pass.canchaid}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
                            Text("Horario: ${pass.horaInicio} - ${pass.horaFin}", color = ArenaTextVariant, fontSize = 12.sp)
                            Text("Coach: ${pass.instructorId}", color = ArenaPrimaryContainer, fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(140.dp).padding(8.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.QrCode2, null, tint = Color.Black, modifier = Modifier.fillMaxSize())
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    confirmedPass = null
                                    onNavigateBack()
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer, contentColor = Color.Black)
                            ) {
                                Text("ENTENDIDO", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, color: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = ArenaTextVariant)
        Text(value, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Preview(showBackground = true, name = "Reserva con Tokens Gym")
@Composable
fun GymBookingScheduleScreenPreview() {
    EliteAthleteOSTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            GymBookingScheduleScreen(
                sedeId = "sede_gym_1",
                classId = "class_1",
                className = "Crossfit WOD High-Octane",
                tokenCost = 1,
                onNavigateBack = {},
                onBookingConfirmed = {}
            )
        }
    }
}
