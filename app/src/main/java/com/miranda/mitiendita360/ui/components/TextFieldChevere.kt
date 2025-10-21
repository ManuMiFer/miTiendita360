package com.miranda.mitiendita360.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
fun TextFieldChevere2(
    // Parámetros que el Composable necesita para funcionar
    value: String, onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier

) {
    // Controladores para el teclado y el foco
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
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