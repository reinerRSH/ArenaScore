package com.example.arena.ui.admin

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.arena.R
import com.example.arena.domain.Cancha
import com.example.arena.domain.Reserva
import com.example.arena.domain.Sede
import com.example.arena.View.ui.theme.EliteAthleteOSTheme
import com.example.arena.navigation.Screen
import com.example.arena.ui.screens.AdminDashboardContent
import com.example.arena.ui.screens.AdminUiStateShared
import com.example.arena.ui.screens.ReceiptModalShared

@Composable
fun AdminDashboardScreen(
    sedeId: String,
    initialTab: Int = 0,
    viewModel: AdminDashboardViewModel,
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(sedeId) {
        viewModel.loadAdminData(sedeId)
    }

    LaunchedEffect(uiState.isLogout) {
        if (uiState.isLogout) {
            navController.navigate(Screen.Login) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val sharedUiState = AdminUiStateShared(
        selectedSede = uiState.selectedSede,
        canchas = uiState.canchas,
        instructores = uiState.instructores,
        pendingPayments = uiState.pendingPayments,
        activeMatches = uiState.activeMatches,
        notifications = uiState.notifications,
        isLoading = uiState.isLoading,
        isImageUploading = uiState.isImageUploading,
        message = uiState.message
    )

    var currentCanchaIdForImage by remember { mutableStateOf<String?>(null) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null && currentCanchaIdForImage != null) {
                viewModel.uploadCourtImage(currentCanchaIdForImage!!, uri)
            }
        }
    )

    if (uiState.selectedReceipt != null) {
        val receipt = uiState.selectedReceipt!!
        ReceiptModalShared(
            reserva = receipt,
            onDismiss = { viewModel.dismissReceipt() },
            onDownload = {
                Toast.makeText(context, "Comprobante guardado en descargas", Toast.LENGTH_SHORT).show()
            },
            onShare = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, """
                        COMPROBANTE ARENA
                        Cancha: ${receipt.canchaid}
                        Fecha: ${receipt.fecha}
                        Horario: ${receipt.horaInicio} - ${receipt.horaFin}
                        Referencia: ${receipt.referenciaPago}
                        Total: $${receipt.montoTotal}
                    """.trimIndent())
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Compartir Comprobante"))
            }
        )
    }

    AdminDashboardContent(
        uiState = sharedUiState,
        initialTab = initialTab,
        onNavigateBack = onNavigateBack,
        onToggleCancha = { id, status -> viewModel.toggleCanchaStatus(id, status) },
        onUpdateCancha = { id, name, img, sponsor, type -> viewModel.updateCancha(id, name, img, sponsor, type) },
        onPickImage = { id ->
            currentCanchaIdForImage = id
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onUpdatePrice = { viewModel.updatePrice(it) },
        onUpdateExtras = { viewModel.updateExtras(it) },
        onUpdateServicios = { viewModel.updateServicios(it) },
        onUpdatePagoMovil = { b, d, t -> viewModel.updatePagoMovil(b, d, t) },
        onSaveInstructor = { viewModel.saveInstructor(it) },
        onRemoveInstructor = { viewModel.removeInstructor(it) },
        onVerifyPayment = { id, approve -> viewModel.verifyPayment(id, approve) },
        onManualBookings = { canchaId, horas -> viewModel.createManualBookings(canchaId, horas) },
        onCancelReservation = { id -> viewModel.cancelReservation(id) },
        onCourtSelectedForSchedule = { id -> viewModel.updateSelectedCourtForSchedule(id) },
        onShowReceipt = { viewModel.showReceipt(it) },
        onLogout = { viewModel.logout() },
        courtImageProvider = { url, modifier ->
            androidx.compose.runtime.key(url) {
                AsyncImage(
                    model = if (url.isNotEmpty()) url else R.drawable.cancha,
                    placeholder = androidx.compose.ui.res.painterResource(R.drawable.cancha),
                    error = androidx.compose.ui.res.painterResource(R.drawable.cancha),
                    contentDescription = null,
                    modifier = modifier,
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    )
}

@Preview(showBackground = true, name = "Admin Dashboard Content Preview")
@Composable
fun AdminDashboardContentPreview() {
    val mockState = com.example.arena.ui.screens.AdminUiStateShared(
        selectedSede = Sede(
            id = "sede_1",
            nombreSede = "Elite Padel Arena",
            ubicacion = "Caracas, Venezuela",
            suscripcionStatus = "ACTIVA",
            precioBase = 45.0
        ),
        canchas = listOf(
            Cancha(id = "c1", nombre = "Cancha 1", estado = "ACTIVA", tipo = "PANORÁMICA"),
            Cancha(id = "c2", nombre = "Cancha 2", estado = "INACTIVA", tipo = "MURO")
        ),
        pendingPayments = listOf(
            Reserva(id = "r1", referenciaPago = "982341", montoTotal = 52.0, nombreUsuario = "Juan Perez", canchaid = "Cancha 1")
        )
    )

    EliteAthleteOSTheme {
        AdminDashboardContent(
            uiState = mockState,
            onNavigateBack = {},
            onToggleCancha = { _, _ -> },
            onUpdatePrice = {},
            onUpdateExtras = {},
            onVerifyPayment = { _, _ -> }
        )
    }
}
