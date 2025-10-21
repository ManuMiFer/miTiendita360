package com.miranda.mitiendita360

import BarcodeAnalyzer
import ScannerView
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.FinishComposingTextCommand
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.models.DetalleVentaRequest
import com.miranda.mitiendita360.models.PagoRequest
import com.miranda.mitiendita360.models.Producto
import com.miranda.mitiendita360.models.VentaRequest
import com.miranda.mitiendita360.network.RetrofitClient
import com.miranda.mitiendita360.ui.components.ActionButton
import com.miranda.mitiendita360.ui.components.BotonChevere
import com.miranda.mitiendita360.ui.theme.Celeste
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.Rojito
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.concurrent.Executors

data class CartItem(
    val producto: Producto,
    var quantity: Int
)
class SaleInsertActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {


            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            // --- 1. ESTADOS PRINCIPALES ---
            val productosEnVenta = remember { mutableStateListOf<CartItem>() }
            var showScanner by remember { mutableStateOf(false) }


            val searchProductLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Usamos getParcelableArrayListExtra para recibir la lista
                    val selectedProducts = result.data?.getParcelableArrayListExtra<Producto>("SELECTED_PRODUCTS")

                    if (!selectedProducts.isNullOrEmpty()) {
                        // --- LÓGICA SÚPER SIMPLIFICADA ---
                        selectedProducts.forEach { producto ->
                            // Reutilizamos la lógica de añadir al carrito que ya tienes
                            val existingItemIndex = productosEnVenta.indexOfFirst { it.producto.id == producto.id }
                            if (existingItemIndex != -1) {
                                // Si ya existe, incrementa la cantidad
                                val currentItem = productosEnVenta[existingItemIndex]
                                productosEnVenta[existingItemIndex] = currentItem.copy(quantity = currentItem.quantity + 1)
                            } else {
                                // Si es nuevo, añádelo
                                productosEnVenta.add(CartItem(producto = producto, quantity = 1))
                            }
                        }
                        Toast.makeText(context, "${selectedProducts.size} producto(s) añadido(s).", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // --- 2. COMPONENTES DE LA CÁMARA (INICIALIZADOS UNA SOLA VEZ) ---
            val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
            val cameraProvider = remember { cameraProviderFuture.get() }
            val previewView = remember { PreviewView(context) }
            val executor = remember { Executors.newSingleThreadExecutor() }
            val barcodeAnalyzer = remember {
                BarcodeAnalyzer { barcode ->
                    showScanner = false

                    if (barcode.isNotBlank()) {
                        scope.launch {
                            try {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId == null) {
                                    Toast.makeText(context, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                val response = RetrofitClient.productoService.getProductByBarcode(barcode, userId)
                                if (response.success && response.product != null) {
                                    // <-- CAMBIO 3: Lógica para añadir/actualizar en el carrito
                                    val existingItemIndex = productosEnVenta.indexOfFirst { it.producto.id == response.product.id }
                                    if (existingItemIndex != -1) {
                                        // Si el producto ya existe, actualiza su cantidad
                                        val currentItem = productosEnVenta[existingItemIndex]
                                        productosEnVenta[existingItemIndex] = currentItem.copy(quantity = currentItem.quantity + 1)
                                    } else {
                                        // Si es nuevo, añádelo con cantidad 1
                                        productosEnVenta.add(CartItem(producto = response.product, quantity = 1))
                                    }
                                    Toast.makeText(context, "${response.product.nombre} añadido", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, response.message ?: "Producto no encontrado", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("API_CALL", "Error al buscar producto: ", e)
                                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            val imageAnalyzer = remember {
                ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(executor, barcodeAnalyzer) }
            }

            // --- 3. GESTIÓN DE PERMISOS ---
            val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

            MiTiendita360Theme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
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
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.padding(5.dp))
                                Column {
                                    Text(
                                        "Registrar",
                                        color = Color.White,
                                        fontSize = 25.sp
                                    )
                                    Text(
                                        "Nueva Venta",
                                        color = Color.White,
                                        fontSize = 25.sp
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Column (
                        Modifier
                            .fillMaxSize()
                            .background(color = Color.White)
                            .padding(innerPadding),
                    ) {
                        Column(
                            Modifier
                                .clip(
                                    shape = WideOvalBottomShape(
                                        arcHeight = 200f, // Profundidad de la curva
                                        horizontalControlOffset = 180f
                                    )
                                )
                                .background(color = Fondo1)
                        ) {
                            Row (
                                Modifier
                                    .background(color = Fondo1)
                                    .padding(horizontal = 25.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween

                            ){
                                Text(
                                    "Productos",
                                    color = Color.White,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row (
                                ){
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(shape = CircleShape)
                                            .clickable {
                                                if (cameraPermissionState.status.isGranted) {
                                                    barcodeAnalyzer.startScanning() // Resetea el analizador
                                                    showScanner = true // Muestra el diálogo
                                                } else {
                                                    cameraPermissionState.launchPermissionRequest()
                                                }
                                            }
                                            .background(color = Color.White)
                                            .padding(7.dp)

                                    ){
                                        Image(
                                            painterResource(R.drawable.barcodescan),
                                            contentDescription = "",
                                        )
                                    }
                                    Spacer(modifier = Modifier.padding(5.dp))
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(shape = CircleShape)
                                            .clickable {
                                                val intent = Intent(
                                                    context,
                                                    SearchProductActivity::class.java
                                                )
                                                searchProductLauncher.launch(intent)
                                            }
                                            .background(color = Celeste)
                                            .padding(5.dp)
                                    ){
                                        Image(
                                            imageVector = (Icons.Default.Search),
                                            contentDescription = "",
                                            colorFilter = ColorFilter.tint(color = Color.White),
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.padding(10.dp))
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(15.dp),
                                modifier = Modifier
                                    .height(330.dp)
                                    .padding(horizontal = 25.dp)
                            ) {
                                // --- USA LA LISTA DINÁMICA ---
                                items(
                                    items = productosEnVenta,
                                    key = { it.producto.id!! } // La clave sigue siendo el ID del producto
                                ) { cartItem -> // La variable ahora es 'cartItem'
                                    Row(
                                        Modifier
                                            .clip(shape = RoundedCornerShape(30.dp))
                                            .fillMaxWidth()
                                            .background(color = GrisClaro)
                                            .padding(15.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = "https://manuelmirandafernandez.com/imagenes/${cartItem.producto.imagen}", // Usa cartItem
                                            contentDescription = cartItem.producto.nombre,
                                            modifier = Modifier
                                                .size(75.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.padding(6.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                cartItem.producto.nombre, // Usa cartItem
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Text(
                                                "S/ ${"%.2f".format(cartItem.producto.precioVenta)}", // Usa cartItem
                                                color = VerdeLimon,
                                                fontSize = 18.sp,
                                            )

                                            // <-- CAMBIO 5: El estado de la cantidad ahora vive fuera
                                            val subtotal = cartItem.quantity * cartItem.producto.precioVenta

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TextFielCantidad(
                                                    quantity = cartItem.quantity, // Usa la cantidad del cartItem
                                                    onQuantityChange = { newQuantity ->
                                                        // Actualiza la cantidad directamente en el objeto de la lista
                                                        val index = productosEnVenta.indexOf(cartItem)
                                                        if (index != -1) {
                                                            productosEnVenta[index] = cartItem.copy(quantity = newQuantity)
                                                        }
                                                    }
                                                )
                                                Text(
                                                    text = "S/ ${"%.2f".format(subtotal)}",
                                                    color = Color.White,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.padding(10.dp))
                            Box(modifier = Modifier.padding(horizontal = 25.dp)) {
                                HorizontalDivider(
                                    thickness = 1.dp,        // Grosor de la línea
                                    color = GrisClaro        // Color de la línea, puedes usar el que prefieras
                                )
                            }
                            Spacer(modifier = Modifier.padding(10.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 25.dp),
                                horizontalAlignment = Alignment.End // Alinea los textos a la derecha
                            ) {
                                val totalVenta = productosEnVenta.sumOf { it.producto.precioVenta * it.quantity }
                                Row (
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ){
                                    Text(
                                        "Subtotal:",
                                        color = Color.White,
                                        fontSize = 18.sp)
                                    Text(
                                        "S/ ${"%.2f".format(totalVenta)}", // Usa la misma variable
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                }
                                Row (
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ){
                                    Text(
                                        "Descuento:",
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        "S/ 0.00",
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                }
                                Row (
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ){
                                    Text(
                                        "TOTAL:",
                                        color = VerdeLimon,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "S/ ${"%.2f".format(totalVenta)}",
                                        color = VerdeLimon,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.padding(40.dp))
                            }
                        }

                        Column (
                            modifier = Modifier.padding(horizontal = 25.dp)
                        ){
                            BotonChevere(
                                texto = "Cancelar",
                                colorFondo = Rojito,
                                colorTexto = Color.White
                            ) {
                                finish()
                            }
                            Spacer(modifier = Modifier.padding(5.dp))
                            BotonChevere(
                                texto = "Guardar Venta",
                                colorFondo = VerdeLimon,
                                colorTexto = GrisClaro
                            ) {
                                // --- INICIO DE LA LÓGICA DE GUARDADO ---
                                scope.launch {

                                    // Validar que el carrito no esté vacío
                                    if (productosEnVenta.isEmpty()) {
                                        Toast.makeText(context, "El carrito está vacío", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }

                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    if (userId == null) {
                                        Toast.makeText(context, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }

                                    // 1. Construir la lista de detalles
                                    val detalles = productosEnVenta.map { cartItem ->
                                        DetalleVentaRequest(
                                            idProducto = cartItem.producto.id!!,
                                            precioVenta = cartItem.producto.precioVenta,
                                            cantidad = cartItem.quantity,
                                            subtotal = cartItem.quantity * cartItem.producto.precioVenta
                                        )
                                    }
                                    val totalVenta = detalles.sumOf { it.subtotal }
                                    val dbFormatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()) // <-- FORMATO DE FECHA
                                    val fechaHoraParaEnviar: String = dbFormatter.format(java.util.Date()) // <-- VARIABLE CON FECHA Y HORA

                                    val pagos = listOf(
                                        PagoRequest(metodoPago = "Efectivo", monto = totalVenta)
                                    )


                                    val ventaRequest = VentaRequest(
                                        montoTotal = totalVenta,
                                        idUsuario = userId,
                                        idCliente = null,
                                        detalles = detalles,
                                        pagos = pagos,
                                        fechaHora = fechaHoraParaEnviar // <-- ENVÍA LA FECHA Y HORA
                                    )

                                    try {
                                        val response = RetrofitClient.productoService.registrarVenta(ventaRequest)
                                        if (response.success) {
                                            Toast.makeText(context, response.message, Toast.LENGTH_LONG).show()
                                            finish()
                                        } else {
                                            Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        var errorMessage = "Error desconocido."
                                        if (e is HttpException) {
                                            try {
                                                // INTENTAMOS LEER EL CUERPO DEL ERROR, AQUÍ ESTÁ LA CLAVE
                                                val errorBody = e.response()?.errorBody()?.string()

                                                if (!errorBody.isNullOrEmpty()) {
                                                    // Si el cuerpo del error no está vacío, ese es nuestro mensaje
                                                    errorMessage = errorBody
                                                } else {
                                                    // Si el cuerpo del error está vacío, mostramos el código y mensaje HTTP
                                                    errorMessage = "Error HTTP ${e.code()}: ${e.message()}"
                                                }
                                            } catch (jsonError: Exception) {
                                                // Si hay un error al leer el cuerpo del error
                                                errorMessage = "Error al leer la respuesta del servidor."
                                            }
                                        } else {
                                            // Para otros tipos de errores (ej. sin conexión a internet)
                                            errorMessage = e.message ?: "No se pudo conectar al servidor."
                                        }

                                        // Mostramos el mensaje de error DETALLADO en el Log y en un Toast largo
                                        Log.e("REGISTRAR_VENTA", "FALLO CRÍTICO EN LA API: $errorMessage")
                                        // Usamos un AlertDialog para asegurarnos de que el mensaje se pueda leer completo
                                        AlertDialog.Builder(context)
                                            .setTitle("Error Crítico de la API")
                                            .setMessage(errorMessage)
                                            .setPositiveButton("Entendido") { dialog, _ -> dialog.dismiss() }
                                            .show()
                                    }
                                }
                            }
                        }

                    }
                    if (showScanner) {
                        Dialog(
                            onDismissRequest = { showScanner = false },
                            // --- AÑADE ESTAS PROPIEDADES ---
                            properties = DialogProperties(
                                usePlatformDefaultWidth = false, // Desactiva el ancho por defecto de la plataforma
                                decorFitsSystemWindows = false // Permite que el diálogo se dibuje sobre las barras del sistema
                            )
                        ) {
                            // Envolvemos el ScannerView en un Box para que ocupe toda la superficie
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black) // Fondo negro para evitar transparencias
                            ) {
                                // Usamos el ScannerView que ya creaste
                                ScannerView(
                                    onBarcodeScanned = { barcode ->
                                        // La lógica ya se maneja en la inicialización del BarcodeAnalyzer
                                    },
                                    cameraProvider = cameraProvider,
                                    previewView = previewView,
                                    imageAnalyzer = imageAnalyzer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun TextFielCantidad(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- BOTÓN DE DECREMENTAR ---
        ActionButton(
            icon = Icons.Default.Remove,
            iconColor = Color.White,
            backgroundColor = Color.Red,
            buttonSize = 20.dp,
            onClick = {
                // Decrementa solo si es mayor que 1
                if (quantity > 1) {
                    onQuantityChange(quantity - 1)
                }
            },
        )

        // --- CAMPO DE TEXTO NUMÉRICO ---
        BasicTextField(
            // El 'value' ahora debe manejar el caso de que la cantidad sea 0
            value = if (quantity == 0) "" else quantity.toString(),
            onValueChange = { newValue ->
                // --- LÓGICA CORREGIDA ---
                if (newValue.isEmpty()) {
                    // Si el usuario borra todo, actualizamos el estado a 0
                    onQuantityChange(0)
                } else {
                    // Si no está vacío, intentamos convertirlo a número
                    val newQuantity = newValue.toIntOrNull()
                    if (newQuantity != null && newQuantity <= 100) {
                        // Solo actualizamos si es un número válido y menor o igual a 100
                        onQuantityChange(newQuantity)
                    }
                    // Si no es un número válido (ej: "abc") o es mayor a 100, no hacemos nada.
                }
            },
            // Estilos y configuración
            modifier = Modifier
                .width(50.dp) // Ancho justo para hasta 3 dígitos
                .background(
                    color = Fondo1, // Un fondo que se integre
                    shape = RoundedCornerShape(8.dp)
                ),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            maxLines = 1
        )

        // --- BOTÓN DE INCREMENTAR ---
        ActionButton(
            icon = Icons.Default.Add,
            iconColor = Color.White,
            backgroundColor = Color.Green,
            buttonSize = 20.dp,
            onClick = {
                // Incrementa solo si es menor que 100
                if (quantity < 100) {
                    onQuantityChange(quantity + 1)
                }
            }
        )
    }
}
