package com.example.arena.ui.home



import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arena.R

// --- COLORES TÁCTICOS (Asegúrate de tenerlos en tu Theme, o usa estos locales para probar) ---
private val ArenaSurfaceBase = Color(0xFF0F0F10)
private val ArenaGreen = Color(0xFFCCFF00)
private val ArenaCyan = Color(0xFF00E5FF)

@Composable
fun HomeArenaScreen(
    // navController: NavController, // Descomenta cuando lo conectes a la navegación
    // viewModel: HomeViewModel = hiltViewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArenaSurfaceBase)
    ) {
        // --- CONTENIDO DIVIDIDO EN 50/50 ---
        Column(modifier = Modifier.fillMaxSize()) {

            // Mitad Superior: CROSSFIT
            SportSplitSection(
                modifier = Modifier.weight(1f),
                title = "CROSSFIT",
                subtitle = "HIGH INTENSITY FOUNDATION",
                accentColor = ArenaGreen,
                liveText = "Live 1,240 Athletes",
                imageRes = R.drawable.crossfit, // TODO: Cambiar por R.drawable.bg_crossfit
                alignment = Alignment.BottomStart,
                onClick = { /* TODO: Navegar al dashboard de CrossFit */ }
            )

            // Mitad Inferior: PADEL
            SportSplitSection(
                modifier = Modifier.weight(1f),
                title = "PADEL",
                subtitle = "PRECISION & AGILITY",
                accentColor = ArenaCyan,
                liveText = "Live 890 Courts",
                imageRes = R.drawable.padel, // TODO: Cambiar por R.drawable.bg_padel
                alignment = Alignment.BottomEnd,
                onClick = { /* TODO: Navegar al dashboard de Padel */ }
            )
        }

        // --- HEADER FLOTANTE (Queda por encima del contenido) ---
        TopArenaHeader()
    }
}

@Composable
fun SportSplitSection(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    accentColor: Color,
    liveText: String,
    imageRes: Int,
    alignment: Alignment,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        // 1. Imagen de Fondo
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Background for $title",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.6f // Oscurecemos la imagen un poco para simular el overlay
        )

        // 2. Gradiente Oscuro (Desde la base hacia arriba para que el texto resalte)
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

        // 3. Contenido Central (Textos y Botón)
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
                        text = "ENTER ARENA",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Enter",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 4. Indicador de "Live" con animación de pulso
        Box(
            modifier = Modifier
                .align(alignment)
                .padding(24.dp)
        ) {
            LiveIndicator(text = liveText, color = accentColor)
        }
    }
}

@Composable
fun LiveIndicator(text: String, color: Color) {
    // Animación infinita para el punto de estado
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = color.copy(alpha = alpha), shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = color.copy(alpha = 0.8f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            textDecoration = null
        )
    }
}

@Composable
fun TopArenaHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding() // Respeta el notch y la barra de estado
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ARENA",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )

        IconButton(onClick = { /* TODO: Abrir menú lateral o perfil */ }) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color.White
            )
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_7_pro")
@Composable
fun HomeArenaPreview() {
    HomeArenaScreen()
}
