package com.example.arena.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arena.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingTypeContentShared(
    onNavigateBack: () -> Unit,
    onTypeSelected: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("RESERVAR", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = ArenaPrimaryContainer)) } },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ArenaPrimaryContainer) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = ArenaSurfaceBase
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OptionCardShared(
                title = "CLASES",
                subtitle = "Entrenamiento personalizado y grupal",
                icon = Icons.Default.Groups,
                modifier = Modifier.weight(1f),
                onClick = { onTypeSelected("CLASES") }
            )

            OptionCardShared(
                title = "JUEGO",
                subtitle = "Reserva tu pista para partidos amistosos",
                icon = Icons.Default.SportsScore,
                modifier = Modifier.weight(1f),
                onClick = { onTypeSelected("JUEGO") }
            )
        }
    }
}

@Composable
fun OptionCardShared(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = ArenaSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, Brush.linearGradient(colors = listOf(ArenaPrimaryContainer.copy(alpha = 0.5f), Color.Transparent)))
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))) {
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = ArenaPrimaryContainer, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = Color.White))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.7f)), modifier = Modifier.weight(1f))
                    Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(8.dp), color = ArenaSurfaceBase.copy(alpha = 0.2f), border = androidx.compose.foundation.BorderStroke(1.dp, ArenaPrimaryContainer.copy(alpha = 0.5f))) {
                        Box(contentAlignment = Alignment.Center) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = ArenaPrimaryContainer, modifier = Modifier.size(24.dp)) }
                    }
                }
            }
        }
    }
}
