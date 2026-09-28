package com.example.arena.ui.booking

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.arena.R
import com.example.arena.View.ui.theme.EliteAthleteOSTheme
import com.example.arena.ui.screens.BookingTypeContentShared

@Composable
fun BookingTypeSelectionScreen(
    sedeId: String,
    canchaId: String,
    canchaName: String = "",
    viewModel: BookingTypeViewModel,
    notifications: List<com.example.arena.domain.Reserva> = emptyList(),
    onNavigateBack: () -> Unit,
    onTypeSelected: (String) -> Unit,
    onNotificationClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    BookingTypeContentShared(
        clasesPainter = painterResource(R.drawable.clasescard),
        // JUEGO usaba una URL antes, usaremos un recurso local o similar si lo tienes
        // Por ahora cargamos el de padel como ejemplo o el que desees
        juegoPainter = painterResource(R.drawable.padel),
        notifications = notifications,
        onNavigateBack = onNavigateBack,
        onTypeSelected = { type -> viewModel.onTypeSelected(type, onTypeSelected) },
        onNotificationClick = onNotificationClick,
        onHistoryClick = onHistoryClick
    )
}

@Preview(showBackground = true)
@Composable
fun BookingTypeSelectionPreview() {
    EliteAthleteOSTheme {
        BookingTypeContentShared(
            onNavigateBack = {},
            onTypeSelected = {}
        )
    }
}
