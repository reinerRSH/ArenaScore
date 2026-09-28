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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arena.domain.Cancha
import com.example.arena.domain.Reserva
import com.example.arena.domain.Sede
import com.example.arena.ui.theme.*
import com.example.arena.ui.components.ScanlineEffectShared
import com.example.arena.ui.components.ArenaTopBarShared

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtSelectionContentShared(
    sede: Sede?,
    canchas: List<Cancha>,
    notifications: List<Reserva> = emptyList(),
    onNavigateBack: () -> Unit,
    onCourtSelected: (Cancha) -> Unit,
    onNotificationClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    courtImageProvider: @Composable (Cancha, Modifier) -> Unit = { _, m -> Box(m.background(Color.Gray)) }
) {
    Scaffold(
        topBar = {
            ArenaTopBarShared(
                onNavigateBack = onNavigateBack,
                notificationCount = notifications.size,
                onNotificationClick = onNotificationClick,
                onHistoryClick = onHistoryClick
            )
        },
        containerColor = ArenaSurfaceBase
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            ScanlineEffectShared()
            
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
                                CourtCardShared(
                                    cancha = cancha, 
                                    onClick = { onCourtSelected(cancha) },
                                    imageContent = { courtImageProvider(cancha, it) }
                                )
                            }
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
                Text("VOLVER A SEDES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ArenaTextVariant, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = sedeName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun CourtCardShared(cancha: Cancha, onClick: () -> Unit, imageContent: @Composable (Modifier) -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Imagen sola arriba
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                imageContent(Modifier.fillMaxSize())
            }
            
            // Contenido fuera de la imagen abajo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cancha.nombre.uppercase(),
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, null, tint = ArenaPrimaryContainer, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = cancha.tipo,
                                color = ArenaPrimaryContainer,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                maxLines = 1
                            )
                        }
                    }
                    
                    // Precio o Estado si fuera necesario, o el patrocinador
                    if (cancha.patrocinador.isNotEmpty()) {
                        Text(
                            text = cancha.patrocinador,
                            color = ArenaTextVariant,
                            fontSize = 10.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("RESERVAR AHORA", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}
