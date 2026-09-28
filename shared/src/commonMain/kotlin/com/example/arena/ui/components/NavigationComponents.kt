package com.example.arena.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onNotificationClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = title,
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
                Spacer(modifier = Modifier.width(48.dp))
            }
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                BadgedBox(badge = {
                    if (notificationCount > 0) {
                        Badge(containerColor = ArenaWarning) { Text(notificationCount.toString()) }
                    }
                }) {
                    Icon(Icons.Default.Notifications, "Notifications", tint = Color.White)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}
