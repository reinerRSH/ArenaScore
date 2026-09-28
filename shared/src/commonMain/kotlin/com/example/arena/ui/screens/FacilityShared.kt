package com.example.arena.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arena.domain.Sede
import com.example.arena.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacilityListContentShared(
    facilities: List<Sede>,
    isLoading: Boolean,
    errorMessage: String?,
    selectedFilter: String,
    searchQuery: String,
    onFilterSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onFacilitySelected: (Sede) -> Unit,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("ARENA", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = ArenaPrimaryContainer)) } },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ArenaPrimaryContainer) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = ArenaSurfaceBase
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar sedes, canchas...", color = ArenaTextVariant, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = ArenaTextVariant) },
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ArenaPrimaryContainer, unfocusedContainerColor = ArenaSurfaceElevated.copy(alpha = 0.5f), focusedContainerColor = ArenaSurfaceElevated.copy(alpha = 0.5f))
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val filters = listOf("filter_all" to "Todos", "filter_nearby" to "Cercanos", "filter_top_rated" to "Mejor Valorados")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { (id, label) ->
                    val isSelected = selectedFilter == id
                    SuggestionChip(
                        onClick = { onFilterSelected(id) },
                        label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
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
                        Text(errorMessage, color = ArenaWarning, textAlign = TextAlign.Center)
                        Button(onClick = onRetry) { Text("REINTENTAR") }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(16.dp)) {
                        items(facilities) { sede ->
                            FacilityCardShared(sede = sede, onClick = { onFacilitySelected(sede) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FacilityCardShared(sede: Sede, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(Color.Gray))
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Text(sede.nombreSede.uppercase(), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = ArenaTextVariant, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(sede.ubicacion, color = ArenaTextVariant, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = ArenaSuccess.copy(alpha = 0.1f), border = BorderStroke(1.dp, ArenaSuccess.copy(alpha = 0.4f)), shape = RoundedCornerShape(6.dp)) {
                    Text("${sede.courtsAvailable} / ${sede.totalCanchas} DISP.", color = ArenaSuccess, fontSize = 9.sp, modifier = Modifier.padding(6.dp))
                }
            }
        }
    }
}
