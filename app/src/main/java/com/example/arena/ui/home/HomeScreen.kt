package com.example.arena.ui.home

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.arena.R
import com.example.arena.domain.Reserva
import com.example.arena.navigation.Screen
import com.example.arena.ui.MainViewModel
import com.example.arena.ui.screens.HomeArenaContentShared
import com.example.arena.ui.screens.ReceiptModalShared

@Composable
fun HomeArenaScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedReceipt by remember { mutableStateOf<Reserva?>(null) }
    
    // ... (rest of the code)

    LaunchedEffect(uiState) {
        android.util.Log.d("HomeScreen", "Current uiState: $uiState")
        when (uiState) {
            is HomeNavigationState.Success -> {
                val state = uiState as HomeNavigationState.Success
                android.util.Log.d("HomeScreen", "Success role: ${state.role}, authorized count: ${state.authorizedSedes.size}")
                
                if (state.role == "STAFF" && state.authorizedSedes.isEmpty()) {
                    android.util.Log.d("HomeScreen", "Navigating to StaffSetup")
                    navController.navigate(Screen.StaffSetup)
                } else if (state.role == "STAFF" && state.authorizedSedes.size == 1) {
                    android.util.Log.d("HomeScreen", "Navigating to AdminDashboard with sedeId: ${state.authorizedSedes.first()}")
                    navController.navigate(Screen.AdminDashboard(sedeId = state.authorizedSedes.first()))
                } else {
                    android.util.Log.d("HomeScreen", "Navigating to FacilityList")
                    navController.navigate(Screen.FacilityList(sport = state.sport))
                }
            }
            HomeNavigationState.Logout -> {
                navController.navigate(Screen.Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is HomeNavigationState.Error -> {
                val errorState = uiState as HomeNavigationState.Error
                Toast.makeText(context, errorState.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    if (selectedReceipt != null) {
        val receipt = selectedReceipt!!
        ReceiptModalShared(
            reserva = receipt,
            onDismiss = { selectedReceipt = null },
            onDownload = {
                Toast.makeText(context, "Comprobante guardado en galería", Toast.LENGTH_SHORT).show()
            },
            onShare = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, """
                        ARENA SPORTS CENTER - COMPROBANTE
                        Cancha: ${receipt.canchaid}
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

    HomeArenaContentShared(
        isLoading = uiState is HomeNavigationState.Loading,
        crossfitPainter = painterResource(R.drawable.crossfit),
        padelPainter = painterResource(R.drawable.padel),
        crossfitTitle = stringResource(R.string.crossfit_label),
        crossfitSubtitle = stringResource(R.string.crossfit_subtitle),
        onSportSelected = { viewModel.selectSport(it) },
        onShowReceipt = { selectedReceipt = it },
        onLogout = { viewModel.logout() },
        onProfileClick = { navController.navigate(Screen.Profile) },
        courtImageProvider = { url, modifier ->
            AsyncImage(
                model = if (url.isNotEmpty()) url else R.drawable.cancha,
                placeholder = painterResource(R.drawable.cancha),
                error = painterResource(R.drawable.cancha),
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
    )
}
