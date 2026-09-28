package com.example.arena.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arena.domain.Cancha
import com.example.arena.domain.Reserva
import com.example.arena.domain.Sede

// Theme Constants (Shared)
val ArenaSurfaceBase = Color(0xFF0A0C10)
val ArenaSurfaceElevated = Color(0xFF141820)
val ArenaPrimaryContainer = Color(0xFF00F5FF)
val ArenaTextVariant = Color(0xFFB9CACA)
val ArenaSuccess = Color(0xFF39FF14)
val ArenaWarning = Color(0xFFEF4444)
val ArenaStaffBorder = Color(0xFF2D3748)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardContent(
    uiState: AdminUiStateShared,
    onNavigateBack: () -> Unit,
    onToggleCancha: (String, String) -> Unit,
    onUpdatePrice: (Double) -> Unit,
    onUpdateExtras: (Map<String, Double>) -> Unit,
    onVerifyPayment: (String, Boolean) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("RESUMEN", "CANCHAS", "PRECIOS", "PAGOS")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ADMIN COMMAND CENTER",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
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
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ArenaSurfaceBase,
                    contentColor = ArenaPrimaryContainer,
                    divider = { HorizontalDivider(color = ArenaStaffBorder.copy(alpha = 0.5f)) },
                    edgePadding = 16.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    when (selectedTab) {
                        0 -> SummaryTab(uiState)
                        1 -> CourtsTab(uiState, onToggleCancha)
                        2 -> PricingTab(uiState, onUpdatePrice, onUpdateExtras)
                        3 -> PaymentsTab(uiState, onVerifyPayment)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryTab(state: AdminUiStateShared) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            AdminCard(title = "ESTADO DE LA SEDE", icon = Icons.Default.Dashboard) {
                Column {
                    DetailRow("Nombre", state.selectedSede?.nombreSede ?: "-")
                    DetailRow("Ubicación", state.selectedSede?.ubicacion ?: "-")
                    DetailRow("Suscripción", state.selectedSede?.suscripcionStatus ?: "-", color = ArenaSuccess)
                }
            }
        }
        item {
            AdminCard(title = "MÉTRICAS RÁPIDAS", icon = Icons.Default.Analytics) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricBox("Canchas", state.canchas.size.toString())
                    MetricBox("Pendientes", state.pendingPayments.size.toString(), color = ArenaWarning)
                    MetricBox("Activas", state.canchas.count { it.estado == "ACTIVA" }.toString(), color = ArenaSuccess)
                }
            }
        }
    }
}

@Composable
fun CourtsTab(state: AdminUiStateShared, onToggleCancha: (String, String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(state.canchas) { cancha ->
            AdminCard(title = cancha.nombre, icon = Icons.Default.SportsTennis) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tipo: ${cancha.tipo}", fontSize = 12.sp, color = ArenaTextVariant)
                        Text(
                            text = if (cancha.estado == "ACTIVA") "OPERATIVA" else "FUERA DE SERVICIO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cancha.estado == "ACTIVA") ArenaSuccess else ArenaWarning,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = cancha.estado == "ACTIVA",
                        onCheckedChange = { onToggleCancha(cancha.id, cancha.estado) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ArenaPrimaryContainer,
                            checkedTrackColor = ArenaPrimaryContainer.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PricingTab(
    state: AdminUiStateShared,
    onUpdatePrice: (Double) -> Unit,
    onUpdateExtras: (Map<String, Double>) -> Unit
) {
    var basePrice by remember(state.selectedSede) { mutableStateOf(state.selectedSede?.precioBase?.toString() ?: "0.0") }
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item {
            AdminCard(title = "PRECIO BASE POR HORA", icon = Icons.Default.AttachMoney) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = basePrice,
                        onValueChange = { basePrice = it },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, color = Color.White),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ArenaPrimaryContainer)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { basePrice.toDoubleOrNull()?.let { onUpdatePrice(it) } },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer, contentColor = Color.Black)
                    ) {
                        Text("GUARDAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            AdminCard(title = "GESTIÓN DE EXTRAS", icon = Icons.Default.Inventory2) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.selectedSede?.extras?.forEach { (name, price) ->
                        var extraPrice by remember { mutableStateOf(price.toString()) }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(name, modifier = Modifier.weight(1f), color = Color.White, fontSize = 14.sp)
                            OutlinedTextField(
                                value = extraPrice,
                                onValueChange = { extraPrice = it },
                                modifier = Modifier.width(80.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            IconButton(onClick = {
                                val currentExtras = state.selectedSede.extras.toMutableMap()
                                extraPrice.toDoubleOrNull()?.let { 
                                    currentExtras[name] = it
                                    onUpdateExtras(currentExtras)
                                }
                            }) {
                                Icon(Icons.Default.Save, contentDescription = "Guardar", tint = ArenaPrimaryContainer, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentsTab(state: AdminUiStateShared, onVerifyPayment: (String, Boolean) -> Unit) {
    if (state.pendingPayments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("NO HAY PAGOS PENDIENTES", color = ArenaTextVariant, fontFamily = FontFamily.Monospace)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.pendingPayments) { reserva ->
                AdminCard(title = "REF: ${reserva.referenciaPago}", icon = Icons.Default.AccountBalanceWallet) {
                    Column {
                        Text("Monto: $${reserva.montoTotal}", color = ArenaPrimaryContainer, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Text("Usuario ID: ${reserva.usuarioid.takeLast(6)}", fontSize = 10.sp, color = ArenaTextVariant)
                        Text("Cancha: ${reserva.canchaid}", fontSize = 12.sp, color = Color.White)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            OutlinedButton(
                                onClick = { onVerifyPayment(reserva.id, false) },
                                border = BorderStroke(1.dp, ArenaWarning),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = ArenaWarning, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RECHAZAR", color = ArenaWarning, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onVerifyPayment(reserva.id, true) },
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ArenaSuccess, contentColor = Color.Black),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("APROBAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        color = ArenaSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = ArenaPrimaryContainer, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = ArenaPrimaryContainer,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, color: Color = Color.White) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = ArenaTextVariant)
        Text(value, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MetricBox(label: String, value: String, color: Color = ArenaPrimaryContainer) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = color, fontFamily = FontFamily.Monospace)
        Text(label.uppercase(), fontSize = 9.sp, color = ArenaTextVariant, fontWeight = FontWeight.Bold)
    }
}

data class AdminUiStateShared(
    val selectedSede: Sede? = null,
    val canchas: List<Cancha> = emptyList(),
    val pendingPayments: List<Reserva> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)
