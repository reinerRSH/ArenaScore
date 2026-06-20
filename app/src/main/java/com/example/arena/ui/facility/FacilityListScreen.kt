package com.example.arena.ui.facility

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.arena.Model.Di.Domain.Sede
import com.example.arena.R

@Composable
fun FacilityListScreen(
    sport: String,
    role: String,
    authorizedSedes: List<String>,
    viewModel: FacilityViewModel,
    onFacilitySelected: (Sede) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    LaunchedEffect(sport, role, authorizedSedes) {
        viewModel.loadFacilities(sport, role, authorizedSedes)
    }

    FacilityListContent(
        uiState = uiState,
        selectedFilter = selectedFilter,
        searchQuery = searchQuery,
        onFilterSelected = { viewModel.onFilterSelected(it) },
        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
        onFacilitySelected = onFacilitySelected,
        onRetry = { viewModel.loadFacilities(sport, role, authorizedSedes) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacilityListContent(
    uiState: FacilityUiState,
    selectedFilter: String,
    searchQuery: String,
    onFilterSelected: (String) -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onFacilitySelected: (Sede) -> Unit = {},
    onRetry: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopArenaBar()
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChanged = onSearchQueryChanged
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filter Chips
            FilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Sección de tarjetas
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    is FacilityUiState.Loading -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is FacilityUiState.Success -> {
                        val filteredFacilities = uiState.facilities.filter { sede ->
                            val matchesFilter = when (selectedFilter) {
                                "filter_indoor" -> sede.tags.any { tag -> tag.contains("dentro", ignoreCase = true) }
                                "filter_outdoor" -> sede.tags.any { tag -> tag.contains("fuera", ignoreCase = true) || tag.contains("exterior", ignoreCase = true) }
                                "filter_top_rated" -> true // Handled by sorting below
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

                        if (filteredFacilities.isEmpty()) {
                            Text(
                                text = stringResource(id = R.string.no_facilities_found),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Center),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(filteredFacilities) { sede ->
                                    FacilityCard(
                                        sede = sede,
                                        onClick = { onFacilitySelected(sede) }
                                    )
                                }
                            }
                        }
                    }
                    is FacilityUiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onRetry,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.retry_button), 
                                    color = MaterialTheme.colorScheme.primary
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
fun TopArenaBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón de Menú Estilizado
        Surface(
            onClick = { /* TODO */ },
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Text(
                text = stringResource(id = R.string.menu_label),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        Text(
            text = stringResource(id = R.string.titulo_app),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )

        // Botón de Perfil Estilizado
        Surface(
            onClick = { /* TODO */ },
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = stringResource(id = R.string.desc_profile),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                stringResource(id = R.string.search_placeholder),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        singleLine = true
    )
}

@Composable
fun FilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filterOptions = listOf(
        "filter_all" to stringResource(id = R.string.filter_all),
        "filter_nearby" to stringResource(id = R.string.filter_nearby),
        "filter_top_rated" to stringResource(id = R.string.filter_top_rated),
        "filter_indoor" to stringResource(id = R.string.filter_indoor),
        "filter_outdoor" to stringResource(id = R.string.filter_outdoor)
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filterOptions) { (id, label) ->
            val isSelected = selectedFilter == id
            SuggestionChip(
                onClick = { onFilterSelected(id) },
                label = { 
                    Text(
                        label, 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ) 
                },
                shape = RoundedCornerShape(16.dp),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    labelColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                ),
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            )
        }
    }
}

@Composable
fun FacilityCard(
    sede: Sede,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick, // Toda la tarjeta es cliqueable
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // 1. Contenedor de la Imagen (Aumentamos la altura para que no se vea cortada)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp) // Aumentado de 110.dp a 160.dp para dar más aire
            ) {
                // Imagen de la sede (o la local si no hay URL)
                AsyncImage(
                    model = if (sede.imageUrl.isNotEmpty()) sede.imageUrl else R.drawable.cancha,
                    contentDescription = sede.nombreSede,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.cancha),
                    error = painterResource(id = R.drawable.cancha)
                )

                // Star Rating Badge (Encima de la imagen)
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = sede.starRating.toString(),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 2. Detalles (Debajo de la imagen, sin solaparse)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = sede.nombreSede.uppercase(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = sede.ubicacion,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge de Disponibilidad
                    val badgeColor = when {
                        sede.courtsAvailable > 1 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    }

                    Surface(
                        color = badgeColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        val availabilityText = if (sede.totalCanchas > 0) {
                            "${sede.courtsAvailable} / ${sede.totalCanchas} " + stringResource(id = R.string.courts_available_format).substringAfter(" ")
                        } else {
                            stringResource(id = R.string.courts_available_format, sede.courtsAvailable)
                        }

                        Text(
                            text = availabilityText,
                            color = badgeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Tags Técnicos (opcional, al lado derecho si existen)
                    if (sede.tags.isNotEmpty()) {
                        Text(
                            text = "#${sede.tags.first().uppercase()}",
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FacilityListPreview() {
    val sampleSedes = listOf(
        Sede(
            id = "1",
            nombreSede = "Padel Galis Arena",
            ubicacion = "6.2km away",
            courtsAvailable = 10,
            starRating = 5.0,
            imageUrl = ""
        ),
        Sede(
            id = "2",
            nombreSede = "The Padel Club",
            ubicacion = "8.5km away",
            courtsAvailable = 4,
            starRating = 4.7,
            imageUrl = ""
        ),
        Sede(
            id = "3",
            nombreSede = "Skyline Padel",
            ubicacion = "12.0km away",
            courtsAvailable = 1,
            starRating = 4.2,
            imageUrl = ""
        )
    )
    
    com.example.arena.View.ui.theme.EliteAthleteOSTheme {
        FacilityListContent(
            uiState = FacilityUiState.Success(sampleSedes),
            selectedFilter = "filter_all",
            searchQuery = ""
        )
    }
}
