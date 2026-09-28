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
import com.example.arena.ui.facility.FacilityUiState
import com.example.arena.ui.home.HomeViewModel
import com.example.arena.ui.home.HomeNavigationState

@Composable
fun FacilityListScreen(
    navController: NavController,
    sport: String,
    viewModel: FacilityViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val homeState by homeViewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val notifications by homeViewModel.notifications.collectAsState()
    
    val context = LocalContext.current
    var selectedReceipt by remember { mutableStateOf<Reserva?>(null) }

    LaunchedEffect(sport, homeState) {
        if (homeState is HomeNavigationState.Success) {
            val state = homeState as HomeNavigationState.Success
            viewModel.loadFacilities(sport, state.role, state.authorizedSedes)
        }
    }

    if (selectedReceipt != null) {
        ReceiptModalShared(
            reserva = selectedReceipt,
            onDismiss = { selectedReceipt = null },
            onDownload = { Toast.makeText(context, "Descargando comprobante...", Toast.LENGTH_SHORT).show() },
            onShare = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Comprobante Arena - Cancha: ${selectedReceipt!!.canchaid}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Compartir Comprobante"))
            }
        )
    }

    FacilityListContentShared(
        facilities = if (uiState is FacilityUiState.Success) (uiState as FacilityUiState.Success).facilities else emptyList(),
        isLoading = uiState is FacilityUiState.Loading,
        errorMessage = if (uiState is FacilityUiState.Error) (uiState as FacilityUiState.Error).message else null,
        notifications = notifications,
        selectedFilter = selectedFilter,
        searchQuery = searchQuery,
        onFilterSelected = { viewModel.onFilterSelected(it) },
        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
        onFacilitySelected = { sede ->
            navController.navigate(Screen.SelectCourt(sedeId = sede.id))
        },
        onNavigateBack = { navController.popBackStack() },
        onProfileClick = { navController.navigate(Screen.Profile) },
        onRetry = { 
             if (homeState is HomeNavigationState.Success) {
                val state = homeState as HomeNavigationState.Success
                viewModel.loadFacilities(sport, state.role, state.authorizedSedes)
            }
        },
        onNotificationClick = {
            homeViewModel.markNotificationsAsRead()
            navController.navigate(Screen.MyReservations)
        }
    )
}
