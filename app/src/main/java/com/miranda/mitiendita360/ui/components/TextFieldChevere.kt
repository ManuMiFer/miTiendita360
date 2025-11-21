package com.miranda.mitiendita360.ui.components

import androidx.compose.animation.core.copy
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.miranda.mitiendita360.ui.theme.xd

@Composable
fun TextFieldChevere(
    // Parámetros que el Composable necesita para funcionar
    value: String, onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    // Parámetro opcional para cambiar la acción del teclado
    imeAction: ImeAction = ImeAction.Next
) {
    // Controladores para el teclado y el foco
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Etiqueta del campo de texto
        Text(
            text = label,
            textAlign = TextAlign.Start,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp) // Espacio entre etiqueta y campo
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(13.dp))
                .background(Color.White),
            contentAlignment = Alignment.CenterStart
        ) {
            // Campo de texto básico
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                cursorBrush = SolidColor(Color.Gray),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                )
            )
            // Placeholder que aparece cuando el campo está vacío y sin foco
            if (!isFocused && value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
@Composable
fun SearchTextField(
    // Parámetros que el Composable necesita para funcionar
    value: String, onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier

) {
    var isFocused by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(100.dp))
                .background(xd),
            contentAlignment = Alignment.CenterStart
        ) {

            Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp)
                )
            // Campo de texto básico
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 50.dp)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                cursorBrush = SolidColor(Color.Gray),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction),
                keyboardActions = keyboardActions
            )
            // Placeholder que aparece cuando el campo está vacío y sin foco
            if (!isFocused && value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 50.dp),
                )
            }
        }
    }
}
@Composable
fun TextFieldChevere2(
    // Parámetros que el Composable necesita para funcionar
    value: String, onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    imeAction: ImeAction = ImeAction.Next,
    keyboarType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    color: Color,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    // Controladores para el teclado y el foco
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Etiqueta del campo de texto
        Text(
            text = label,
            textAlign = TextAlign.Start,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp) // Espacio entre etiqueta y campo
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(13.dp))
                .background(color),
            contentAlignment = Alignment.CenterStart
        ) {
            // Campo de texto básico
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                cursorBrush = SolidColor(Color.Gray),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction, keyboardType = keyboarType),
                keyboardActions = keyboardActions,
                enabled = enabled,
            )
            // Placeholder que aparece cuando el campo está vacío y sin foco
            if (!isFocused && value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
fun SearchTextField2(
    // Parámetros que el Composable necesita para funcionar
    value: String, onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier,

) {
    var isFocused by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(13.dp))
                .background(xd),
            contentAlignment = Alignment.CenterStart
        ) {

            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Color.Gray,
                modifier = Modifier.padding(start = 16.dp)
            )
            // Campo de texto básico
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 50.dp)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                cursorBrush = SolidColor(Color.Gray),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction),
                keyboardActions = keyboardActions
            )
            // Placeholder que aparece cuando el campo está vacío y sin foco
            if (!isFocused && value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 50.dp),
                )
            }
        }
    }
}
@Composable
fun TextAreaChevere(
    // Parámetros que el Composable necesita para funcionar
    value: String, onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    // La acción por defecto para un campo multi-línea suele ser 'None' o 'Default'
    imeAction: ImeAction = ImeAction.Default,
    keyboarType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    color: Color,
    // Parámetro opcional para definir la altura mínima
    minHeight: Dp = 120.dp
) {
    // Controladores para el teclado y el foco
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Etiqueta del campo de texto
        Text(
            text = label,
            textAlign = TextAlign.Start,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp) // Espacio entre etiqueta y campo
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(13.dp))
                .background(color),
            // Alineamos el contenido (placeholder) arriba a la izquierda
            contentAlignment = Alignment.TopStart
        ) {
            // Campo de texto básico
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight) // <-- CAMBIO CLAVE: Altura mínima
                    .padding(16.dp)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                cursorBrush = SolidColor(Color.Gray),
                // --- CAMBIOS CLAVE PARA MULTI-LÍNEA ---
                // singleLine = true, // <-- ELIMINADO para permitir múltiples líneas
                keyboardOptions = KeyboardOptions.Default.copy(
                    // Capitaliza las oraciones automáticamente, ideal para descripciones
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = imeAction,
                    keyboardType = keyboarType
                ),
                keyboardActions = KeyboardActions(
                    // La acción por defecto es suficiente, el usuario usa "Enter" para nueva línea
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                enabled = enabled,
            )
            // Placeholder que aparece cuando el campo está vacío
            // Se alinea arriba a la izquierda gracias al 'contentAlignment' del Box
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
fun TextFieldChevereBasico(
    // Parámetros que el Composable necesita para funcionar
    value: String, onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction = ImeAction.Next,
    keyboarType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    color: Color,
    colorTexto: Color
) {
    // Controladores para el teclado y el foco
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(13.dp))
                .background(color),
            contentAlignment = Alignment.CenterStart
        ) {
            // Campo de texto básico
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                cursorBrush = SolidColor(Color.Gray),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction, keyboardType = keyboarType),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                enabled = enabled,
                textStyle = TextStyle(
                    color = colorTexto
                )
            )
            // Placeholder que aparece cuando el campo está vacío y sin foco
            if (!isFocused && value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableDropdown(
    // Parámetros del Dropdown
    options: List<T>,
    onValueChange: (T) -> Unit,
    optionToString: (T) -> String,

    // Parámetros del Campo de Búsqueda
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    placeholder: String,

    // Parámetros de Estilo y UI
    color: Color,
    colorFlecha: Color,
    colorTexto: Color
) {
    var expanded by remember { mutableStateOf(false) }

    // Filtra las opciones basándose en el texto de búsqueda

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            // --- CAMBIO CLAVE ---
            // Usamos un Box para superponer el campo de búsqueda y el icono de la flecha
            Box(
                modifier = Modifier
                    .menuAnchor() // IMPORTANTE: Ancla el menú a este Box
                    .fillMaxWidth()
            ) {
                // 1. TU CAMPO DE BÚSQUEDA
                // (Le quitamos el fondo y el clip para que el 'container' del Dropdown lo maneje)
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                expanded = true // Abre el menú cuando el campo obtiene el foco
                            }
                        },
                    textStyle = LocalTextStyle.current.copy(color = colorTexto),
                    singleLine = true,
                    cursorBrush = SolidColor(Color.White)
                ) { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = searchQuery,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = remember { MutableInteractionSource() },
                        // Icono de búsqueda al inicio
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = Color.Gray
                            )
                        },
                        // Icono de flecha al final
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Desplegar menú",
                                tint = colorFlecha,
                            )
                        },
                        // El placeholder se muestra aquí
                        placeholder = { Text(text = placeholder, color = Color.Gray) },
                        // El 'container' le da la forma y el color de fondo
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
                        },
                        // Padding para que el texto no se pegue a los iconos
                        contentPadding = PaddingValues(start = 0.dp)
                    )
                }
            }

            // 2. EL MENÚ DESPLEGABLE
            // Ahora muestra las opciones filtradas
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White) // Fondo del menú
            ) {
                if (options.isEmpty() && searchQuery.isNotEmpty()) { // Condición mejorada
                    DropdownMenuItem(
                        text = { Text("Sin resultados", color = colorTexto.copy(alpha = 0.5f)) },
                        onClick = { },
                        enabled = false
                    )
                } else {
                    options.forEach { option -> // Usamos 'options'
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = optionToString(option),
                                    color = colorTexto
                                )
                            },
                            onClick = {
                                onValueChange(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
