package com.miranda.mitiendita360.ui.components

import android.graphics.drawable.Icon
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Gray,
    iconColor: Color = Color.White,
    iconSize: Dp = 30.dp,
    buttonSize: Dp = 40.dp // Tamaño total del botón, un poco más grande que el icono
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(buttonSize) // Establece el tamaño total del botón
            .clip(CircleShape) // Corta el Box en forma de círculo
            .background(backgroundColor) // Aplica el color de fondo
            .clickable  ( onClick = {onClick()},
                ) // Hace que el Box sea cliqueable
            .padding(2.dp) // Un pequeño padding interior si se desea
    ) {
        Image(
            imageVector = icon, // Utiliza el icono proporcionado,
            contentDescription = null, // Descripción para accesibilidad, null si es decorativo
            modifier = Modifier.size(iconSize), // Aplica el tamaño al icono
            colorFilter = ColorFilter.tint(iconColor) // Aplica el color al icono
        )

    }
}

@Composable
fun ActionButton2(
    onClick:  () -> Unit,
    @DrawableRes iconResource: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Gray,
    iconColor: Color = Color.White,
    iconSize: Dp = 30.dp,
    buttonSize: Dp = 40.dp // Tamaño total del botón, un poco más grande que el icono
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(buttonSize) // Establece el tamaño total del botón
            .clip(CircleShape) // Corta el Box en forma de círculo
            .background(backgroundColor) // Aplica el color de fondo
            .clickable { onClick() } // Hace que el Box sea cliqueable
            .padding(2.dp) // Un pequeño padding interior si se desea
    ) {
        Image(
            painter = painterResource(id = iconResource), // Utiliza el icono proporcionado,
            contentDescription = null, // Descripción para accesibilidad, null si es decorativo
            modifier = Modifier.size(iconSize), // Aplica el tamaño al icono
            colorFilter = ColorFilter.tint(iconColor) // Aplica el color al icono
        )

    }
}