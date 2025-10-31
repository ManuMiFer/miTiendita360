package com.miranda.mitiendita360

import android.content.Intent
import android.icu.text.NumberFormat
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.models.Venta
import com.miranda.mitiendita360.network.RetrofitClient
import com.miranda.mitiendita360.ui.components.DatePickerField2
import com.miranda.mitiendita360.ui.components.SearchTextField
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.GrisClaro2
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import kotlinx.coroutines.launch

sealed interface SaleHistoryUiState {
    data class Success(val ventas: List<Venta>) : SaleHistoryUiState
    object Loading : SaleHistoryUiState
    data class Error(val message: String) : SaleHistoryUiState
}
class SaleHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiTiendita360Theme {
                MiTiendita360Theme {
                    // 2. ESTADOS PARA FILTROS Y UI
                    var uiState by remember { mutableStateOf<SaleHistoryUiState>(SaleHistoryUiState.Loading) }
                    var searchQuery by remember { mutableStateOf("") }
                    var startDate by remember { mutableStateOf("") }
                    var endDate by remember { mutableStateOf("") }

                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                    // 3. FUNCIÓN PARA LLAMAR A LA API
                    fun fetchSales(
                        uid: String,
                        query: String,
                        fechaInicio: String,
                        fechaFin: String
                    ) {
                        if (uid.isBlank()) {
                            uiState = SaleHistoryUiState.Error("Usuario no autenticado.")
                            return
                        }
                        uiState = SaleHistoryUiState.Loading
                        lifecycleScope.launch {
                            uiState = try {
                                val response = RetrofitClient.productoService.getVentas(
                                    userId = uid,
                                    searchTerm = query.ifBlank { null },
                                    fechaInicio = fechaInicio.ifBlank { null },
                                    fechaFin = fechaFin.ifBlank { null }
                                )
                                if (response.success) {
                                    SaleHistoryUiState.Success(response.data)
                                } else {
                                    SaleHistoryUiState.Error(
                                        response.message ?: "Error desconocido"
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("SaleHistory", "Error al cargar ventas", e)
                                SaleHistoryUiState.Error("Error de conexión: ${e.message}")
                            }
                        }
                    }

                    // 4. LÓGICA PARA REACCIONAR A CAMBIOS
                    LaunchedEffect(Unit) { // Carga inicial
                        fetchSales(userId, "", "", "")
                    }
                    LaunchedEffect(searchQuery, startDate, endDate) { // Recargar al cambiar filtros
                        fetchSales(userId, searchQuery, startDate, endDate)
                    }

                    // 5. CÁLCULO DE INGRESOS TOTALES
                    val totalIngresos = remember(uiState) {
                        if (uiState is SaleHistoryUiState.Success) {
                            (uiState as SaleHistoryUiState.Success).ventas.sumOf { it.totalVenta }
                        } else 0.0
                    }

                    val currencyFormat =
                        remember { NumberFormat.getCurrencyInstance(java.util.Locale("es", "PE")) }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            // Pega este bloque completo dentro de topBar = { ... }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(
                                        shape = WideOvalBottomShape(
                                            arcHeight = 300f, // Profundidad de la curva
                                            horizontalControlOffset = 180f
                                        )
                                    )
                                    .height(250.dp) // Altura estándar que usas
                                    .background(color = Fondo1)
                                    .padding(25.dp)
                            ) {
                                Spacer(modifier = Modifier.padding(10.dp))

                                Row (
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                {
                                    Image(
                                        painter = painterResource(R.drawable.tienda), // Usamos el ícono de reporte
                                        contentDescription = "Historial de Ventas",
                                        colorFilter = ColorFilter.tint(color = VerdeLimon),
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(5.dp))
                                    Column {
                                        Text(
                                            "Historial de",
                                            color = Color.White,
                                            fontSize = 25.sp
                                        )
                                        Text(
                                            "Ventas",
                                            color = Color.White,
                                            fontSize = 25.sp,
                                            fontWeight = FontWeight.Bold // Resaltamos el título principal
                                        )
                                    }
                                }
                            }

                        }
                    ) { innerPadding ->
                        Column(
                            Modifier
                                .fillMaxSize()
                                .background(color = GrisClaro)
                                .padding(innerPadding)
                                .padding(horizontal = 40.dp)
                        ) {
                            // --- UI de Filtros ---
                            SearchTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = "Buscar por producto...",
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Column (
                            ) {
                                DatePickerField2(
                                    selectedDate = startDate,
                                    onDateSelected = { startDate = it },
                                    placeholderText = "Desde",
                                            containerColor = Fondo1,     // Fondo blanco
                                    contentColor = VerdeLimon,        // Ícono y placeholder en azul
                                    selectedTextColor = VerdeLimon
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                DatePickerField2(
                                    selectedDate = endDate,
                                    onDateSelected = { endDate = it },    placeholderText = "Hasta", // Nuevo texto
                                    // Texto de fecha seleccionada en gris oscuro
                                    containerColor = VerdeLimon,     // Fondo blanco
                                    contentColor = GrisClaro,        // Ícono y placeholder en azul
                                    selectedTextColor = GrisClaro,
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(shape = RoundedCornerShape(13.dp))
                                    .background(color = Fondo1)
                                    .padding(10.dp),
                            ) {
                                Text(
                                    "Ingresos Totales:",
                                    color = VerdeLimon,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    currencyFormat.format(totalIngresos),
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(15.dp))

                            // --- UI de la Lista de Ventas ---
                            Box(modifier = Modifier.fillMaxSize()) {
                                when (val state = uiState) {
                                    is SaleHistoryUiState.Loading -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.align(
                                                Alignment.Center
                                            )
                                        )
                                    }

                                    is SaleHistoryUiState.Error -> {
                                        Text(
                                            text = state.message,
                                            color = Color.Red,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .padding(16.dp)
                                        )
                                    }

                                    is SaleHistoryUiState.Success -> {
                                        if (state.ventas.isEmpty()) {
                                            Text(
                                                text = "No se encontraron ventas con los filtros aplicados.",
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        } else {
                                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                items(
                                                    items = state.ventas,
                                                    key = { it.id }
                                                ) { venta ->
                                                    SaleItemCard(
                                                        venta = venta,
                                                        currencyFormat = currencyFormat,

                                                    )
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
    }

    // Composable para cada elemento de la lista
    @Composable
    fun SaleItemCard(venta: Venta,
                     currencyFormat: NumberFormat,
                     onItemClick: () -> Unit = {}
    ) {
        val context = LocalContext.current
        // Formateadores de fecha y hora
        val inputFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            java.util.Locale.getDefault()
        )
        val outputDateFormat = SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val outputTimeFormat = SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())

        val dateObject = try {
            inputFormat.parse(venta.fecha)
        } catch (e: Exception) {
            null
        }
        val fechaFormateada = dateObject?.let { outputDateFormat.format(it) } ?: "N/A"
        val horaFormateada = dateObject?.let { outputTimeFormat.format(it) } ?: "N/A"

        Row(
            modifier = Modifier
                .fillMaxWidth() // Corregido: fillMaxWidth() en lugar de fillMaxSize()
                .clip(shape = RoundedCornerShape(13.dp))
                .background(color = Color.White)
                .padding(12.dp)
                .clickable {
                    val intent = Intent(context, SaleDetailActivity::class.java).apply {
                        putExtra("SALE_ID", venta.id)
                    }
                    context.startActivity(intent)
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "ID: ${venta.id}",
                    color = Fondo1,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Fecha: $fechaFormateada", color = Fondo1, fontSize = 16.sp)
                Text("Hora: $horaFormateada", color = Fondo1, fontSize = 16.sp)
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    currencyFormat.format(venta.totalVenta),
                    color = Fondo1,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${venta.cantidadProductos} productos",
                    color = GrisClaro2,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

