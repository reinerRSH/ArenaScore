package com.example.arena.ui.court

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun CourtCard(
    court: Court,
    onClick: () -> Unit
) {
    val isAvailable = court.isAvailable
    val cardAlpha = if (isAvailable) 1f else 0.6f
    val backgroundColor = if (isAvailable) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = borderStroke(isAvailable)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Stripes for Unavailable State
            if (!isAvailable) {
                DiagonalStripes(modifier = Modifier.matchParentSize())
            }

            Column {
                // Image Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    AsyncImage(
                        model = court.imageUrl,
                        contentDescription = court.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, backgroundColor),
                                    startY = 50f
                                )
                            )
                    )

                    // Type Badge
                    Surface(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopEnd),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(4.dp),
                        border = borderStroke(isAvailable, alpha = 0.3f)
                    ) {
                        Text(
                            text = court.type,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = court.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (!isAvailable) {
                            StatusBadge(court.status)
                        }
                    }

                    Text(
                        text = court.idTechnical,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Slot Duration
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp),
                        border = borderStroke(false, alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DURACIÓN TURNO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = court.duration,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Button
                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isAvailable,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isAvailable) "SELECCIONAR CANCHA" else "NO DISPONIBLE",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (isAvailable) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: CourtStatus) {
    val color = when (status) {
        CourtStatus.AVAILABLE -> MaterialTheme.colorScheme.secondary // Magenta
        CourtStatus.RESERVED -> MaterialTheme.colorScheme.error
        CourtStatus.BUSY -> Color(0xFFF59E0B) // Warning/Orange
        CourtStatus.MAINTENANCE -> Color.Gray
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}

@Composable
fun DiagonalStripes(modifier: Modifier = Modifier) {
    val strokeColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    Canvas(modifier = modifier) {
        val step = 20.dp.toPx()
        val strokeWidth = 1.dp.toPx()
        for (i in -size.width.toInt()..size.width.toInt() + size.height.toInt() step step.toInt()) {
            drawLine(
                color = strokeColor,
                start = Offset(i.toFloat(), 0f),
                end = Offset(i.toFloat() + size.height, size.height),
                strokeWidth = strokeWidth
            )
        }
    }
}

@Composable
private fun borderStroke(isAvailable: Boolean, alpha: Float = 0.3f) = 
    androidx.compose.foundation.BorderStroke(
        width = 1.dp,
        color = if (isAvailable) {
            MaterialTheme.colorScheme.primary.copy(alpha = alpha)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = alpha)
        }
    )
