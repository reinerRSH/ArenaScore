package com.example.arena.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arena.ui.theme.*

@Composable
fun HomeArenaContentShared(
    isLoading: Boolean,
    onSportSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(ArenaSurfaceBase)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Mitad Superior: CROSSFIT
            SportSplitSectionShared(
                modifier = Modifier.weight(1f),
                title = "CROSSFIT",
                subtitle = "CIMENTACIÓN DE ALTA INTENSIDAD",
                accentColor = ArenaSuccess,
                liveText = "En vivo: 1,240 Atletas",
                onClick = { onSportSelected("CROSSFIT") }
            )

            // Mitad Inferior: PADEL
            SportSplitSectionShared(
                modifier = Modifier.weight(1f),
                title = "PÁDEL",
                subtitle = "PRECISIÓN Y AGILIDAD",
                accentColor = ArenaPrimaryContainer,
                liveText = "En vivo: 890 Canchas",
                onClick = { onSportSelected("PADEL") }
            )
        }

        TopArenaHeaderShared()

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
fun SportSplitSectionShared(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    accentColor: Color,
    liveText: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth().clickable { onClick() }
    ) {
        // Placeholder for image since resources are tricky in KMP for now
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

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
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = subtitle,
                color = accentColor,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
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
fun TopArenaHeaderShared() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "ARENA", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        IconButton(onClick = { }) {
            Icon(imageVector = Icons.Default.Menu, contentDescription = null, tint = Color.White)
        }
    }
}
