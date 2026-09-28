package com.example.arena.ui.gym

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.arena.View.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymClassSelectionScreen(
    sedeId: String,
    sedeName: String = "Centro de Entrenamiento",
    onNavigateBack: () -> Unit,
    onClassSelected: (GymClass) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Clases, 1: Noticias, 2: Perfil
    var selectedCategory by remember { mutableStateOf("TODOS") }
    val categories = listOf("TODOS", "WOD", "FUERZA", "HIIT", "CARDIO", "CALISTENIA")

    val filteredClasses = remember(selectedCategory) {
        if (selectedCategory == "TODOS") {
            mockGymClasses
        } else {
            mockGymClasses.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = ArenaSuccess.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, ArenaSuccess.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ArenaSuccess)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ACTIVO", color = ArenaSuccess, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = ArenaPrimaryContainer)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ArenaSurfaceBase)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ArenaSurfaceElevated,
                contentColor = ArenaPrimaryContainer
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Clases") },
                    label = { Text("Clases", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = ArenaPrimaryContainer,
                        indicatorColor = ArenaPrimaryContainer,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Newspaper, contentDescription = "Noticias") },
                    label = { Text("Noticias", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = ArenaPrimaryContainer,
                        indicatorColor = ArenaPrimaryContainer,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = ArenaPrimaryContainer,
                        indicatorColor = ArenaPrimaryContainer,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        containerColor = ArenaSurfaceBase
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Categorías Filter Row
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { category ->
                                val isSelected = selectedCategory == category
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ArenaPrimaryContainer,
                                        selectedLabelColor = Color.Black,
                                        containerColor = ArenaSurfaceElevated,
                                        labelColor = Color.White
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = ArenaStaffBorder,
                                        selectedBorderColor = ArenaPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Lista de Clases
                        if (filteredClasses.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hay clases disponibles en esta categoría", color = ArenaTextVariant)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(filteredClasses) { gymClass ->
                                    GymClassCard(
                                        gymClass = gymClass,
                                        onClick = { onClassSelected(gymClass) }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> GymNewsFeedView()
                2 -> GymProfileView()
            }
        }
    }
}

@Composable
fun GymNewsFeedView() {
    val newsList = listOf(
        Triple(
            "GRAN TORNEO WOD CROSSFIT 2026",
            "Inscripciones abiertas para la competencia nacional de atletas Elite. Premiazo de $2,000 en equipamiento y suplementación.",
            "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600"
        ),
        Triple(
            "NUEVO COACH DE POWERLIFTING",
            "Damos la bienvenida a Elena Rostova, especialista en técnica biomecánica y desarrollo de fuerza máxima.",
            "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=600"
        ),
        Triple(
            "HORARIOS ESPECIALES DE FERIADOS",
            "Mantendremos nuestra programación de clases matutinas y nocturnas sin interrupciones. ¡Reserva tus cupos con anticipación!",
            "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=600"
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                "ANUNCIOS Y NOTICIAS DEL CENTRO",
                color = ArenaPrimaryContainer,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }

        items(newsList) { (title, snippet, image) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                        AsyncImage(
                            model = image,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(snippet, color = ArenaTextVariant, fontSize = 11.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GymProfileView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card de Atleta VIP
        Surface(
            color = ArenaSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ArenaPrimaryContainer.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(40.dp),
                    color = ArenaPrimaryContainer.copy(alpha = 0.2f),
                    border = BorderStroke(2.dp, ArenaPrimaryContainer),
                    modifier = Modifier.size(70.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = ArenaPrimaryContainer, modifier = Modifier.size(40.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("ATLETA VIP ARENA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text("Membresía Ilimitada Activa", color = ArenaSuccess, fontWeight = FontWeight.Bold, fontSize = 11.sp)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ArenaStaffBorder)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SALDO", color = ArenaTextVariant, fontSize = 10.sp)
                        Text("15 Tokens", color = ArenaPrimaryContainer, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ESTADO", color = ArenaTextVariant, fontSize = 10.sp)
                        Text("AL DÍA", color = ArenaSuccess, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NIVEL", color = ArenaTextVariant, fontSize = 10.sp)
                        Text("ELITE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }

        // Pase Digital de Acceso Rápido
        Surface(
            color = ArenaSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PASE DIGITAL DE INGRESO RÁPIDO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(130.dp).padding(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.QrCode2, null, tint = Color.Black, modifier = Modifier.fillMaxSize())
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Muestra este QR en la entrada del gimnasio", color = ArenaTextVariant, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun GymClassCard(
    gymClass: GymClass,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArenaSurfaceElevated.copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, ArenaStaffBorder.copy(alpha = 0.5f))
    ) {
        Column {
            // Header Image con Hora en Escala Media en la esquina superior izquierda
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                AsyncImage(
                    model = gymClass.imageUrl,
                    contentDescription = gymClass.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, ArenaSurfaceElevated),
                                startY = 40f
                            )
                        )
                )

                // Hora en escala media donde estaba la box de intensidad
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    border = BorderStroke(1.dp, ArenaPrimaryContainer.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = ArenaPrimaryContainer, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            gymClass.scheduleTime,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Details
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    gymClass.name.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    gymClass.description,
                    color = ArenaTextVariant,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = ArenaSuccess, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(gymClass.coachName, color = ArenaTextVariant, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar de Cupos
                val progress = gymClass.currentEnrolled.toFloat() / gymClass.maxCapacity.toFloat()
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cupos inscriptos", color = ArenaTextVariant, fontSize = 10.sp)
                        Text("${gymClass.currentEnrolled} / ${gymClass.maxCapacity} Atletas", color = ArenaSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = ArenaSuccess,
                        trackColor = ArenaSurfaceBase
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Button
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer, contentColor = Color.Black)
                ) {
                    Text("RESERVAR CLASE", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Catálogo de Clases Gym")
@Composable
fun GymClassSelectionScreenPreview() {
    EliteAthleteOSTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            GymClassSelectionScreen(
                sedeId = "sede_gym_1",
                sedeName = "Centro de Entrenamiento Central",
                onNavigateBack = {},
                onClassSelected = {}
            )
        }
    }
}
