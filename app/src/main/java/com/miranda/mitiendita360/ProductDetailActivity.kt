package com.miranda.mitiendita360

import android.app.Activity
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope

import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.models.Producto
import com.miranda.mitiendita360.network.RetrofitClient
import com.miranda.mitiendita360.ui.components.BotonChevere
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import kotlinx.coroutines.launch
import java.text.NumberFormat

sealed interface ProductDetailUiState {
    data class Success(val product: Producto) : ProductDetailUiState
    object Loading : ProductDetailUiState
    data class Error(val message: String) : ProductDetailUiState
}
class ProductDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        enableEdgeToEdge()
        setContent {
            MiTiendita360Theme {
                var uiState by remember { mutableStateOf<ProductDetailUiState>(ProductDetailUiState.Loading) }

                // 4. LLAMADA A LA API AL INICIAR
                LaunchedEffect(key1 = productId) {
                    if (productId != -1 && userId.isNotBlank()) {
                        lifecycleScope.launch {
                            uiState = try {
                                // REUTILIZAMOS el endpoint getProducts, pasándole el ID
                                val response = RetrofitClient.productoService.getProducts(
                                    userId = userId,
                                    searchTerm = null,
                                    categoryId = null,
                                    estado = null
                                )
                                // Como devuelve una lista, tomamos el primer (y único) elemento
                                if (response.success && response.data.isNotEmpty()) {
                                    ProductDetailUiState.Success(response.data.first())
                                } else {
                                    ProductDetailUiState.Error(response.message ?: "Producto no encontrado")
                                }
                            } catch (e: Exception) {
                                Log.e("ProductDetail", "Error al cargar el producto", e)
                                ProductDetailUiState.Error("Error de conexión: ${e.message}")
                            }
                        }
                    } else {
                        uiState = ProductDetailUiState.Error("ID de producto o usuario inválido")
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
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
                                    painterResource(R.drawable.tienda),
                                    contentDescription = "",
                                    colorFilter = ColorFilter.tint(color = VerdeLimon),
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.padding(5.dp))
                                Column {
                                    Text(
                                        "Detalle",
                                        color = Color.White,
                                        fontSize = 25.sp
                                    )
                                    Text(
                                        "Producto",
                                        color = Color.White,
                                        fontSize = 25.sp
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color.White)
                    ) {
                        when (val state = uiState) {
                            is ProductDetailUiState.Loading -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                            is ProductDetailUiState.Error -> {
                                Text(
                                    text = state.message,
                                    color = Color.Red,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                )
                            }
                            is ProductDetailUiState.Success -> {
                                // Cuando los datos cargan, se muestra el contenido
                                ProductDetailContent(product = state.product) {
                                    setResult(Activity.RESULT_CANCELED) // El usuario solo regresa
                                    finish()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ProductDetailContent(product: Producto, onRegresarClick: () -> Unit) {
    // Formateador de moneda para Soles Peruanos
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(java.util.Locale("es", "PE")) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(shape = WideOvalBottomShape(arcHeight = 200f, horizontalControlOffset = 180f))
                .fillMaxWidth()
                .background(color = Fondo1)
        ) {
            AsyncImage(
                model = "https://manuelmirandafernandez.com/imagenes/${product.imagen}", // <-- CORREGIDO: Añadido "https://"
                contentDescription = "Imagen de ${product.nombre}", // <-- MEJORADO: Descripción más útil
                placeholder = painterResource(id = R.drawable.box), // <-- BUENA PRÁCTICA: Muestra algo mientras carga
                error = painterResource(id = R.drawable.box),
                contentScale = ContentScale.Crop,// <-- BUENA PRÁCTICA: Muestra algo si falla la carga
                modifier = Modifier
                    .size(150.dp) // 1. Mantiene el tamaño cuadrado
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                text = product.nombre ?: "Nombre no disponible",
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.padding(40.dp))
        }

        // Usamos un LazyColumn por si el contenido es muy largo en pantallas pequeñas
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(25.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp), // Ocupa el espacio disponible
                ) {
                    Column(modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            DetailItem(label = "Stock Actual:", value = product.stockActual?.toString() ?: "N/A")
                            DetailItem(label = "Stock Mínimo:", value = product.stockMinimo?.toString() ?: "N/A")
                        }
                        DetailItem(label = "Categoría:", value = product.idCategoria?.toString() ?: "No especificada")
                        DetailItem(label = "Proveedor:", value = product.idProveedor ?: "No especificado")
                        DetailItem(label = "Marca:", value = product.marca ?: "No especificada")

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            DetailItem(label = "Precio V:", value = currencyFormat.format(product.precioVenta ?: 0.0))
                            DetailItem(label = "Precio C:", value = currencyFormat.format(product.precioCompra ?: 0.0))
                        }
                        DetailItem(label = "Fecha Cad.:", value = product.fechaCad ?: "No aplica")
                        DetailItem(label = "Estado:", value = if (product.estado == 1) "Activo" else "Inactivo")
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    BotonChevere(
                        texto = "Regresar",
                        colorFondo = GrisClaro,
                        colorTexto = Color.White,
                        onClick = onRegresarClick
                    )
                }
            }
        }
    }
}
@Composable
fun DetailItem(label: String, value: String) {
    Row {
        Text(
            text = label,
            color = Fondo1,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.padding(5.dp))
        Text(
            text = value,
            color = GrisClaro,
            fontSize = 18.sp
        )
    }
}