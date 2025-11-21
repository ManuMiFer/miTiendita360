package com.miranda.mitiendita360

import android.app.Activity
import android.content.Intent
import android.icu.text.NumberFormat
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.models.Venta
import com.miranda.mitiendita360.network.RetrofitClient
import com.miranda.mitiendita360.ui.components.ActionButton2
import com.miranda.mitiendita360.ui.components.DatePickerField2
import com.miranda.mitiendita360.ui.components.SearchTextField
import com.miranda.mitiendita360.ui.components.SearchTextField2
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.GrisClaro2
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface SaleHistoryUiState {
    data class Success(val ventas: List<Venta>) : SaleHistoryUiState
    object Loading : SaleHistoryUiState
    data class Error(val message: String) : SaleHistoryUiState
}
class SaleHistoryActivity : ComponentActivity() {

    private val refreshTrigger = mutableStateOf(0)

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 3. Comprobar si SaleDetailActivity nos dijo que necesita refrescar
        if (result.resultCode == Activity.RESULT_OK && result.data?.getBooleanExtra("NEEDS_REFRESH", false) == true) {
            // 4. Cambiamos el valor del trigger. Esto hará que el LaunchedEffect se vuelva a ejecutar.
            refreshTrigger.value++
        }
    }
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

                    var mostrarAnuladas by remember { mutableStateOf(false) }


                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                    // 3. FUNCIÓN PARA LLAMAR A LA API
                    fun fetchSales(
                        uid: String,
                        query: String,
                        fechaInicio: String,
                        fechaFin: String,
                        anuladas: Boolean
                    ) {
                        if (uid.isBlank()) {
                            uiState = SaleHistoryUiState.Error("Usuario no autenticado.")
                            return
                        }
                        uiState = SaleHistoryUiState.Loading

                        val estadosAFiltrar = if(anuladas) "anulada" else "activa,devuelta"
                        lifecycleScope.launch {
                            uiState = try {
                                val response = RetrofitClient.productoService.getVentas(
                                    userId = uid,
                                    searchTerm = query.ifBlank { null },
                                    fechaInicio = fechaInicio.ifBlank { null },
                                    fechaFin = fechaFin.ifBlank { null },
                                    estados = estadosAFiltrar
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
                    LaunchedEffect(refreshTrigger.value) { // Carga inicial
                        fetchSales(userId, "", "", "", mostrarAnuladas,)
                    }
                    LaunchedEffect(searchQuery, startDate, endDate, mostrarAnuladas) { // Recargar al cambiar filtros
                        fetchSales(userId, searchQuery, startDate, endDate, mostrarAnuladas)
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
                                Box {
                                    Row (
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ){
                                        Icon(
                                            imageVector = Icons.Default.ArrowBackIosNew,
                                            contentDescription = "Icono de reporte",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clickable { finish() }
                                        )
                                    }
                                    Row (
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
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
                                                fontSize = 25.sp
                                            )
                                        }
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
                                .padding(horizontal = 20.dp)
                        ) {
                            val keyboardController = LocalSoftwareKeyboardController.current
                            Row (
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ){
                                SearchTextField2(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = "Buscar por Dni/Producto",
                                    modifier = Modifier.weight(1f),
                                    imeAction = ImeAction.Search,
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            keyboardController?.hide()
                                        }
                                    )

                                )
                                ActionButton2(
                                    onClick = {
                                        mostrarAnuladas = !mostrarAnuladas
                                        keyboardController?.hide()
                                    },
                                    iconResource = R.drawable.devolucion,
                                    backgroundColor =  if (mostrarAnuladas) Fondo1 else Color.White,
                                    iconColor = if (mostrarAnuladas) Color.White else Fondo1,
                                    buttonSize = 50.dp, // Tamaño estándar para un Icon Button
                                    iconSize = 40.dp
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column (modifier = Modifier.weight(1f)){
                                    DatePickerField2(
                                        selectedDate = startDate,
                                        onDateSelected = { startDate = it },
                                        placeholderText = "Desde",
                                        containerColor = Fondo1,     // Fondo blanco
                                        contentColor = VerdeLimon,        // Ícono y placeholder en azul
                                        selectedTextColor = VerdeLimon
                                    )
                                }
                                Column (modifier = Modifier.weight(1f)) {
                                    DatePickerField2(
                                        selectedDate = endDate,
                                        onDateSelected = { endDate = it },
                                        placeholderText = "Hasta", // Nuevo texto
                                        // Texto de fecha seleccionada en gris oscuro
                                        containerColor = VerdeLimon,     // Fondo blanco
                                        contentColor = GrisClaro,        // Ícono y placeholder en azul
                                        selectedTextColor = GrisClaro,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            if (!mostrarAnuladas) {
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
                            }else{
                                Text(
                                    "Ventas Anuladas:",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

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
                                            Text(text = "No se encontraron ventas con los filtros aplicados.", textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center))
                                        } else {
                                            // 1. Agrupamos las ventas por fecha (string "yyyy-MM-dd")
                                            val groupedVentas = state.ventas.groupBy { it.fecha.substring(0, 10) }

                                            // 2. Usamos LazyColumn con stickyHeader
                                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                groupedVentas.forEach { (dateStr, ventasDelDia) ->
                                                    // 3. Encabezado fijo por cada grupo
                                                    stickyHeader {
                                                        DateHeader(dateString = dateStr)
                                                    }
                                                    // 4. Items (tarjetas de venta) para ese grupo
                                                    items(
                                                        items = ventasDelDia,
                                                        key = { "venta-${it.id}" } // Key única para cada venta
                                                    ) { venta ->
                                                        SaleItemCard(
                                                            venta = venta,
                                                            currencyFormat = currencyFormat
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
                    detailLauncher.launch(intent)
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

@Composable
fun DateHeader(dateString: String) {
    // Formateadores para convertir el string de la fecha
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    // Formateador para mostrar "15 de octubre"
    val displayer = SimpleDateFormat("d 'de' MMMM", Locale("es", "ES"))

    val headerText = remember(dateString) {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val saleDate = Calendar.getInstance().apply { time = parser.parse(dateString) ?: Date() }

        when {
            // Comparamos año y día del año
            today.get(Calendar.YEAR) == saleDate.get(Calendar.YEAR) && today.get(Calendar.DAY_OF_YEAR) == saleDate.get(Calendar.DAY_OF_YEAR) -> "Hoy, ${displayer.format(saleDate.time)}"
            yesterday.get(Calendar.YEAR) == saleDate.get(Calendar.YEAR) && yesterday.get(Calendar.DAY_OF_YEAR) == saleDate.get(Calendar.DAY_OF_YEAR) -> "Ayer, ${displayer.format(saleDate.time)}"
            else -> displayer.format(saleDate.time)
        }
    }

    Text(
        text = headerText,
        color = VerdeLimon,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .background(GrisClaro) // Mismo fondo que la pantalla para efecto flotante
            .padding(vertical = 8.dp)
    )
}

