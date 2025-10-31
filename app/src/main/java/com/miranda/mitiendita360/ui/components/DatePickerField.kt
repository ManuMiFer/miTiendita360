package com.miranda.mitiendita360.ui.components

import android.app.DatePickerDialog
import android.icu.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miranda.mitiendita360.R
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DatePickerField(
    label: String,
    selectedDate: String, // La fecha seleccionada en formato de texto
    onDateSelected: (String) -> Unit,

// Función para devolver la fecha como String
) {
    // Necesitamos el contexto para poder lanzar el DatePickerDialog
    val context = LocalContext.current

    // Formateador para convertir la fecha del selector (Long) a un String legible (dd/MM/yyyy)
    val dateFormatter = remember {
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )
    }

    val showDatePicker = {
        val calendar = Calendar.getInstance()
        if (selectedDate.isNotEmpty()) {
            try {
                calendar.time = dateFormatter.parse(selectedDate)
            } catch (e: Exception) {
            }
        }

        DatePickerDialog(
            context,
            R.style.MiDatePickerDialogTheme,
            { _, year, month, dayOfMonth ->

                val correctMonth = month + 1

                // Formato para la base de datos (YYYY-MM-DD)
                val dbFormatDate = String.format("%04d-%02d-%02d", year, correctMonth, dayOfMonth)

                // Llamamos al callback con la fecha para la BD
                onDateSelected(dbFormatDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            textAlign = TextAlign.Start,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        // Usamos un Box para que el área clickeable sea todo el campo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp) // Altura similar a los otros campos
                .clip(shape = RoundedCornerShape(13.dp))
                .background(Color.White)
                .clickable { showDatePicker() }, // Al hacer clic, mostramos el diálogo
            contentAlignment = Alignment.CenterStart
        ) {
            // Texto que muestra la fecha seleccionada o un placeholder
            Text(
                text = if (selectedDate.isNotEmpty()) selectedDate else "dd/mm/aaaa",
                color = if (selectedDate.isNotEmpty()) Color.Black else Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            // Icono de calendario para indicar que es un campo de fecha
            Icon(
                painter = painterResource(id = R.drawable.calendar), // Asegúrate de tener este icono
                contentDescription = "Seleccionar Fecha",
                tint = VerdeLimon,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(30.dp)
            )
        }
    }
}

@Composable
fun DatePickerField2(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier, // Modifier es ahora el tercer parámetro y opcional
    placeholderText: String = "Filtrar por Fecha",
    containerColor: Color = VerdeLimon,
    contentColor: Color = Fondo1, // Color para el ícono y el texto del placeholder
    selectedTextColor: Color = Color.Black // Color para el texto cuando ya hay una fecha
) {
    val context = LocalContext.current

    val dateFormatter = remember {
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )
    }

    val showDatePicker = {
        val calendar = Calendar.getInstance()
        if (selectedDate.isNotEmpty()) {
            try {
                // Intenta parsear la fecha en formato yyyy-MM-dd que viene de la BD
                val dbFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                calendar.time = dbFormatter.parse(selectedDate)
            } catch (e: Exception) {
                // Si falla, no hace nada y usa la fecha actual
            }
        }

        DatePickerDialog(
            context,
            R.style.MiDatePickerDialogTheme,
            { _, year, month, dayOfMonth ->
                val correctMonth = month + 1
                // Formato para la base de datos (YYYY-MM-DD)
                val dbFormatDate = String.format("%04d-%02d-%02d", year, correctMonth, dayOfMonth)
                onDateSelected(dbFormatDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // El Column ahora usa el modifier que le pasamos desde fuera
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(shape = RoundedCornerShape(13.dp))
                .background(containerColor) // Usa el color del contenedor personalizable
                .clickable { showDatePicker() },
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                painter = painterResource(id = R.drawable.calendar),
                contentDescription = placeholderText, // El content description ahora es más descriptivo
                tint = contentColor, // Usa el color de contenido personalizable
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(30.dp)
            )
            Text(
                text = if (selectedDate.isNotEmpty()) selectedDate else placeholderText, // Usa el texto del placeholder personalizable
                // Elige el color basado en si hay una fecha seleccionada o no
                color = if (selectedDate.isNotEmpty()) selectedTextColor else contentColor,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}