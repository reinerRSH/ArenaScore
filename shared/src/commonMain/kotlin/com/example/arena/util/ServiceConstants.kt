package com.example.arena.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.LocalParking
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
    ServiceItem("Duchas", Icons.Default.Bathtub),
    ServiceItem("Estacionamiento", Icons.Default.LocalParking),
    ServiceItem("Restaurante", Icons.Default.Restaurant),
    ServiceItem("Aire Acondicionado", Icons.Default.AcUnit)
)
