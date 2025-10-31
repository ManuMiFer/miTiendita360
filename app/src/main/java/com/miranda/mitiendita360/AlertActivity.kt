package com.miranda.mitiendita360

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.models.Producto
import com.miranda.mitiendita360.network.AlertasData
import com.miranda.mitiendita360.network.RetrofitClient
import com.miranda.mitiendita360.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AlertActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var alertas by remember { mutableStateOf<AlertasData?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var totalNotificaciones by remember { mutableIntStateOf(0) }
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            LaunchedEffect(key1 = uid) {
                if (uid != null) {
                    try {
                        val response = RetrofitClient.notificacionService.getAlertas(uid)
                        if (response.success && response.data != null) {
                            alertas = response.data
                            totalNotificaciones = response.data.aVencer.size +
                                    response.data.vencidos.size +
                                    response.data.stockBajo.size +
                                    response.data.sinStock.size
                        }
                    } catch (e: Exception) {
                        Log.e("AlertActivity", "Error al cargar alertas: ${e.message}", e)
                    } finally {
                        isLoading = false
                    }
                } else {
                    isLoading = false
                }
            }

            MiTiendita360Theme {
                Scaffold(
                    topBar = {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp, start = 40.dp)
                                .background(color = Fondo1)
                        ) {
                            Box(
                                Modifier
                                    .size(50.dp)
                                    .clickable(onClick = { finish() }),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    imageVector = (Icons.AutoMirrored.Filled.ArrowBackIos),
                                    contentDescription = "",
                                    colorFilter = ColorFilter.tint(color = xd),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .offset(y = 4.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Fondo1)
                            .padding(innerPadding)
                            .padding(25.dp)
                    ) {
                        Text(
                            "Notificaciones ($totalNotificaciones)",
                            color = VerdeLimon,
                            fontSize = 30.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else if (alertas != null) {
                            val currentAlerts = alertas!!

                            LazyColumn(modifier = Modifier.fillMaxWidth()) {

                                // --- SECCIÓN 1: PRODUCTOS A VENCER ---
                                item(key = "header_a_vencer") {
                                    Text(
                                        "Productos a vencerce (${currentAlerts.aVencer.size})",
                                        color = Color.White,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                if (currentAlerts.aVencer.isEmpty()) {
                                    item(key = "felicidades_a_vencer") {
                                        MensajeFelicidades("No hay productos próximos a vencer.")
                                    }
                                } else {
                                    items(currentAlerts.aVencer, key = { "aVencer_${it.id}" }) { producto ->
                                        AlertaItemCard(producto, AlertaTipo.A_VENCER)
                                    }
                                }
                                item(key = "spacer_a_vencer") { Spacer(modifier = Modifier.height(20.dp)) }

                                // --- SECCIÓN 2: PRODUCTOS VENCIDOS ---
                                item(key = "header_vencidos") {
                                    Text(
                                        "Productos que vencieron (${currentAlerts.vencidos.size})",
                                        color = Color.White,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                if (currentAlerts.vencidos.isEmpty()) {
                                    item(key = "felicidades_vencidos") {
                                        MensajeFelicidades("¡Genial! No tienes productos vencidos.")
                                    }
                                } else {
                                    items(currentAlerts.vencidos, key = { "vencido_${it.id}" }) { producto ->
                                        AlertaItemCard(producto, AlertaTipo.VENCIDO)
                                    }
                                }
                                item(key = "spacer_vencidos") { Spacer(modifier = Modifier.height(20.dp)) }

                                // --- SECCIÓN 3: PRODUCTOS A AGOTARSE ---
                                item(key = "header_stock_bajo") {
                                    Text(
                                        "Productos a agotarse (${currentAlerts.stockBajo.size})",
                                        color = Color.White,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                if (currentAlerts.stockBajo.isEmpty()) {
                                    item(key = "felicidades_stock_bajo") {
                                        MensajeFelicidades("Tu inventario está bien abastecido.")
                                    }
                                } else {
                                    items(currentAlerts.stockBajo, key = { "stockBajo_${it.id}" }) { producto ->
                                        AlertaItemCard(producto, AlertaTipo.STOCK_BAJO)
                                    }
                                }
                                item(key = "spacer_stock_bajo") { Spacer(modifier = Modifier.height(20.dp)) }

                                // --- SECCIÓN 4: PRODUCTOS SIN STOCK ---
                                item(key = "header_sin_stock") {
                                    Text(
                                        "Productos sin stock (${currentAlerts.sinStock.size})",
                                        color = Color.White,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                if (currentAlerts.sinStock.isEmpty()) {
                                    item(key = "felicidades_sin_stock") {
                                        MensajeFelicidades("¡Felicidades, no hay productos sin stock!")
                                    }
                                } else {
                                    items(currentAlerts.sinStock, key = { "sinStock_${it.id}" }) { producto ->
                                        AlertaItemCard(producto, AlertaTipo.SIN_STOCK)
                                    }
                                }
                            }
                        } else {
                            // Mensaje si la API falla en general
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(painterResource(id = R.drawable.check), contentDescription = null, tint = VerdeLimon, modifier = Modifier.size(60.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("¡Todo en orden!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Text("No tienes notificaciones por ahora.", color = GrisClaro, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// NUEVO COMPOSABLE REUTILIZABLE para el mensaje de Felicidades
@Composable
fun MensajeFelicidades(texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, color = Verde.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .background(color = Verde.copy(alpha = 0.3f))
            .padding(10.dp)
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center // Centra el texto
    ) {
        Text(
            text = texto,
            color = Verde.copy(alpha = 0.7f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ... (El resto de tu código: AlertaItemCard, AlertaTipo y las funciones de fecha no cambian) ...
enum class AlertaTipo {
    A_VENCER, VENCIDO, STOCK_BAJO, SIN_STOCK
}

@Composable
fun AlertaItemCard(producto: Producto, tipo: AlertaTipo) {
    val context = LocalContext.current
    val borderColor = when(tipo) {
        AlertaTipo.A_VENCER, AlertaTipo.STOCK_BAJO -> Amarillo.copy(alpha = 0.7f)
        AlertaTipo.VENCIDO -> Rojo.copy(alpha = 0.7f)
        AlertaTipo.SIN_STOCK -> Rojo.copy(alpha = 0.7f) // Para productos sin stock
    }
    val backgroundColor = when(tipo) {
        AlertaTipo.A_VENCER, AlertaTipo.STOCK_BAJO -> Amarillo.copy(alpha = 0.3f)
        AlertaTipo.VENCIDO -> Rojo.copy(alpha = 0.3f)
        AlertaTipo.SIN_STOCK -> Rojo.copy(alpha = 0.3f) // Para productos sin stock
    }
    val textColor = when(tipo) {
        AlertaTipo.A_VENCER, AlertaTipo.STOCK_BAJO -> Amarillo.copy(alpha = 0.7f)
        AlertaTipo.VENCIDO -> Rojo.copy(alpha = 0.7f)
        AlertaTipo.SIN_STOCK -> Rojo.copy(alpha = 0.7f) // Para productos sin stock
    }

    val (linea1, linea2) = when (tipo) {
        AlertaTipo.A_VENCER -> {
            val dias = diasHastaVencimiento(producto.fechaCad ?: "")
            Pair("Le quedan $dias dias para vencer", "Fecha de vencimiento: ${formatearFecha(producto.fechaCad)}")
        }
        AlertaTipo.VENCIDO -> {
            val dias = diasDesdeVencimiento(producto.fechaCad ?: "") * -1
            Pair("Vencio hace $dias dias", "Fecha de vencimiento: ${formatearFecha(producto.fechaCad)}")
        }
        AlertaTipo.STOCK_BAJO -> {
            Pair("Ya solo quedan ${producto.stockActual} unidades", "Stock minimo: ${producto.stockMinimo}")
        }
        AlertaTipo.SIN_STOCK -> {
            Pair("Este producto no tiene stock", "Stock actual: 0")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, color = borderColor, RoundedCornerShape(12.dp))
            .background(color = backgroundColor)
            .padding(10.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("${BuildConfig.API_BASE_URL}imagenes/${producto.imagen}")
                .placeholder(R.drawable.image)
                .error(R.drawable.image)
                .crossfade(true)
                .build(),
            contentDescription = "Imagen del producto",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                producto.nombre,
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                linea1,
                color = textColor,
                fontSize = 13.sp,
            )
            Text(
                linea2,
                color = textColor,
                fontSize = 13.sp,
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// --- Funciones auxiliares para calcular fechas ---
fun diasHastaVencimiento(fechaCad: String): Long {
    if (fechaCad.isBlank()) return 0
    return try {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val fechaVencimiento = formato.parse(fechaCad) ?: return 0
        val hoy = Date()
        val diff = fechaVencimiento.time - hoy.time
        if (diff < 0) return 0
        TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1
    } catch (e: Exception) {
        0
    }
}

fun diasDesdeVencimiento(fechaCad: String): Long {
    if (fechaCad.isBlank()) return 0
    return try {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val fechaVencimiento = formato.parse(fechaCad) ?: return 0
        val calHoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val hoy = calHoy.time
        val diff = hoy.time - fechaVencimiento.time
        if (diff < 0) return 0
        TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
    } catch (e: Exception) {
        0
    }
}

fun formatearFecha(fecha: String?): String {
    if (fecha.isNullOrBlank()) return "N/A"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        formatter.format(parser.parse(fecha)!!)
    } catch (e: Exception) {
        fecha
    }
}

