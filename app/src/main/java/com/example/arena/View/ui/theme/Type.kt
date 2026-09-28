package com.example.arena.View.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val baseTextStyle = TextStyle(
    fontFamily = FontFamily.Default,
    letterSpacing = 0.15.sp
)

val Typography = Typography(
    displayLarge = baseTextStyle.copy(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    displayMedium = baseTextStyle.copy(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
    displaySmall = baseTextStyle.copy(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
    headlineLarge = baseTextStyle.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = baseTextStyle.copy(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = baseTextStyle.copy(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = baseTextStyle.copy(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    titleMedium = baseTextStyle.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    titleSmall = baseTextStyle.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
    bodyLarge = baseTextStyle.copy(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal),
    bodyMedium = baseTextStyle.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
    bodySmall = baseTextStyle.copy(fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Normal),
    labelLarge = baseTextStyle.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
    labelMedium = baseTextStyle.copy(fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium),
    labelSmall = baseTextStyle.copy(fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.Medium)
)
