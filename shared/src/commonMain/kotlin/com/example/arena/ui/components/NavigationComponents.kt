package com.example.arena.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.arena.ui.theme.ArenaPrimaryContainer
import com.example.arena.ui.theme.ArenaWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArenaTopBarShared(
    title: String = "ARENA",
    showBack: Boolean = true,
    notificationCount: Int = 0,
    onNavigateBack: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = ArenaPrimaryContainer
                    )
                )
            }
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ArenaPrimaryContainer)
                }
            } else {
                IconButton(onClick = onProfileClick) {
                    Icon(Icons.Default.AccountCircle, "Profile", tint = ArenaPrimaryContainer)
                }
            }
        },
        actions = {
            // Icono de Historial (DRY: mismo estilo cian)
            IconButton(onClick = onHistoryClick) {
                Icon(Icons.Filled.History, "History", tint = ArenaPrimaryContainer)
            }
            
            // Icono de Notificaciones con BadgedBox de Material 3
            IconButton(onClick = onNotificationClick) {
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ) {
                                // Punto rojo dinámico o número
                                if (notificationCount > 9) Text("+9") else Text(notificationCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Notifications, "Notifications", tint = Color.White)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}
