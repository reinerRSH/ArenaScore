package com.example.arena.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.arena.domain.Reserva

@Composable
fun ReceiptModalShared(
    reserva: Reserva?,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    if (reserva == null) return
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ArenaSurfaceElevated,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, ArenaPrimaryContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Info, null, tint = ArenaPrimaryContainer, modifier = Modifier.size(48.dp))
                Text("COMPROBANTE DE RESERVA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, modifier = Modifier.padding(top = 16.dp))
                HorizontalDivider(color = ArenaStaffBorder, modifier = Modifier.padding(vertical = 16.dp))
                
                ReceiptRowShared("CANCHA", reserva.canchaid)
                ReceiptRowShared("FECHA", reserva.fecha)
                ReceiptRowShared("HORARIO", "${reserva.horaInicio} - ${reserva.horaFin}")
                ReceiptRowShared("REFERENCIA", reserva.referenciaPago)
                ReceiptRowShared("TOTAL", "$${reserva.montoTotal}", color = ArenaPrimaryContainer)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, ArenaPrimaryContainer)) {
                        Icon(Icons.Default.Share, null, tint = ArenaPrimaryContainer, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compartir", color = ArenaPrimaryContainer, fontSize = 12.sp)
                    }
                    Button(onClick = onDownload, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = ArenaPrimaryContainer, contentColor = Color.Black)) {
                        Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bajar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                
                TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 8.dp)) {
                    Text("CERRAR", color = ArenaTextVariant)
                }
            }
        }
    }
}

@Composable
fun ReceiptRowShared(label: String, value: String, color: Color = Color.White) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = ArenaTextVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
    }
}
