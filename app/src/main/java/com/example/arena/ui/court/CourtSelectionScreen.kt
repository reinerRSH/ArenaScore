package com.example.arena.ui.court

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.arena.R
import com.example.arena.domain.Cancha
import com.example.arena.domain.Sede
import com.example.arena.View.ui.theme.EliteAthleteOSTheme
import com.example.arena.ui.screens.CourtSelectionContentShared

@Preview(showBackground = true, name = "Court Selection Preview")
@Composable
fun CourtSelectionPreview() {
    val mockSede = Sede(
        id = "s1",
        nombreSede = "Elite Padel Arena",
        ubicacion = "Caracas"
    )
    val mockCanchas = listOf(
        Cancha(id = "c1", nombre = "Cancha 1", tipo = "PANORÁMICA", estado = "ACTIVA"),
        Cancha(id = "c2", nombre = "Cancha 2", tipo = "MURO", estado = "ACTIVA")
    )

    EliteAthleteOSTheme {
        CourtSelectionContentShared(
            sede = mockSede,
            canchas = mockCanchas,
            onNavigateBack = {},
            onCourtSelected = {}
        )
    }
}

@Composable
fun CourtSelectionScreen(
    sedeId: String,
    viewModel: CourtViewModel,
    notifications: List<com.example.arena.domain.Reserva> = emptyList(),
    onNavigateBack: () -> Unit = {},
    onCourtSelected: (com.example.arena.domain.Cancha) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sedeId) {
        viewModel.loadCourtSelection(sedeId)
    }

    when (val state = uiState) {
        is CourtUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is CourtUiState.Success -> {
            CourtSelectionContentShared(
                sede = state.state.selectedSede,
                canchas = state.state.canchas,
                notifications = notifications,
                onNavigateBack = onNavigateBack,
                onCourtSelected = onCourtSelected,
                onNotificationClick = onNotificationClick,
                onHistoryClick = onHistoryClick,
                courtImageProvider = { cancha, modifier ->
                    androidx.compose.runtime.key(cancha.imageUrl) {
                        AsyncImage(
                            model = if (cancha.imageUrl.isNotEmpty()) cancha.imageUrl else R.drawable.cancha,
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
        is CourtUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
