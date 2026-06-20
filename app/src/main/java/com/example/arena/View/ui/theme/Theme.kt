package com.example.arena.View.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ArenaColorScheme = darkColorScheme(

    background = ArenaBackground,
    surface = ArenaSurfaceBase,
    surfaceVariant = ArenaSurfaceElevated,
    primary = ArenaPrimaryContainer,
    secondary = ArenaMagenta,
    onPrimary = ArenaOnPrimaryFixed,
    onSurface = ArenaTextPrimary,
    onSurfaceVariant = ArenaTextVariant,
    error = ArenaErrorContainer,

)

@Composable
fun EliteAthleteOSTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ArenaColorScheme,
        // Aquí podríamos agregar tipografías personalizadas más adelante
        content = content
    )
}