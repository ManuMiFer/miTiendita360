package com.miranda.mitiendita360.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.VerdeLimon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownChevere(
    label: String,
    options: List<T>,
    selectedValue: String,
    onValueChange: (T) -> Unit,
    optionToString: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "Dropdown Arrow Rotation" // Etiqueta para herramientas de depuración
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = label,
            textAlign = TextAlign.Start,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BasicTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .height(50.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Start
                )
            ) { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = selectedValue,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = remember { MutableInteractionSource() },
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    trailingIcon = { Icon(
                        imageVector = Icons.Default.ArrowDropDown ,
                        contentDescription = "Desplegar menú",
                        tint = VerdeLimon,
                        modifier = Modifier.rotate(rotationState)
                    ) },
                    container = {
                        OutlinedTextFieldDefaults.ContainerBox(
                            enabled = true,
                            isError = false,
                            interactionSource = remember { MutableInteractionSource() },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(13.dp),
                        )
                    }
                )
            }
            // Este es el menú desplegable que aparece
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(
                            text = optionToString(option),
                            color = Color.Black)
                        },
                        onClick = {
                            onValueChange(option) // Actualiza el valor seleccionado
                            expanded = false      // Cierra el menú
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownChevere2(
    label: String,
    options: List<T>,
    selectedValue: String,
    onValueChange: (T) -> Unit,
    optionToString: (T) -> String,
    color: Color,
    colorFlecha: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "Dropdown Arrow Rotation" // Etiqueta para herramientas de depuración
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = label,
            textAlign = TextAlign.Start,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BasicTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .height(50.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Start
                )
            ) { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = selectedValue,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = remember { MutableInteractionSource() },
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    trailingIcon = { Icon(
                        imageVector = Icons.Default.ArrowDropDown ,
                        contentDescription = "Desplegar menú",
                        tint = colorFlecha,
                        modifier = Modifier.rotate(rotationState)
                    ) },
                    container = {
                        OutlinedTextFieldDefaults.ContainerBox(
                            enabled = true,
                            isError = false,
                            interactionSource = remember { MutableInteractionSource() },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = color,
                                focusedContainerColor = color,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(13.dp),
                        )
                    }
                )
            }
            // Este es el menú desplegable que aparece
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(
                            text = optionToString(option),
                            color = Color.Black)
                        },
                        onClick = {
                            onValueChange(option) // Actualiza el valor seleccionado
                            expanded = false      // Cierra el menú
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownChevereBasico(
    options: List<T>,
    selectedValue: String,
    onValueChange: (T) -> Unit,
    optionToString: (T) -> String,
    color: Color,
    colorFlecha: Color,
    colorTexto: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "Dropdown Arrow Rotation" // Etiqueta para herramientas de depuración
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BasicTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .height(50.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Start,
                    color = colorTexto
                )
            ) { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = selectedValue,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = remember { MutableInteractionSource() },
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    trailingIcon = { Icon(
                        imageVector = Icons.Default.ArrowDropDown ,
                        contentDescription = "Desplegar menú",
                        tint = colorFlecha,
                        modifier = Modifier.rotate(rotationState)
                    ) },
                    container = {
                        OutlinedTextFieldDefaults.ContainerBox(
                            enabled = true,
                            isError = false,
                            interactionSource = remember { MutableInteractionSource() },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = color,
                                focusedContainerColor = color,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(13.dp),
                        )
                    }
                )
            }
            // Este es el menú desplegable que aparece
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(
                            text = optionToString(option),
                            color = Fondo1
                        )
                        },
                        onClick = {
                            onValueChange(option) // Actualiza el valor seleccionado
                            expanded = false      // Cierra el menú
                        }
                    )
                }
            }
        }
    }
}