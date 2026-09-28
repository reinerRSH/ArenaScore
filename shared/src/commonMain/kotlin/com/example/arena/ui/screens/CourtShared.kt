package com.example.arena.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arena.domain.Cancha
import com.example.arena.domain.Sede
import com.example.arena.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtSelectionContentShared(
    sede: Sede?,
    canchas: List<Cancha>,
    onNavigateBack: () -> Unit,
    onCourtSelected: (Cancha) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("ARENA", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = ArenaPrimaryContainer)) } },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ArenaPrimaryContainer) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = ArenaSurfaceBase
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            HeaderSectionShared(sedeName = sede?.nombreSede ?: "Cargando...", onNavigateBack = onNavigateBack)
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                color = ArenaSurfaceElevated.copy(alpha = 0.8f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.2f))
            ) {
                LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 280.dp), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(canchas) { cancha ->
                        if (cancha.estado == "ACTIVA") {
                            CourtCardShared(cancha = cancha, onClick = { onCourtSelected(cancha) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSectionShared(sedeName: String, onNavigateBack: () -> Unit) {
    Surface(color = ArenaSurfaceElevated.copy(alpha = 0.8f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.2f)), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            TextButton(onClick = onNavigateBack, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(16.dp), tint = ArenaTextVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Text("VOLVER A SEDES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ArenaTextVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = sedeName, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun CourtCardShared(cancha: Cancha, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SportsTennis, null, tint = ArenaPrimaryContainer, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(cancha.nombre, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(cancha.tipo, color = ArenaTextVariant, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer, contentColor = Color.Black)) {
                Text("SELECCIONAR", fontWeight = FontWeight.Bold)
            }
        }
    }
}
