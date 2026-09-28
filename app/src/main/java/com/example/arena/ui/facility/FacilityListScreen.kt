package com.example.arena.ui.facility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import com.example.arena.View.ui.theme.EliteAthleteOSTheme
import com.example.arena.R
import com.example.arena.domain.Reserva
import com.example.arena.ui.booking.BookingScheduleViewModel
import com.example.arena.domain.Sede
import com.example.arena.ui.screens.FacilityListContentShared

@Preview(showBackground = true, name = "Facility List Preview")
@Composable
fun FacilityListPreview() {
    val mockFacilities = listOf(
        Sede(id = "s1", nombreSede = "Elite Padel Arena", ubicacion = "Caracas", totalCanchas = 5, courtsAvailable = 3),
        Sede(id = "s2", nombreSede = "Padel Center", ubicacion = "Barquisimeto", totalCanchas = 4, courtsAvailable = 1)
    )

    EliteAthleteOSTheme {
        FacilityListContentShared(
            facilities = mockFacilities,
            isLoading = false,
            errorMessage = null,
            selectedFilter = "filter_all",
            searchQuery = "",
            onFilterSelected = {},
            onSearchQueryChanged = {},
            onFacilitySelected = {},
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@Composable
fun FacilityListScreen(
    sport: String,
    role: String,
    authorizedSedes: List<String>,
    notifications: List<Reserva> = emptyList(),
    viewModel: FacilityViewModel,
    bookingViewModel: BookingScheduleViewModel? = null,
    onNavigateBack: () -> Unit = {},
    onFacilitySelected: (Sede) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    LaunchedEffect(sport, role, authorizedSedes) {
        viewModel.loadFacilities(sport, role, authorizedSedes)
    }

    LaunchedEffect(Unit) {
        bookingViewModel?.reset()
    }

    val facilities = if (uiState is FacilityUiState.Success) (uiState as FacilityUiState.Success).facilities else emptyList()

    FacilityListContentShared(
        facilities = facilities,
        isLoading = uiState is FacilityUiState.Loading,
        errorMessage = (uiState as? FacilityUiState.Error)?.message,
        notifications = notifications,
        selectedFilter = selectedFilter,
        searchQuery = searchQuery,
        onFilterSelected = { viewModel.onFilterSelected(it) },
        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
        onFacilitySelected = onFacilitySelected,
        onNavigateBack = onNavigateBack,
        onNotificationClick = onNotificationClick,
        onHistoryClick = onHistoryClick,
        onProfileClick = onProfileClick,
        onRetry = { viewModel.loadFacilities(sport, role, authorizedSedes) },
        facilityImageProvider = { sede, modifier ->
            androidx.compose.runtime.key(sede.imageUrl) {
                AsyncImage(
                    model = if (sede.imageUrl.isNotEmpty()) sede.imageUrl else R.drawable.cancha,
                    placeholder = painterResource(R.drawable.cancha),
                    error = painterResource(R.drawable.cancha),
                    contentDescription = null,
                    modifier = modifier,
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    )
}
