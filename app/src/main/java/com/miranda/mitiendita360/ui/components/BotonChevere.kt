package com.miranda.mitiendita360.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BotonChevere(
    // Parámetros para personalizar el botón
    texto: String,
    colorFondo: Color,
    colorTexto: Color,
    onClick: () -> Unit // Parámetro para la acción al hacer clic
) {
    Button(
        onClick = onClick, // Usa la acción que se pasa como parámetro
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp), // Altura consistente
        shape = RoundedCornerShape(100.dp), // Esquinas consistentes con los text fields
        colors = ButtonDefaults.buttonColors(
            containerColor = colorFondo,
            contentColor = colorTexto
        )
    ) {
        Text(
            text = texto,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}