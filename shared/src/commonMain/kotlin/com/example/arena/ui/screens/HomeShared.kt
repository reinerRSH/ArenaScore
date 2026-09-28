package com.example.arena.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.layout.ContentScale
import com.example.arena.domain.Reserva
import com.example.arena.ui.theme.*
import com.example.arena.ui.components.ScanlineEffectShared

@Composable
fun HomeArenaContentShared(
    isLoading: Boolean,
    crossfitPainter: Painter? = null,
    padelPainter: Painter? = null,
    crossfitTitle: String = "CENTRO DE ENTRENAMIENTO",
    crossfitSubtitle: String = "CIMENTACIÓN DE ALTA INTENSIDAD",
    padelTitle: String = "PÁDEL",
    padelSubtitle: String = "PRECISIÓN Y AGILIDAD",
    onSportSelected: (String) -> Unit,
    onShowReceipt: (Reserva) -> Unit = {},
    onLogout: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    courtImageProvider: @Composable (String, Modifier) -> Unit = { _, _ -> }
) {
    Box(
        modifier = Modifier.fillMaxSize().background(ArenaSurfaceBase)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SportSplitSectionShared(
                modifier = Modifier.weight(1f),
                title = crossfitTitle,
                subtitle = crossfitSubtitle,
                accentColor = ArenaSuccess,
                liveText = "En vivo: 1,240 Atletas",
                imagePainter = crossfitPainter,
                onClick = { onSportSelected("CROSSFIT") }
            )

            SportSplitSectionShared(
                modifier = Modifier.weight(1f),
                title = padelTitle,
                subtitle = padelSubtitle,
                accentColor = ArenaPrimaryContainer,
                liveText = "En vivo: 890 Canchas",
                imagePainter = padelPainter,
                onClick = { onSportSelected("PADEL") }
            )
        }

        TopArenaHeaderShared(
            onLogout = onLogout,
            onProfileClick = onProfileClick
        )

        ScanlineEffectShared()

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ArenaSuccess)
            }
        }
    }
}

@Composable
fun UserNotificationsSheetShared(
    notifications: List<Reserva>,
    onNotificationClick: (Reserva) -> Unit,
    courtImageProvider: @Composable (String, Modifier) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "NOTIFICACIONES", 
            color = ArenaPrimaryContainer, 
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        HorizontalDivider(color = ArenaStaffBorder, modifier = Modifier.padding(bottom = 16.dp))
        
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp), 
                contentAlignment = Alignment.Center
            ) {
                Text("No tienes notificaciones nuevas", color = ArenaTextVariant, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(notifications) { reserva ->
                    Surface(
                        color = ArenaSurfaceBase,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onNotificationClick(reserva) }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(4.dp),
                                color = ArenaSurfaceElevated
                            ) {
                                courtImageProvider(reserva.canchaImageUrl, Modifier.fillMaxSize())
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Reserva Confirmada", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${reserva.canchaid} - ${reserva.fecha}", color = ArenaTextVariant, fontSize = 12.sp)
                                Text("Toca para ver comprobante", color = ArenaPrimaryContainer, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp)) // Espacio para el drag handle si no se usa
    }
}

@Composable
fun SportSplitSectionShared(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    accentColor: Color,
    liveText: String,
    imagePainter: Painter? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth().clickable { onClick() }
    ) {
        if (imagePainter != null) {
            androidx.compose.foundation.Image(
                painter = imagePainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alpha = 0.6f
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, ArenaSurfaceBase.copy(alpha = 0.9f)),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = if (title.length > 15) 20.sp else 28.sp,
                lineHeight = if (title.length > 15) 24.sp else 32.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = accentColor,
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ENTRAR A ARENA",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
            LiveIndicatorShared(text = liveText, color = accentColor)
        }
    }
}

@Composable
fun LiveIndicatorShared(text: String, color: Color) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color = color.copy(alpha = alpha), shape = CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = color.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
fun TopArenaHeaderShared(
    onLogout: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onProfileClick) {
                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "ARENA", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onLogout) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
            }
        }
    }
}
