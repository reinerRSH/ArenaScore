package com.example.arena.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Wc
import androidx.compose.ui.graphics.vector.ImageVector

data class ServiceItem(
    val name: String,
    val icon: ImageVector
)

val servicesList = listOf(
    ServiceItem("Baños", Icons.Default.Wc),
    ServiceItem("Duchas", Icons.Default.Shower),
    ServiceItem("Estacionamiento", Icons.Default.DirectionsCar),
    ServiceItem("Restaurante", Icons.Default.Restaurant),
    ServiceItem("Aire Acondicionado", Icons.Default.AcUnit)
)
