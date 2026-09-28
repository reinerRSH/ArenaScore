package com.example.arena.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arena.ui.theme.*

@Composable
fun OutlinedTextFieldShared(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibleChange: (Boolean) -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
        shape = RoundedCornerShape(8.dp),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { onPasswordVisibleChange(!passwordVisible) }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = ArenaTextVariant
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ArenaPrimaryContainer,
            unfocusedBorderColor = ArenaUnfocusedBorder,
            focusedLabelColor = ArenaPrimaryContainer,
            unfocusedLabelColor = ArenaTextVariant,
            focusedContainerColor = ArenaSurfaceBase,
            unfocusedContainerColor = ArenaSurfaceBase,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = ArenaPrimaryContainer
        ),
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(
            color = Color.White, 
            fontSize = 14.sp,
            fontFamily = if (isPassword) FontFamily.Default else null
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email)
    )
}
