package com.miranda.mitiendita360

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.gson.annotations.SerializedName
import com.miranda.mitiendita360.network.RetrofitClient
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.GrisClaro2
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import kotlin.text.format

data class SaleProductDetail(
    val nombre: String,
    val cantidad: Int,
    @SerializedName("precio_unitario")
    val precioUnitario: Double,
    val subtotal: Double
)

data class SalePaymentDetail(
    val metodoPago: String,
    val monto: Double
)

data class SaleDetail(
    val id: Int,
    val fechaHora: String,
    val montoTotal: Double,
    val productos: List<SaleProductDetail>,
    val pagos: List<SalePaymentDetail>
)

data class SaleDetailResponse(
    val success: Boolean,
    val message: String?,
    val data: SaleDetail?
)

// --- 2. ESTADOS DE LA UI ---
sealed interface SaleDetailUiState {
    data class Success(val saleDetail: SaleDetail) : SaleDetailUiState
    object Loading : SaleDetailUiState
    data class Error(val message: String) : SaleDetailUiState
}
class SaleDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val saleId = intent.getIntExtra("SALE_ID", -1)
        setContent {
            MiTiendita360Theme {
                var uiState by remember { mutableStateOf<SaleDetailUiState>(SaleDetailUiState.Loading) }

                // --- 2. LLAMAR A LA API AL INICIAR ---
                LaunchedEffect(Unit) {
                    if (saleId == -1) {
                        uiState = SaleDetailUiState.Error("ID de venta inválido.")
                        return@LaunchedEffect
                    }
                    lifecycleScope.launch {
                        uiState = try {
                            val response = RetrofitClient.productoService.getVentaDetalle(saleId)
                            if (response.success && response.data != null) {
                                SaleDetailUiState.Success(response.data)
                            } else {
                                SaleDetailUiState.Error(response.message ?: "Error al obtener los datos.")
                            }
                        } catch (e: Exception) {
                            Log.e("SaleDetailActivity", "Error al cargar detalle", e)
                            SaleDetailUiState.Error("Error de conexión: ${e.message}")
                        }
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize(),
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
                                        "Detalle de",
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GrisClaro)
                            .padding(innerPadding)
                            ,
                        contentAlignment = Alignment.Center
                    ) {
                        // --- 4. MANEJO DE ESTADOS DE LA UI ---
                        when (uiState) {
                            is SaleDetailUiState.Loading -> CircularProgressIndicator()
                            is SaleDetailUiState.Error -> Text(
                                text = (uiState as SaleDetailUiState.Error).message,
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                            is SaleDetailUiState.Success -> {
                                // Mostramos el contenido real cuando los datos se cargan
                                SaleDetailContent(sale = (uiState as SaleDetailUiState.Success).saleDetail)
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun SaleDetailContent(sale: SaleDetail) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(java.util.Locale("es", "PE")) }
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
    val outputDateFormat = SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    val outputTimeFormat = SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
    val dateObject = try { inputFormat.parse(sale.fechaHora) } catch (e: Exception) { null }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECCIÓN TOTAL Y RESUMEN ---
        item (){
            Column (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally

            ){
                Text(
                    text = "Total: ${currencyFormat.format(sale.montoTotal)}",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Fecha: ${dateObject?.let { outputDateFormat.format(it) } ?: "N/A"}", fontSize = 20.sp, color = GrisClaro2)
                        Text("Hora: ${dateObject?.let { outputTimeFormat.format(it) } ?: "N/A"}", fontSize = 20.sp, color = GrisClaro2)
                    }
                    Text("ID: ${sale.id}", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }

        // --- SECCIÓN PRODUCTOS ---
        item {
            Text("Productos:", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VerdeLimon)
        }
        items(sale.productos) { producto ->
            ProductDetailItem(producto, currencyFormat)
        }

        // --- SECCIÓN PAGOS ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Pagos", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VerdeLimon)
            HorizontalDivider(thickness = 1.dp, color = GrisClaro, modifier = Modifier.padding(top=4.dp))
            Box() {
                HorizontalDivider(
                    thickness = 1.dp,        // Grosor de la línea
                    color = Color.White        // Color de la línea, puedes usar el que prefieras
                )
            }
        }
        items(sale.pagos) { pago ->
            PaymentDetailItem(pago, currencyFormat)
        }
    }
}
@Composable
fun ProductDetailItem(producto: SaleProductDetail, currencyFormat: NumberFormat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(13.dp))
            .background(color = Fondo1)
            .padding( 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(producto.nombre, fontWeight = FontWeight.SemiBold, fontSize = 25.sp,
                color = Color.White)
            Text(
                "${producto.cantidad} x ${currencyFormat.format(producto.precioUnitario)}",
                color = Color.White,
                fontSize = 20.sp
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(currencyFormat.format(producto.subtotal), fontWeight = FontWeight.Bold, fontSize = 40.sp, color = VerdeLimon)

    }
}

@Composable
fun PaymentDetailItem(pago: SalePaymentDetail, currencyFormat: NumberFormat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(pago.metodoPago.replaceFirstChar { it.titlecase(java.util.Locale.getDefault()) }, fontSize = 20.sp, color = Color.White)
        Text(currencyFormat.format(pago.monto), fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
            color = Color.White)
    }
}


