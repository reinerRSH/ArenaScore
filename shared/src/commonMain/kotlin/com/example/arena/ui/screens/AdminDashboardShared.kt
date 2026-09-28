package com.example.arena.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.arena.domain.Cancha
import com.example.arena.domain.Reserva
import com.example.arena.domain.Sede
import com.example.arena.domain.Instructor
import com.example.arena.domain.SlotStatus
import com.example.arena.domain.BookingLogicShared
import com.example.arena.ui.components.ScanlineEffectShared
import com.example.arena.util.servicesList

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
    initialTab: Int = 0,
    onNavigateBack: () -> Unit,
    onToggleCancha: (String, String) -> Unit,
    onUpdateCancha: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> },
    onPickImage: (String) -> Unit = {},
    onUpdateServicios: (List<String>) -> Unit = {},
    onUpdatePagoMovil: (String, String, String) -> Unit = { _, _, _ -> },
    onSaveInstructor: (Instructor) -> Unit = {},
    onRemoveInstructor: (String) -> Unit = {},
    onUpdatePrice: (Double) -> Unit,
    onUpdateExtras: (Map<String, Double>) -> Unit,
    onVerifyPayment: (String, Boolean) -> Unit,
    onShowReceipt: (Reserva) -> Unit = {},
    onManualBookings: (String, List<String>) -> Unit = { _, _ -> },
    onCancelReservation: (String) -> Unit = {},
    onCourtSelectedForSchedule: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    courtImageProvider: @Composable (String, Modifier) -> Unit = { _, _ -> }
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val tabs = listOf("RESUMEN", "HORARIO", "GESTIÓN CANCHAS", "PRECIOS", "PAGOS", "INSTRUCTORES")
    var showNotifications by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ADMIN COMMAND CENTER",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = ArenaPrimaryContainer)
                    }
                },
                actions = {
                    IconButton(onClick = { showNotifications = true }) {
                        BadgedBox(badge = {
                            if (uiState.pendingPayments.isNotEmpty()) {
                                Badge(containerColor = ArenaWarning) { 
                                    Text(uiState.pendingPayments.size.toString()) 
                                }
                            }
                        }) {
                            Icon(Icons.Default.Notifications, "Notifications", tint = ArenaPrimaryContainer)
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = ArenaPrimaryContainer)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ArenaSurfaceBase)
            )
        },
        containerColor = ArenaSurfaceBase
    ) { paddingValue ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValue)) {
            ScanlineEffectShared()

            if (showNotifications) {
                AdminNotificationsModal(
                    pendingPayments = uiState.pendingPayments,
                    onDismiss = { showNotifications = false },
                    onNotificationClick = {
                        selectedTab = 4 // PAGOS es el índice 4
                        showNotifications = false
                    }
                )
            }

            Column {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ArenaSurfaceBase,
                    contentColor = ArenaPrimaryContainer,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ArenaPrimaryContainer
                        )
                    },
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
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    when (selectedTab) {
                        0 -> SummaryTab(
                            state = uiState, 
                            onUpdateServicios = onUpdateServicios
                        )
                        1 -> ScheduleTab(
                            state = uiState,
                            onManualBookings = onManualBookings,
                            onCancelReservation = onCancelReservation,
                            onCourtSelected = onCourtSelectedForSchedule
                        )
                        2 -> CourtsTab(uiState, onToggleCancha, onUpdateCancha, onPickImage, courtImageProvider)
                        3 -> PricingTab(uiState, onUpdatePrice, onUpdateExtras, onUpdatePagoMovil)
                        4 -> PaymentsTab(uiState, onVerifyPayment)
                        5 -> InstructorsTab(uiState, onSaveInstructor, onRemoveInstructor)
                    }
                    
                    if (uiState.isImageUploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = ArenaPrimaryContainer,
                                trackColor = ArenaSurfaceElevated
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryTab(
    state: AdminUiStateShared, 
    onUpdateServicios: (List<String>) -> Unit = {}
) {
    val currentTags = state.selectedSede?.tags ?: emptyList()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            AdminCard(title = "SERVICIOS DE LA SEDE (TAGS)", icon = Icons.Default.Settings) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    servicesList.forEach { service ->
                        val isChecked = currentTags.any { it.equals(service.name, ignoreCase = true) }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                val newList = if (isChecked) {
                                    currentTags.filter { !it.equals(service.name, ignoreCase = true) }
                                } else {
                                    currentTags + service.name.lowercase()
                                }
                                onUpdateServicios(newList)
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    val newList = if (checked) {
                                        currentTags + service.name.lowercase()
                                    } else {
                                        currentTags.filter { !it.equals(service.name, ignoreCase = true) }
                                    }
                                    onUpdateServicios(newList)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = ArenaPrimaryContainer,
                                    uncheckedColor = Color.Gray
                                )
                            )
                            Icon(service.icon, null, tint = ArenaPrimaryContainer, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                service.name.uppercase(),
                                color = Color.White,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        item {
            AdminCard(title = "ESTADO DE LA SEDE", icon = Icons.Default.Info) {
                Column {
                    DetailRow("Nombre", state.selectedSede?.nombreSede ?: "-")
                    DetailRow("Ubicación", state.selectedSede?.ubicacion ?: "-")
                    DetailRow(
                        "Suscripción", 
                        state.selectedSede?.suscripcionStatus?.takeIf { it.isNotEmpty() } ?: "-", 
                        color = ArenaSuccess
                    )
                }
            }
        }
        
        item {
            AdminCard(title = "CANCHAS EN JUEGO", icon = Icons.Default.Refresh) {
                if (state.activeMatches.isEmpty()) {
                    Text("No hay partidos en curso", fontSize = 9.sp, color = ArenaTextVariant, maxLines = 1)
                } else {
                    state.activeMatches.forEach { match ->
                        Surface(
                            color = ArenaPrimaryContainer.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, ArenaPrimaryContainer.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, null, tint = ArenaPrimaryContainer, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(match.canchaName, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${match.horaInicio} - ${match.horaFin}", fontSize = 8.sp, color = ArenaTextVariant, maxLines = 1)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Text("JUGANDO", color = ArenaSuccess, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        item {
            AdminCard(title = "MÉTRICAS RÁPIDAS", icon = Icons.Default.DateRange) {
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
fun CourtsTab(
    state: AdminUiStateShared, 
    onToggleCancha: (String, String) -> Unit,
    onUpdateCancha: (String, String, String, String, String) -> Unit,
    onPickImage: (String) -> Unit,
    courtImageProvider: @Composable (String, Modifier) -> Unit
) {
    if (state.canchas.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Info, null, tint = ArenaTextVariant, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "NO HAY CANCHAS REGISTRADAS", 
                    color = ArenaTextVariant, 
                    fontSize = 12.sp
                )
                Text(
                    "Sede ID: ${state.selectedSede?.id ?: "N/A"}",
                    color = ArenaTextVariant.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.canchas, key = { it.id }) { cancha ->
                var isEditing by remember { mutableStateOf(false) }
                var editName by remember(cancha.nombre) { mutableStateOf(cancha.nombre) }
                var editSponsor by remember(cancha.patrocinador) { mutableStateOf(cancha.patrocinador) }
                var editType by remember(cancha.tipo) { mutableStateOf(cancha.tipo) }

                AdminCard(
                    title = if (isEditing) "Editando ${cancha.nombre}" else cancha.nombre,
                    icon = Icons.Default.Build,
                    actions = {
                        IconButton(onClick = { 
                            if (isEditing) {
                                onUpdateCancha(cancha.id, editName, cancha.imageUrl, editSponsor, editType)
                                isEditing = false
                            } else {
                                isEditing = true
                            }
                        }) {
                            Icon(
                                if (isEditing) Icons.Default.Check else Icons.Default.Edit, 
                                null, 
                                tint = if (isEditing) ArenaSuccess else ArenaPrimaryContainer
                            )
                        }
                    }
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f) 
                                .clip(RoundedCornerShape(12.dp))
                                .background(ArenaSurfaceBase)
                                .border(
                                    width = 1.dp,
                                    color = ArenaPrimaryContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onPickImage(cancha.id) }
                        ) {
                            courtImageProvider(cancha.imageUrl, Modifier.fillMaxSize())
                            
                            Surface(
                                color = Color.Black.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.AddAPhoto, 
                                            null, 
                                            tint = ArenaPrimaryContainer,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(
                                            "CAMBIAR FOTO", 
                                            color = ArenaPrimaryContainer, 
                                            fontSize = 10.sp, 
                                            fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (cancha.imageUrl.isEmpty()) {
                            Text(
                                "Sin imagen configurada", 
                                color = ArenaWarning, 
                                fontSize = 10.sp, 
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        if (isEditing) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = editName,
                                    onValueChange = { newValue -> editName = newValue },
                                    label = { Text("Nombre Cancha", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.White)
                                )
                                OutlinedTextField(
                                    value = editSponsor,
                                    onValueChange = { newValue -> editSponsor = newValue },
                                    label = { Text("Patrocinador", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.White)
                                )
                                OutlinedTextField(
                                    value = editType,
                                    onValueChange = { newValue -> editType = newValue },
                                    label = { Text("Tipo (ej. Panorámica, Muro)", fontSize = 10.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.White)
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tipo: ${cancha.tipo}", fontSize = 10.sp, color = ArenaTextVariant)
                                    Text("Patrocinador: ${cancha.patrocinador}", fontSize = 9.sp, color = ArenaTextVariant)
                                    Text(
                                        text = if (cancha.estado == "ACTIVA") "OPERATIVA" else "FUERA DE SERVICIO",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (cancha.estado == "ACTIVA") ArenaSuccess else ArenaWarning,
                                        maxLines = 1
                                    )
                                }
                                
                                Switch(
                                    checked = cancha.estado == "ACTIVA",
                                    onCheckedChange = { isChecked ->
                                        onToggleCancha(cancha.id, if (isChecked) "ACTIVA" else "INACTIVA") 
                                    },
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingTab(
    state: AdminUiStateShared,
    onUpdatePrice: (Double) -> Unit,
    onUpdateExtras: (Map<String, Double>) -> Unit,
    onUpdatePagoMovil: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var basePrice by remember(state.selectedSede) { mutableStateOf(state.selectedSede?.precioBase?.toString() ?: "0.0") }
    var extrasExpanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }

    val bancos = listOf("0102 - BANCO DE VENEZUELA", "0105 - BANCO MERCANTIL", "0108 - BANCO PROVINCIAL", "0114 - BANCO DEL CARIBE", "0115 - BANCO EXTERIOR", "0128 - BANCO CARONI", "0134 - BANESCO", "0137 - BANCO SOFITASA", "0138 - BANCO PLAZA", "0151 - BFC BANCO FONDO COMÚN", "0156 - 100% BANCO", "0157 - BANCO DEL SUR", "0163 - BANCO DEL TESORO", "0166 - BANCO AGRÍCOLA DE VENEZUELA", "0168 - BANCRECER", "0169 - MI BANCO", "0171 - BANCO ACTIVO", "0172 - BANCAMIGA", "0174 - BANPLUS", "0175 - BANCO BICENTENARIO", "0177 - BANFANB", "0191 - BNC BANCO NACIONAL DE CRÉDITO")
    val tiposDoc = listOf("V", "E", "J")
    val operadoras = listOf("0412", "0414", "0424", "0416", "0426")

    LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item {
            AdminCard(title = "DATOS DE PAGO MÓVIL", icon = Icons.Default.AccountBalance, actions = {
                IconButton(onClick = { paymentExpanded = !paymentExpanded }) {
                    Icon(if (paymentExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = ArenaPrimaryContainer)
                }
            }) {
                if (paymentExpanded) {
                    var selectedBanco by remember(state.selectedSede) { mutableStateOf(state.selectedSede?.datosPago?.get("banco") ?: "") }
                    var selectedDocType by remember(state.selectedSede) { 
                        mutableStateOf(state.selectedSede?.datosPago?.get("documento")?.take(1) ?: "V") 
                    }
                    var docNumber by remember(state.selectedSede) { 
                        mutableStateOf(state.selectedSede?.datosPago?.get("documento")?.drop(1) ?: "") 
                    }
                    var selectedOp by remember(state.selectedSede) { 
                        mutableStateOf(state.selectedSede?.datosPago?.get("telefono")?.take(4) ?: "0412") 
                    }
                    var phoneSuffix by remember(state.selectedSede) { 
                        mutableStateOf(state.selectedSede?.datosPago?.get("telefono")?.drop(4) ?: "") 
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ExposedDropdownFieldShared(label = "BANCO RECEPTOR", options = bancos, selectedOption = selectedBanco) { selectedBanco = it }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExposedDropdownFieldShared(modifier = Modifier.width(100.dp), label = "TIPO", options = tiposDoc, selectedOption = selectedDocType) { selectedDocType = it }
                            OutlinedTextField(
                                value = docNumber, 
                                onValueChange = { if (it.all { c -> c.isDigit() }) docNumber = it }, 
                                label = { Text("NÚMERO DOC", fontSize = 9.sp, maxLines = 1) }, 
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 11.sp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExposedDropdownFieldShared(modifier = Modifier.width(100.dp), label = "OP", options = operadoras, selectedOption = selectedOp) { selectedOp = it }
                            OutlinedTextField(
                                value = phoneSuffix, 
                                onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 7) phoneSuffix = it }, 
                                label = { Text("NÚMERO TELÉFONO", fontSize = 9.sp, maxLines = 1) }, 
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 11.sp)
                            )
                        }

                        Button(
                            onClick = { onUpdatePagoMovil(selectedBanco, "$selectedDocType$docNumber", "$selectedOp$phoneSuffix") }, 
                            modifier = Modifier.fillMaxWidth().height(56.dp), 
                            colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ACTUALIZAR DATOS DE PAGO", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                } else {
                    Text("Configura los datos para recibir pagos móviles venezolanos.", fontSize = 12.sp, color = ArenaTextVariant)
                }
            }
        }

        item {
            AdminCard(title = "PRECIO BASE POR HORA", icon = Icons.Default.ShoppingCart) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = if (basePrice == "0.0" || basePrice == "0") "" else basePrice,
                        onValueChange = { 
                            basePrice = it
                            it.toDoubleOrNull()?.let { onUpdatePrice(it) }
                        },
                        placeholder = { Text("0", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ArenaPrimaryContainer)
                    )
                }
            }
        }

        item {
            AdminCard(
                title = "GESTIÓN DE EXTRAS", 
                actions = {
                    IconButton(onClick = { extrasExpanded = !extrasExpanded }) {
                        Icon(
                            imageVector = if (extrasExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = ArenaPrimaryContainer
                        )
                    }
                }
            ) {
                if (extrasExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val extraOptions = listOf("PALA DE CORTESÍA", "PALA")
                        val currentExtras = state.selectedSede?.extras ?: emptyMap()

                        extraOptions.forEach { name ->
                            val isEnabled = currentExtras.containsKey(name)
                            val isCourtesy = name == "PALA DE CORTESÍA"
                            var priceValue by remember(name, currentExtras[name]) { mutableStateOf(currentExtras[name]?.toString() ?: "0.0") }

                            LaunchedEffect(currentExtras[name]) {
                                priceValue = currentExtras[name]?.toString() ?: "0.0"
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = isEnabled,
                                    onCheckedChange = { isChecked ->
                                        val newExtras = currentExtras.toMutableMap()
                                        if (isChecked) {
                                            newExtras[name] = if (isCourtesy) 0.0 else (priceValue.toDoubleOrNull() ?: 0.0)
                                        } else {
                                            newExtras.remove(name)
                                        }
                                        onUpdateExtras(newExtras)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = ArenaPrimaryContainer)
                                )
                                Text(
                                    text = name,
                                    color = if (isEnabled) Color.White else Color.Gray,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                if (!isCourtesy) {
                                    OutlinedTextField(
                                        value = if (priceValue == "0.0" || priceValue == "0") "" else priceValue,
                                        onValueChange = { 
                                            priceValue = it
                                            it.toDoubleOrNull()?.let { val m = currentExtras.toMutableMap(); m[name] = it; onUpdateExtras(m) }
                                        },
                                        placeholder = { Text("0", color = Color.Gray, fontSize = 10.sp) },
                                        enabled = isEnabled,
                                        modifier = Modifier.width(100.dp),
                                        textStyle = LocalTextStyle.current.copy(
                                            fontSize = 12.sp, 
                                            color = if (isEnabled) Color.White else Color.DarkGray
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ArenaPrimaryContainer,
                                            disabledBorderColor = Color.DarkGray
                                        )
                                    )
                                } else {
                                    Text(
                                        "GRATIS", 
                                        color = ArenaSuccess, 
                                        fontSize = 9.sp, 
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "Toca para gestionar servicios adicionales",
                        fontSize = 12.sp,
                        color = ArenaTextVariant,
                        modifier = Modifier.clickable { extrasExpanded = true }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownFieldShared(
    modifier: Modifier = Modifier,
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = ArenaPrimaryContainer,
                unfocusedBorderColor = ArenaStaffBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.White)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ArenaSurfaceElevated)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 12.sp, color = Color.White) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentsTab(state: AdminUiStateShared, onVerifyPayment: (String, Boolean) -> Unit) {
    if (state.pendingPayments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("NO HAY PAGOS PENDIENTES", color = ArenaTextVariant)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.pendingPayments) { reserva ->
                AdminCard(title = "REF: ${reserva.referenciaPago}", icon = Icons.Default.Search) {
                    Column {
                        Text("Usuario: ${reserva.nombreUsuario}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Teléfono: ${reserva.telefonoUsuario}", color = ArenaTextVariant, fontSize = 11.sp, maxLines = 1)
                        Text("Monto: $${reserva.montoTotal}", color = ArenaPrimaryContainer, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        Text("Cancha: ${reserva.canchaid}", fontSize = 11.sp, color = Color.White, maxLines = 1)
                        
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

/**
 * Tarjeta base para el panel de administración.
 */
@Composable
fun AdminCard(
    title: String,
    icon: ImageVector? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    Surface(
        color = ArenaSurfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = ArenaPrimaryContainer, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = ArenaPrimaryContainer,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                actions()
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, color: Color = Color.White) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 9.sp, color = ArenaTextVariant, maxLines = 1)
        Text(value, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun MetricBox(label: String, value: String, color: Color = ArenaPrimaryContainer) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color, maxLines = 1)
        Text(label.uppercase(), fontSize = 7.sp, color = ArenaTextVariant, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun AppNotificationsModal(
    notifications: List<Reserva>,
    isStaff: Boolean = false,
    onDismiss: () -> Unit,
    onNotificationClick: (Reserva) -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
            color = ArenaSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ArenaStaffBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isStaff) "PAGOS POR CONFIRMAR" else "NOTIFICACIONES",
                        color = ArenaPrimaryContainer,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
                }
                HorizontalDivider(color = ArenaStaffBorder, modifier = Modifier.padding(vertical = 8.dp))
                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (isStaff) "No hay pagos pendientes" else "No tienes notificaciones nuevas",
                            color = ArenaTextVariant
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(notifications) { reserva ->
                            val isApproved = reserva.estado == Reserva.STATUS_ACTIVA
                            val isCancelled = reserva.estado == Reserva.STATUS_CANCELADA

                            val itemTitle = if (isStaff) {
                                "Nueva Solicitud: $${reserva.montoTotal}"
                            } else if (isApproved) {
                                "Reserva Aprobada"
                            } else if (isCancelled) {
                                "Reserva Rechazada"
                            } else {
                                "Reserva Pendiente de Validación"
                            }

                            val itemIcon = if (isStaff) {
                                Icons.Default.Info
                            } else if (isApproved) {
                                Icons.Default.CheckCircle
                            } else if (isCancelled) {
                                Icons.Default.Cancel
                            } else {
                                Icons.Default.Info
                            }

                            val itemColor = if (isStaff) {
                                ArenaWarning
                            } else if (isApproved) {
                                ArenaSuccess
                            } else if (isCancelled) {
                                ArenaWarning
                            } else {
                                ArenaPrimaryContainer
                            }

                            val subtitle = if (isStaff) {
                                "Ref: ${reserva.referenciaPago} - Usuario: ${reserva.nombreUsuario}"
                            } else {
                                "Fecha: ${reserva.fecha} | Hora: ${reserva.horaInicio} - ${reserva.horaFin}"
                            }

                            Surface(
                                color = ArenaSurfaceBase,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().clickable { onNotificationClick(reserva) }
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(itemIcon, null, tint = itemColor, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(itemTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(subtitle, color = ArenaTextVariant, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminNotificationsModal(
    pendingPayments: List<Reserva>,
    onDismiss: () -> Unit,
    onNotificationClick: (Reserva) -> Unit = {}
) {
    AppNotificationsModal(
        notifications = pendingPayments,
        isStaff = true,
        onDismiss = onDismiss,
        onNotificationClick = onNotificationClick
    )
}

@Composable
fun InstructorsTab(
    state: AdminUiStateShared,
    onSave: (Instructor) -> Unit,
    onRemove: (String) -> Unit
) {
    var selectedInstructor by remember { mutableStateOf<Instructor?>(null) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    
    if (showScheduleDialog && selectedInstructor != null) {
        SelectorDeHorario(
            instructor = selectedInstructor!!,
            canchas = state.canchas,
            onDismiss = { showScheduleDialog = false },
            onSave = { updated -> 
                onSave(updated)
                showScheduleDialog = false
            }
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Button(
                onClick = { 
                    selectedInstructor = Instructor(nombre = "Nuevo Instructor")
                    showScheduleDialog = true 
                }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(8.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer)
            ) {
                Icon(Icons.Default.Add, null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AÑADIR INSTRUCTOR", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        
        items(state.instructores) { instructor ->
            val assignedCourt = state.canchas.find { it.id == instructor.canchaid }?.nombre ?: "Sin cancha"
            AdminCard(title = instructor.nombre, icon = Icons.Default.Person, actions = {
                IconButton(onClick = { 
                    selectedInstructor = instructor
                    showScheduleDialog = true
                }) { Icon(Icons.Default.Edit, null, tint = ArenaPrimaryContainer) }
                IconButton(onClick = { onRemove(instructor.id) }) { Icon(Icons.Default.Delete, null, tint = ArenaWarning) }
            }) {
                Column {
                    Text("Cancha asignada: $assignedCourt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ArenaPrimaryContainer)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Horarios: ${instructor.horario.joinToString(", ")}", fontSize = 12.sp, color = ArenaTextVariant)
                }
            }
        }
    }
}

@Composable
fun SelectorDeHorario(
    instructor: Instructor,
    canchas: List<Cancha>,
    onDismiss: () -> Unit,
    onSave: (Instructor) -> Unit
) {
    var nombre by remember { mutableStateOf(instructor.nombre) }
    var maxAlumnos by remember { mutableStateOf(instructor.maxAlumnos.toString()) }
    var selectedCanchaId by remember { mutableStateOf(instructor.canchaid) }
    val masterSlots = listOf("06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00", "00:00", "01:00")
    
    var hourAssignments by remember { 
        mutableStateOf(instructor.horario.associate { 
            val parts = it.split("_")
            if (parts.size == 2) parts[1] to parts[0] else it to "V"
        })
    }
    
    var isReserveMode by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = ArenaSurfaceBase) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                    Text("GESTIÓN DE HORARIOS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = ArenaPrimaryContainer)
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = nombre, 
                    onValueChange = { nombre = it }, 
                    label = { Text("NOMBRE DEL INSTRUCTOR") }, 
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArenaPrimaryContainer, 
                        focusedTextColor = Color.White, 
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = ArenaStaffBorder
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = maxAlumnos, 
                    onValueChange = { if (it.all { c -> c.isDigit() }) maxAlumnos = it }, 
                    label = { Text("MÁXIMO DE ALUMNOS POR CLASE") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArenaPrimaryContainer, 
                        focusedTextColor = Color.White, 
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = ArenaStaffBorder
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                val canchaOptions = canchas.map { it.nombre }
                val selectedCanchaName = canchas.find { it.id == selectedCanchaId }?.nombre ?: "Seleccionar Cancha"
                
                ExposedDropdownFieldShared(
                    label = "CANCHA DE CLASES",
                    options = canchaOptions,
                    selectedOption = selectedCanchaName,
                    onOptionSelected = { name ->
                        selectedCanchaId = canchas.find { it.nombre == name }?.id ?: ""
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    color = ArenaSurfaceElevated, 
                    shape = RoundedCornerShape(12.dp), 
                    border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp), 
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isReserveMode) "MODO: RESERVA (AMARILLO)" else "MODO: DISPONIBLE (VERDE)", 
                                color = if (isReserveMode) Color(0xFFFFD700) else ArenaSuccess, 
                                fontWeight = FontWeight.Black, 
                                fontSize = 12.sp
                            )
                            Text(text = "Toca las celdas para asignar el color actual.", fontSize = 10.sp, color = ArenaTextVariant)
                        }
                        Switch(
                            checked = isReserveMode, 
                            onCheckedChange = { isReserveMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFD700), 
                                uncheckedThumbColor = ArenaSuccess, 
                                checkedTrackColor = Color(0xFFFFD700).copy(alpha = 0.5f), 
                                uncheckedTrackColor = ArenaSuccess.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("SELECCIONAR TURNOS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ArenaTextVariant)
                Spacer(modifier = Modifier.height(12.dp))

                val chunkedSlots = masterSlots.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    chunkedSlots.forEach { rowSlots ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowSlots.forEach { hour ->
                                val mode = hourAssignments[hour]
                                val isSelected = mode != null
                                
                                val cellColor = when (mode) {
                                    "V" -> ArenaSuccess.copy(alpha = 0.8f)
                                    "A" -> Color(0xFFFFD700).copy(alpha = 0.8f)
                                    else -> ArenaSurfaceElevated.copy(alpha = 0.3f)
                                }
                                
                                val borderColor = when (mode) {
                                    "V" -> ArenaSuccess
                                    "A" -> Color(0xFFFFD700)
                                    else -> ArenaStaffBorder.copy(alpha = 0.5f)
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .clickable {
                                            val currentMode = hourAssignments[hour]
                                            val selectedMode = if (isReserveMode) "A" else "V"
                                            
                                            hourAssignments = if (currentMode == selectedMode) {
                                                hourAssignments - hour
                                            } else {
                                                hourAssignments + (hour to selectedMode)
                                            }
                                        },
                                    color = cellColor,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, borderColor)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            hour, 
                                            color = if (isSelected) Color.Black else Color.White, 
                                            fontWeight = FontWeight.Black, 
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                            if (rowSlots.size < 3) {
                                repeat(3 - rowSlots.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                val isFormValid = nombre.isNotEmpty() && selectedCanchaId.isNotEmpty()
                
                Button(
                    onClick = { 
                        val formattedHorario = hourAssignments.map { "${it.value}_${it.key}" }
                        onSave(instructor.copy(
                            nombre = nombre, 
                            horario = formattedHorario, 
                            canchaid = selectedCanchaId,
                            maxAlumnos = maxAlumnos.toIntOrNull() ?: 5
                        ))
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = isFormValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArenaPrimaryContainer,
                        contentColor = Color.Black,
                        disabledContainerColor = ArenaPrimaryContainer.copy(alpha = 0.2f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (selectedCanchaId.isEmpty()) "SELECCIONA UNA CANCHA" else "GUARDAR CONFIGURACIÓN", 
                        fontWeight = FontWeight.Black
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

data class ActiveMatchShared(
    val canchaId: String,
    val canchaName: String,
    val horaInicio: String,
    val horaFin: String
)

@Composable
fun ScheduleTab(
    state: AdminUiStateShared,
    onManualBookings: (String, List<String>) -> Unit,
    onCancelReservation: (String) -> Unit,
    onCourtSelected: (String) -> Unit = {}
) {
    var selectedCanchaId by remember { mutableStateOf("") }

    LaunchedEffect(state.canchas) {
        if (selectedCanchaId.isEmpty() && state.canchas.isNotEmpty()) {
            selectedCanchaId = state.canchas.first().id
            onCourtSelected(selectedCanchaId)
        }
    }

    val currentCanchaId = if (selectedCanchaId.isNotEmpty()) selectedCanchaId else (state.canchas.firstOrNull()?.id ?: "")
    var selectedSlots by remember { mutableStateOf(setOf<String>()) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val masterSlots = listOf("06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00", "00:00", "01:00")
    
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = ArenaSurfaceElevated,
            titleContentColor = Color.White,
            textContentColor = ArenaTextVariant,
            title = { Text("BLOQUEO DE HORARIO", fontWeight = FontWeight.Black) },
            text = { Text("¿Deseas ocupar los ${selectedSlots.size} turnos seleccionados? Se marcarán como OCUPADO para todos los usuarios.") },
            confirmButton = {
                TextButton(onClick = {
                    onManualBookings(currentCanchaId, selectedSlots.toList())
                    selectedSlots = emptySet()
                    showConfirmDialog = false
                }) {
                    Text("OCUPAR TURNOS", color = ArenaPrimaryContainer, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Selector de Cancha
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.canchas) { cancha ->
                val isSelected = cancha.id == currentCanchaId
                Surface(
                    modifier = Modifier.clickable { 
                        selectedCanchaId = cancha.id 
                        selectedSlots = emptySet()
                        onCourtSelected(cancha.id)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) ArenaPrimaryContainer else ArenaSurfaceElevated,
                    border = if (isSelected) null else BorderStroke(1.dp, ArenaStaffBorder)
                ) {
                    Text(
                        cancha.nombre,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        if (currentCanchaId.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Selecciona una cancha para ver el horario", color = ArenaTextVariant)
            }
        } else {
            val chunkedSlots = masterSlots.chunked(3)
            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    chunkedSlots.forEach { rowSlots ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowSlots.forEach { time ->
                                val status = BookingLogicShared.getEstadoHorario(
                                    time = time,
                                    currentTimeScale = state.currentTimeScale,
                                    reservas = state.dailyReservations,
                                    instructores = state.instructores,
                                    canchaId = currentCanchaId
                                )

                                val currentReservation = state.dailyReservations.find { 
                                    it.canchaid == currentCanchaId && it.horaInicio == time && it.estado != Reserva.STATUS_CANCELADA 
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    ScheduleGridItem(
                                        time = time,
                                        status = status,
                                        isSelected = selectedSlots.contains(time),
                                        reservation = currentReservation,
                                        onToggle = {
                                            selectedSlots = if (selectedSlots.contains(time)) {
                                                selectedSlots - time
                                            } else {
                                                selectedSlots + time
                                            }
                                        },
                                        onCancel = { resId -> onCancelReservation(resId) }
                                    )
                                }
                            }
                            if (rowSlots.size < 3) {
                                repeat(3 - rowSlots.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }

                Button(
                    onClick = { showConfirmDialog = true },
                    enabled = selectedSlots.isNotEmpty(),
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArenaPrimaryContainer, 
                        contentColor = Color.Black,
                        disabledContainerColor = ArenaSurfaceElevated,
                        disabledContentColor = Color.Gray
                    )
                ) {
                    Text(
                        if (selectedSlots.isEmpty()) "SELECCIONA TURNOS" else "RESERVAR AHORA (${selectedSlots.size})", 
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleGridItem(
    time: String,
    status: SlotStatus,
    isSelected: Boolean,
    reservation: Reserva?,
    onToggle: () -> Unit,
    onCancel: (String) -> Unit
) {
    val isOccupied = status == SlotStatus.OCUPADO
    val isPast = status == SlotStatus.PASADO
    val isClass = status == SlotStatus.OCUPADO_INSTRUCTOR
    val isClickable = !isOccupied && !isPast && !isClass
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable(enabled = isClickable) { onToggle() },
        color = when {
            isSelected -> ArenaPrimaryContainer
            isOccupied -> ArenaWarning // ROJO sólido para turnos ocupados
            isPast -> Color.Gray.copy(alpha = 0.35f) // Gris para turnos pasados
            isClass -> Color(0xFFFFD700).copy(alpha = 0.6f) // Dorado para Clases
            else -> ArenaSurfaceElevated.copy(alpha = 0.5f)
        },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = when {
                isSelected -> Color.White
                isOccupied -> ArenaWarning
                isPast -> Color.Gray.copy(alpha = 0.5f)
                isClass -> Color(0xFFFFD700)
                else -> ArenaStaffBorder.copy(alpha = 0.3f)
            }
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    formatTo12hShared(time),
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
                
                when (status) {
                    SlotStatus.OCUPADO -> {
                        val textLabel = if (reservation?.usuarioid == "MANUAL_ADMIN") {
                            "OCUPADO"
                        } else {
                            reservation?.nombreUsuario?.split(" ")?.firstOrNull()?.ifEmpty { "OCUPADO" } ?: "OCUPADO"
                        }
                        Text(
                            textLabel.uppercase(),
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                    SlotStatus.OCUPADO_INSTRUCTOR -> {
                        Text(
                            "CLASE",
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                    SlotStatus.PASADO -> {
                        Text(
                            "PASADO",
                            color = if (isSelected) Color.Black else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                    else -> {
                        Text(
                            if (isSelected) "LISTO" else "LIBRE",
                            color = if (isSelected) Color.Black else ArenaSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (reservation != null && isOccupied) {
                IconButton(
                    onClick = { onCancel(reservation.id) },
                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp).padding(4.dp)
                ) {
                    Icon(Icons.Default.Close, "Liberar", tint = if (isSelected) Color.Black else Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}



data class AdminUiStateShared(
    val selectedSede: Sede? = null,
    val canchas: List<Cancha> = emptyList(),
    val instructores: List<Instructor> = emptyList(),
    val pendingPayments: List<Reserva> = emptyList(),
    val activeMatches: List<ActiveMatchShared> = emptyList(),
    val notifications: List<Reserva> = emptyList(),
    val isLoading: Boolean = false,
    val isImageUploading: Boolean = false,
    val message: String? = null,
    val dailyReservations: List<Reserva> = emptyList(),
    val currentTimeScale: Int = 0
)
