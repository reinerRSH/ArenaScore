package com.example.arena.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arena.domain.Reserva
import com.example.arena.domain.Sede
import com.example.arena.ui.theme.*
import com.example.arena.ui.components.ScanlineEffectShared
import com.example.arena.ui.components.ArenaTopBarShared
import com.example.arena.ui.components.ServiciosRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacilityListContentShared(
    facilities: List<Sede>,
    isLoading: Boolean,
    errorMessage: String?,
    notifications: List<Reserva> = emptyList(),
    selectedFilter: String,
    searchQuery: String,
    onFilterSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onFacilitySelected: (Sede) -> Unit,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onNotificationClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    facilityImageProvider: @Composable (Sede, Modifier) -> Unit = { _, m -> Box(m.background(Color.Gray)) }
) {
    val filteredFacilities = remember(facilities, selectedFilter, searchQuery) {
        facilities.filter { sede ->
            val matchesFilter = when (selectedFilter) {
                "filter_indoor" -> sede.tags.any { tag -> tag.contains("dentro", ignoreCase = true) || tag.contains("interior", ignoreCase = true) }
                "filter_outdoor" -> sede.tags.any { tag -> tag.contains("fuera", ignoreCase = true) || tag.contains("exterior", ignoreCase = true) }
                "filter_top_rated" -> true 
                else -> true
            }
            val matchesSearch = sede.nombreSede.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }.let { list ->
            if (selectedFilter == "filter_top_rated") {
                list.sortedByDescending { it.starRating }
            } else {
                list
            }
        }
    }

    Scaffold(
        topBar = {
            ArenaTopBarShared(
                onNavigateBack = onNavigateBack,
                notificationCount = notifications.size,
                onNotificationClick = onNotificationClick,
                onHistoryClick = onHistoryClick,
                onProfileClick = onProfileClick,
                showBack = false
            )
        },
        containerColor = ArenaSurfaceBase
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            ScanlineEffectShared()
            
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar sedes, canchas...", color = ArenaTextVariant, fontSize = 12.sp, maxLines = 1) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = ArenaTextVariant) },
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ArenaPrimaryContainer, unfocusedContainerColor = ArenaSurfaceElevated.copy(alpha = 0.5f), focusedContainerColor = ArenaSurfaceElevated.copy(alpha = 0.5f))
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val filterOptions = listOf(
                    "filter_all" to "Todos",
                    "filter_nearby" to "Cercanos",
                    "filter_top_rated" to "Mejores",
                    "filter_indoor" to "Interior",
                    "filter_outdoor" to "Exterior"
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterOptions) { (id, label) ->
                        val isSelected = selectedFilter == id
                        SuggestionChip(
                            onClick = { onFilterSelected(id) },
                            label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(16.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = if (isSelected) ArenaPrimaryContainer else Color.Transparent, labelColor = if (isSelected) Color.Black else Color.White)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    color = ArenaSurfaceElevated.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.3f))
                ) {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize()) { CircularProgressIndicator(color = ArenaPrimaryContainer, modifier = Modifier.align(Alignment.Center)) }
                    } else if (errorMessage != null) {
                        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(errorMessage, color = ArenaWarning, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Button(onClick = onRetry) { Text("REINTENTAR") }
                        }
                    } else {
                        if (filteredFacilities.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No se encontraron sedes", color = ArenaTextVariant)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(16.dp)) {
                                items(filteredFacilities) { sede ->
                                    FacilityCardShared(
                                        sede = sede, 
                                        onClick = { onFacilitySelected(sede) },
                                        imageContent = { facilityImageProvider(sede, it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FacilityCardShared(sede: Sede, onClick: () -> Unit, imageContent: @Composable (Modifier) -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Imagen sola arriba (Proporción 16:9 como las canchas)
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                imageContent(Modifier.fillMaxSize())
            }
            
            // Contenido fuera de la imagen abajo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = sede.nombreSede.uppercase(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = ArenaPrimaryContainer, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(sede.ubicacion, color = ArenaTextVariant, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ServiciosRow(servicios = sede.tags)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = ArenaPrimaryContainer.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, ArenaPrimaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${sede.courtsAvailable} / ${sede.totalCanchas} CANCHAS",
                            color = ArenaPrimaryContainer,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1
                        )
                    }
                    
                    // Indicador de "Ver más" o flecha
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = ArenaPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
