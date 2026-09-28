package com.example.arena.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.arena.domain.Reserva
import com.example.arena.navigation.Screen
import com.example.arena.ui.facility.FacilityViewModel
import com.example.arena.ui.home.HomeViewModel

@Composable
fun FacilityListScreen(
    navController: NavController,
    sport: String,
    viewModel: FacilityViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val notifications by homeViewModel.notifications.collectAsState()
    val context = LocalContext.current
    var selectedReceipt by remember { mutableStateOf<Reserva?>(null) }

    LaunchedEffect(sport) {
        viewModel.loadFacilities(sport)
    }

    if (selectedReceipt != null) {
        ReceiptModalShared(
            reserva = selectedReceipt,
            onDismiss = { selectedReceipt = null },
            onDownload = { Toast.makeText(context, "Descargando...", Toast.LENGTH_SHORT).show() },
            onShare = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Reserva Arena Confirmada: ${selectedReceipt!!.canchaid}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Compartir"))
            }
        )
    }

    FacilityListContentShared(
        facilities = uiState.facilities,
        isLoading = uiState.isLoading,
        errorMessage = uiState.message,
        notifications = notifications,
        selectedFilter = uiState.selectedFilter,
        searchQuery = uiState.searchQuery,
        onFilterSelected = { viewModel.updateFilter(it) },
        onSearchQueryChanged = { viewModel.updateSearch(it) },
        onFacilitySelected = { sede ->
            navController.navigate(Screen.CourtSelection(sedeId = sede.id))
        },
        onNavigateBack = { navController.popBackStack() },
        onRetry = { viewModel.loadFacilities(sport) },
        onNotificationClick = { /* Mostrar modal de notificaciones si es necesario, por ahora abre el último */
            if (notifications.isNotEmpty()) selectedReceipt = notifications.first()
        }
    )
}
