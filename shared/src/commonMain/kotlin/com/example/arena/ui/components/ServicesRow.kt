package com.example.arena.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.arena.util.servicesList
import com.example.arena.ui.theme.ArenaPrimaryContainer

/**
 * Renderiza una fila de iconos representativos de los servicios de la sede.
 * @param servicios Lista de nombres de servicios obtenidos de Firestore.
 */
@Composable
fun ServiciosRow(
    servicios: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        servicesList.forEach { item ->
            // Comparación insensible a mayúsculas para coincidir con Firestore
            if (servicios.any { it.equals(item.name, ignoreCase = true) }) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.name,
                    tint = ArenaPrimaryContainer, // Cian del proyecto
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
