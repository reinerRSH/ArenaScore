package com.example.arena.ui.court

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arena.Model.Di.Domain.Sede
import com.example.arena.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtSelectionScreen(
    sedeId: String,
    viewModel: CourtViewModel,
    onNavigateBack: () -> Unit = {},
    onCourtSelected: (Court) -> Unit = {}
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
            CourtSelectionContent(
                state = state.state,
                onNavigateBack = onNavigateBack,
                onCourtSelected = onCourtSelected
            )
        }
        is CourtUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtSelectionContent(
    state: CourtSelectionState,
    onNavigateBack: () -> Unit,
    onCourtSelected: (Court) -> Unit
) {
    Scaffold(
        topBar = {
            CourtSelectionTopBar(onNavigateBack)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header Section
            HeaderSection(
                sedeName = state.selectedSede?.nombreSede ?: "",
                onNavigateBack = onNavigateBack
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Grid Section
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.canchas) { cancha ->
                    // Convertimos el modelo de Dominio Cancha al modelo UI Court
                    val court = Court(
                        id = cancha.id,
                        name = cancha.nombre,
                        type = cancha.tipo,
                        isAvailable = cancha.estado == "ACTIVA",
                        status = if (cancha.estado == "ACTIVA") CourtStatus.AVAILABLE else CourtStatus.RESERVED,
                        idTechnical = "ID: ${cancha.id}",
                        duration = "90 MIN",
                        imageUrl = "" // Se podría añadir a Firestore también
                    )
                    CourtCard(
                        court = court,
                        onClick = { onCourtSelected(court) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(sedeName: String, onNavigateBack: () -> Unit) {
    Column {
        TextButton(
            onClick = onNavigateBack,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VOLVER A SEDES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = sedeName.ifEmpty { "Cargando..." },
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Schedule Info
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HOY // 16:00 - 17:30",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Legend
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(MaterialTheme.colorScheme.secondary, "DISPONIBLE")
                VerticalDivider(modifier = Modifier.height(16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                LegendItem(MaterialTheme.colorScheme.outline, "RESERVADO", opacity = 0.5f)
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, opacity: Float = 1f) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.alpha(opacity)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtSelectionTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VALOR ARENA",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
            }
        },
        actions = {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile", modifier = Modifier.size(28.dp))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Preview(showBackground = true)
@Composable
fun CourtSelectionPreview() {
    com.example.arena.View.ui.theme.EliteAthleteOSTheme {
        CourtSelectionContent(
            state = CourtSelectionState(
                selectedSede = Sede(nombreSede = "Sede de Prueba"),
                canchas = listOf()
            ),
            onNavigateBack = {},
            onCourtSelected = {}
        )
    }
}
