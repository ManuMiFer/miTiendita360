// Archivo: com/miranda/mitiendita360/PurchaseDetailActivity.kt
package com.miranda.mitiendita360

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miranda.mitiendita360.models.Purchase
import com.miranda.mitiendita360.ui.components.DatePickerField2
import com.miranda.mitiendita360.ui.components.SearchTextField2
import com.miranda.mitiendita360.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PurchaseDetailActivity : ComponentActivity() {
    private val viewModel: PurchaseDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState
            val searchQuery by viewModel.searchQuery

            // --- INICIO DE LA LÓGICA DE ADAPTACIÓN ---
            // Estados para la fecha en formato API ("yyyy-MM-dd"), que usará el DatePicker y el ViewModel.
            var startDateApi by remember { mutableStateOf("") }
            var endDateApi by remember { mutableStateOf("") }

            // Formateadores para la conversión
            val apiFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
            val displayFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

            // Estados derivados para mostrar la fecha en formato UI ("dd/MM/yyyy").
            val startDateDisplay = remember(startDateApi) {
                if (startDateApi.isBlank()) ""
                else try {
                    apiFormat.parse(startDateApi)?.let { displayFormat.format(it) } ?: ""
                } catch (e: Exception) { "" }
            }
            val endDateDisplay = remember(endDateApi) {
                if (endDateApi.isBlank()) ""
                else try {
                    apiFormat.parse(endDateApi)?.let { displayFormat.format(it) } ?: ""
                } catch (e: Exception) { "" }
            }
            MiTiendita360Theme {
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Fondo1)
                        .padding(25.dp)
                ){
                    // --- CABECERA ---
                    Row (verticalAlignment = Alignment.CenterVertically){
                        Image(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "",
                            colorFilter = ColorFilter.tint(color = VerdeLimon),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(180.dp)
                        )
                        Text("Detalle Compras", color = Color.White, fontSize = 30.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- Buscador (Conectado al ViewModel) ---
                    SearchTextField2(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = "Buscar por RUC o Nombre...",
                    )
                    Spacer(modifier = Modifier.padding(5.dp))

                    // --- Filtros de Fecha (AQUÍ ESTÁ LA CORRECCIÓN IMPORTANTE) ---
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column (modifier = Modifier.weight(1f)){
                            DatePickerField2(
                                // AHORA LE PASAMOS UN VALOR FORMATEADO PARA MOSTRAR ("dd/MM/yyyy")
                                selectedDate = startDateDisplay,
                                onDateSelected = { newApiDate -> // Recibimos la nueva fecha en formato API ("yyyy-MM-dd")
                                    // 1. Actualiza el estado API
                                    startDateApi = newApiDate
                                    // 2. Llama al ViewModel con el formato que ya es correcto
                                    viewModel.onStartDateChange(newApiDate.ifBlank { null })
                                },
                                placeholderText = "Desde",
                                containerColor = VerdeLimon,
                                contentColor = GrisClaro,
                                selectedTextColor = GrisClaro,
                            )
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Column (modifier = Modifier.weight(1f)){
                            DatePickerField2(
                                // AHORA LE PASAMOS UN VALOR FORMATEADO PARA MOSTRAR ("dd/MM/yyyy")
                                selectedDate = endDateDisplay,
                                onDateSelected = { newApiDate -> // Recibimos la nueva fecha en formato API ("yyyy-MM-dd")
                                    // 1. Actualiza el estado API
                                    endDateApi = newApiDate
                                    // 2. Llama al ViewModel con el formato que ya es correcto
                                    viewModel.onEndDateChange(newApiDate.ifBlank { null })
                                },
                                placeholderText = "Hasta",
                                containerColor = GrisClaro,
                                contentColor = VerdeLimon,
                                selectedTextColor = VerdeLimon,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.padding(15.dp))

                    // --- Área de Contenido Dinámico ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        when (val state = uiState) {
                            is PurchaseUiState.Loading -> {
                                CircularProgressIndicator(color = VerdeLimon)
                            }
                            is PurchaseUiState.Error -> {
                                Text(
                                    text = state.message,
                                    color = Color.Red,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            is PurchaseUiState.Success -> {
                                if (state.purchases.isEmpty()) {
                                    Text(text = "No se encontraron compras.", color = Color.White)
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(bottom = 20.dp)
                                    ) {
                                        items(state.purchases, key = { it.id }) { purchase ->
                                            PurchaseCard(purchase = purchase)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Composable PurchaseCard (sin cambios)
// En PurchaseDetailActivity.kt

@Composable
fun PurchaseCard(purchase: Purchase) {
    val displayDate: String = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(purchase.deliveryDate) // `deliveryDate` ahora es un String
        date?.let { outputFormat.format(it) } ?: purchase.deliveryDate
    } catch (e: Exception) {
        purchase.deliveryDate // Si hay un error, muestra la fecha tal como viene
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GrisClaro)
            .padding(15.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row {
                Text("ID Compra: ", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("#${purchase.id}", color = VerdeLimon, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.padding(5.dp))
            Text(purchase.providerName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("RUC: ${purchase.providerRuc}", color = GrisClaro2, fontSize = 18.sp)

            // Usamos la fecha ya formateada
            Text(displayDate, color = Color.White, fontSize = 15.sp)

            HorizontalDivider(thickness = 1.dp, color = Color.White, modifier = Modifier.padding(vertical = 5.dp))

            // ... el resto del Composable no cambia ...
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(scrollState, enabled = false)
                ) {
                    purchase.products.forEach { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(product.productName, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Row {
                                Text("Cant: ${product.quantity} x ", color = GrisClaro2, fontSize = 16.sp)
                                Text("S/%.2f".format(product.unitPrice), color = Color.White, fontSize = 16.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                CustomScrollbar(scrollState = scrollState, modifier = Modifier.fillMaxHeight())
            }

            HorizontalDivider(thickness = 1.dp, color = Color.White, modifier = Modifier.padding(vertical = 5.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Gasto Total: ", color = VerdeLimon, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("S/ ${"%.2f".format(purchase.totalAmount)}", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}



// Composable CustomScrollbar (sin cambios)
@Composable
fun CustomScrollbar(scrollState: ScrollState, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    BoxWithConstraints(
        modifier = modifier
            .width(6.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    if (scrollState.maxValue > 0) {
                        val scrollableDistance = this.size.height
                        val scrollRatio = scrollState.maxValue / scrollableDistance.toFloat()
                        scope.launch {
                            scrollState.scrollTo(
                                (scrollState.value + (dragAmount * scrollRatio)).coerceIn(
                                    0f,
                                    scrollState.maxValue.toFloat()
                                ).toInt()
                            )
                        }
                    }
                }
            }
    ) {
        if (scrollState.maxValue > 0) {
            val thumbHeight = this.maxHeight * (scrollState.viewportSize.toFloat() / (scrollState.viewportSize + scrollState.maxValue))
            val thumbOffset = (this.maxHeight - thumbHeight) * (scrollState.value.toFloat() / scrollState.maxValue)
            Spacer(modifier = Modifier
                .fillMaxSize()
                .background(GrisClaro2.copy(alpha = 0.3f), CircleShape))
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(thumbHeight)
                .offset(y = thumbOffset)
                .background(VerdeLimon, CircleShape))
        }
    }
}
