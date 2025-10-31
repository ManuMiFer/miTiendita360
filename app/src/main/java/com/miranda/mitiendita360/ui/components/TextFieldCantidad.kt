import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miranda.mitiendita360.ui.components.ActionButton
import com.miranda.mitiendita360.ui.theme.GrisClaro

@Composable
fun TextFieldCantidadChevere(
    value: Int, onValueChange: (Int) -> Unit,
    label: String,
    stockDisponible: Int,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Etiqueta del campo
        Text(
            text = label,
            textAlign = TextAlign.Start,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(13.dp))
                .background(color), // Fondo personalizable
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp), // Padding ajustado
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- BOTÓN DE DECREMENTAR (AHORA CIRCULAR) ---
                ActionButton(
                    onClick = {
                        if (value > 1 && enabled) {
                            onValueChange(value - 1)
                        }
                    },
                    icon = Icons.Default.Remove,
                    backgroundColor = GrisClaro, // Color sólido
                    iconColor = Color.White,
                    buttonSize = 38.dp,
                    iconSize = 22.dp
                )

                // --- CAMPO DE TEXTO NUMÉRICO CENTRAL ---
                // --- CAMPO DE TEXTO NUMÉRICO CENTRAL ---
                Column (
                    modifier = Modifier.weight(1f),
                    // --- AÑADE ESTAS DOS LÍNEAS ---
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    BasicTextField(
                        value = if (value == 0) "" else value.toString(),
                        onValueChange = { newValue ->
                            if (newValue.isEmpty()) {
                                onValueChange(0)
                            } else {
                                val newQuantity = newValue.toIntOrNull()
                                // Permite escribir hasta el stock, pero los botones +/- se detienen antes
                                if (newQuantity != null && newQuantity <= stockDisponible) {
                                    onValueChange(newQuantity)
                                } else if (newQuantity != null && newQuantity > stockDisponible) {
                                    // Opcional: si el usuario escribe un número mayor, lo ajustamos al máximo
                                    onValueChange(stockDisponible)
                                }
                            }
                        },
                        // --- ELIMINA EL PADDING HORIZONTAL EXCESIVO ---
                        modifier = Modifier.fillMaxWidth(), // Dale un ancho fijo para centrarlo bien
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        cursorBrush = SolidColor(Color.White),
                        singleLine = true,
                        enabled = enabled
                    )
                }


                // --- BOTÓN DE INCREMENTAR (AHORA CIRCULAR) ---
                ActionButton(
                    onClick = {
                        if (value < stockDisponible && enabled) {
                            onValueChange(value + 1)
                        }
                    },
                    icon = Icons.Default.Add,
                    backgroundColor = GrisClaro, // Color sólido
                    iconColor = Color.White,
                    buttonSize = 38.dp,
                    iconSize = 22.dp
                )
            }
        }
    }
}
