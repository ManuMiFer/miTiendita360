package com.miranda.mitiendita360

import BarcodeAnalyzer
import ScannerView
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.http.HttpException
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.miranda.mitiendita360.models.Producto
import com.miranda.mitiendita360.network.RetrofitClient
import com.miranda.mitiendita360.ui.components.BotonChevere
import com.miranda.mitiendita360.ui.components.DropdownChevereBasico
import com.miranda.mitiendita360.ui.components.TextFieldChevereBasico
import com.miranda.mitiendita360.ui.theme.Celeste
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.GrisClaro2
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.Rojito
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import com.miranda.mitiendita360.ui.theme.Verdecito
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import kotlin.collections.subtract
import kotlin.text.compareTo
import kotlin.text.format
import kotlin.text.sumOf

class SaleReturnActivity : ComponentActivity() {
    private var saleDetail: SaleDetail? = null

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        saleDetail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("SALE_DETAIL_EXTRA", SaleDetail::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("SALE_DETAIL_EXTRA")
        }

        setContent {
            var mostrarDialogo by remember { mutableStateOf(false) }
            var productosEnVenta = remember { mutableStateListOf<CartItem>() }
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var showScanner by remember { mutableStateOf(false) }




            // ... (El bloque de 'productosOriginalesComoCartItems' está bien)

            // 2. Inicializamos la lista que se va a mostrar con los productos originales.

            val productosOriginalesComoCartItems = remember(saleDetail) {
                saleDetail?.productos?.map { productoDetalle ->
                    // Creamos un objeto Producto temporal para el CartItem.
                    // Nota: Los campos que no vienen en SaleProductDetail (como stock, id) se ponen con valores por defecto.
                    val producto = Producto(
                        id = productoDetalle.id, // Suponiendo que ya modificaste SaleProductDetail para incluir 'id'
                        nombre = productoDetalle.nombre,
                        precioVenta = productoDetalle.precioUnitario,
                        stockActual = Int.MAX_VALUE, // Usamos la cantidad vendida como stock disponible para devolver.
                        imagen = productoDetalle.imagen ?: "", // Suponiendo que ya modificaste SaleProductDetail para incluir 'imagen'
                        idCategoria = 0, // Campo que falta, lo ponemos en 0,
                        codBarras = "",
                        fechaCad = "",
                        idProveedor = "",
                        idUsuario = "",
                        marca = "",
                        precioCompra = 0.0,
                        stockMinimo = 0,


                    )
                    CartItem(
                        producto = producto,
                        quantity = productoDetalle.cantidad
                    )
                }?.toMutableList() ?: mutableListOf()
            }

            val productosParaDevolucion = remember {
                mutableStateListOf<CartItem>().apply {
                    addAll(productosOriginalesComoCartItems)
                }
            }

            val montoTotalOriginal = saleDetail?.montoTotal ?: 0.0
            val totalVenta = productosParaDevolucion.sumOf { it.producto.precioVenta * it.quantity }
            val diferencia = montoTotalOriginal - totalVenta

            val searchProductLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    val selectedProducts = result.data?.getParcelableArrayListExtra<Producto>("SELECTED_PRODUCTS")

                    if (!selectedProducts.isNullOrEmpty()) {
                        // --- LÓGICA SÚPER SIMPLIFICADA ---
                        selectedProducts.forEach { producto ->
                            // Reutilizamos la lógica de añadir al carrito que ya tienes
                            val existingItemIndex = productosParaDevolucion.indexOfFirst { it.producto.id == producto.id }
                            if (existingItemIndex != -1) {
                                // Si ya existe, incrementa la cantidad
                                val currentItem = productosParaDevolucion[existingItemIndex]
                                productosParaDevolucion[existingItemIndex] = currentItem.copy(quantity = currentItem.quantity + 1)
                            } else {
                                // Si es nuevo, añádelo
                                productosParaDevolucion.add(CartItem(producto = producto, quantity = 1))
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
                                    val existingItemIndex = productosParaDevolucion.indexOfFirst { it.producto.id == response.product.id }
                                    if (existingItemIndex != -1) {
                                        // Si el producto ya existe, actualiza su cantidad
                                        val currentItem = productosParaDevolucion[existingItemIndex]
                                        productosParaDevolucion[existingItemIndex] = currentItem.copy(quantity = currentItem.quantity + 1)
                                    } else {
                                        // Si es nuevo, añádelo con cantidad 1
                                        productosParaDevolucion.add(CartItem(producto = response.product, quantity = 1))
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
            val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

            val ListaMetodos = listOf("Efectivo","Yape", "Multiple")
            var mostrarDialogoPago by remember { mutableStateOf(false) }
            var metodoSelecionado by remember { mutableStateOf(ListaMetodos.first()) }
            var yape by remember { mutableStateOf("") }
            var efectivo by remember { mutableStateOf("") }
            var ultimoCampoEditado by remember { mutableStateOf("") }
            val montoAbsolutoDiferencia = kotlin.math.abs(diferencia)
            LaunchedEffect(yape, efectivo) {
                if (metodoSelecionado == "Multiple") {
                    val yapeDouble = yape.toDoubleOrNull() ?: 0.0
                    val efectivoDouble = efectivo.toDoubleOrNull() ?: 0.0

                    // --- CORRECCIÓN CLAVE: Usar totalVenta, no la diferencia ---
                    if (ultimoCampoEditado == "yape") {
                        if (yapeDouble <= totalVenta) {
                            val restante = totalVenta - yapeDouble
                            efectivo = String.format("%.2f", restante)
                        }
                    } else if (ultimoCampoEditado == "efectivo") {
                        if (efectivoDouble <= totalVenta) {
                            val restante = totalVenta - efectivoDouble
                            yape = String.format("%.2f", restante)
                        }
                    }
                }
            }

            LaunchedEffect(metodoSelecionado) {
                if (metodoSelecionado != "Multiple") {
                    yape = ""
                    efectivo = ""
                    ultimoCampoEditado = ""
                }
            }
            MiTiendita360Theme {

                Scaffold(
                    topBar = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(color = Fondo1)

                        ) {
                            Spacer(modifier = Modifier.padding(10.dp))
                            Box {
                                Row (
                                    Modifier
                                        .padding(horizontal = 20.dp)
                                ){
                                    Icon(
                                        imageVector = (Icons.Default.ArrowBackIosNew),
                                        contentDescription = "",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(35.dp)
                                            .clickable {
                                                finish()
                                            }
                                    )
                                }
                                Row (
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Image(
                                        painterResource(R.drawable.devolucion),
                                        contentDescription = "",
                                        colorFilter = ColorFilter.tint(color = Color.White),
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(5.dp))
                                    Column {
                                        Text(
                                            "Procesar",
                                            color = Color.White,
                                            fontSize = 25.sp
                                        )
                                        Text(
                                            "Devolucion",
                                            color = Color.White,
                                            fontSize = 25.sp
                                        )
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    if (saleDetail == null) {
                        // Si por alguna razón no llegan los datos, mostramos un error
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("Error: No se pudo cargar la información de la venta.", color = Color.Red)
                        }
                    } else {
                        // Si tenemos los datos, mostramos la UI principal
                        val sale = saleDetail!!
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(Fondo1)
                                .padding(horizontal = 25.dp)
                        ) {
                            Text("Cliente: ${sale.cliente}", color = GrisClaro2, fontSize = 20.sp)
                            val inputFormat =
                                SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm:ss",
                                    java.util.Locale.getDefault()
                                )
                            val outputDateFormat =
                                SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                            val dateObject = try {
                                inputFormat.parse(sale.fechaHora)
                            } catch (e: Exception) {
                                null
                            }
                            val fechaFormateada =
                                dateObject?.let { outputDateFormat.format(it) } ?: "N/A"
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(fechaFormateada, color = GrisClaro2, fontSize = 20.sp)
                                Text("ID Venta: ${sale.id}", color = GrisClaro2, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.padding(5.dp))
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = GrisClaro
                            )
                            Spacer(modifier = Modifier.padding(5.dp))
                            Row(
                                Modifier
                                    .background(color = Fondo1)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween

                            ) {
                                Text(
                                    "Productos",
                                    color = Color.White,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                ) {
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

                                    ) {
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
                                                val excludedProductIds =
                                                    productosParaDevolucion.map { it.producto.id.toString() }
                                                intent.putStringArrayListExtra(
                                                    "EXCLUDED_IDS",
                                                    ArrayList(excludedProductIds)
                                                )

                                                searchProductLauncher.launch(intent)
                                            }
                                            .background(color = Celeste)
                                            .padding(5.dp)
                                    ) {
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
                            ) {
                                items(
                                    items = productosParaDevolucion, // ¡AQUÍ!
                                    key = { it.producto.nombre } // Usar una clave única
                                ) { cartItem ->

                                    // --- INICIO DE LA MODIFICACIÓN ---

                                    // 1. Estado para controlar el deslizamiento
                                    val dismissState =
                                        rememberSwipeToDismissBoxState(
                                            // Confirmamos la acción de deslizar
                                            confirmValueChange = { dismissValue ->
                                                // Si el deslizamiento fue hacia el final (derecha)
                                                if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                                    // Eliminamos el producto de la lista
                                                    productosParaDevolucion.remove(cartItem)
                                                    // Devolvemos 'true' para confirmar que el elemento fue eliminado
                                                    return@rememberSwipeToDismissBoxState true
                                                }
                                                // Para otros gestos (como volver a su sitio), no hacemos nada
                                                false
                                            },
                                            // Solo permitimos deslizar en una dirección (de inicio a fin)
                                            positionalThreshold = { it * .50f } // El item se borrará al deslizar un 25%
                                        )

                                    // 2. Envolvemos la fila con SwipeToDismissBox
                                    SwipeToDismissBox(
                                        state = dismissState,
                                        // Definimos qué se muestra DETRÁS del elemento al deslizar
                                        backgroundContent = {
                                            val color = when (dismissState.dismissDirection) {
                                                SwipeToDismissBoxValue.StartToEnd -> Rojito // Color para el fondo de borrado
                                                else -> Color.Transparent
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(shape = RoundedCornerShape(30.dp))
                                                    .background(color)
                                                    .padding(15.dp),
                                                contentAlignment = Alignment.CenterStart // Alinea el icono a la derecha
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete, // Asegúrate de tener un icono de basura
                                                    contentDescription = "Eliminar",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                            }
                                        },
                                        // Solo activamos el deslizamiento de izquierda a derecha
                                        enableDismissFromStartToEnd = true,
                                        enableDismissFromEndToStart = false
                                    ) {
                                        Row(
                                            Modifier
                                                .clip(shape = RoundedCornerShape(30.dp))
                                                .fillMaxWidth()
                                                .background(color = GrisClaro)
                                                .padding(15.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = "${BuildConfig.API_BASE_URL}imagenes/${cartItem.producto.imagen}", // Usa cartItem
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
                                                val subtotal =
                                                    cartItem.quantity * cartItem.producto.precioVenta

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    TextFielCantidad(
                                                        quantity = cartItem.quantity, // Usa la cantidad del cartItem
                                                        stockDisponible = cartItem.producto.stockActual,
                                                        onQuantityChange = { newQuantity ->
                                                            val index = productosParaDevolucion.indexOf(cartItem)
                                                            if (index != -1) {
                                                                if (newQuantity > 0) {
                                                                    productosParaDevolucion[index] = cartItem.copy(quantity = newQuantity)
                                                                } else {
                                                                    productosParaDevolucion.removeAt(index)
                                                                }
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
                            }

                            Spacer(modifier = Modifier.padding(10.dp))
                            Box() {
                                HorizontalDivider(
                                    thickness = 1.dp,        // Grosor de la línea
                                    color = GrisClaro        // Color de la línea, puedes usar el que prefieras
                                )
                            }
                            Spacer(modifier = Modifier.padding(5.dp))
                            val totalVenta =
                                productosParaDevolucion.sumOf { it.producto.precioVenta * it.quantity }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.End // Alinea los textos a la derecha
                            ) {

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "Subtotal:",
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        "S/ ${"%.2f".format(totalVenta)}", // Usa la misma variable
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
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
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "TOTAL:",
                                        color = VerdeLimon,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "S/ ${"%.2f".format(totalVenta)}",
                                        color = VerdeLimon,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.padding(5.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(GrisClaro)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        when {
                                            diferencia > 0 -> "Monto a Reembolsar:"
                                            // Si la diferencia es negativa, el cliente debe pagar más
                                            diferencia < 0 -> "Monto a Adicionar:"
                                            // Si la diferencia es cero, no hay cambios
                                            else -> "Monto sin cambios:"
                                        },
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "S/${"%.2f".format(kotlin.math.abs(diferencia))}",
                                        color = VerdeLimon,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        BotonChevere(
                                            texto = "Anular",
                                            colorFondo = Rojito,
                                            colorTexto = Color.White,
                                            onClick = {
                                                mostrarDialogo = true
                                            }
                                        )
                                    }
                                    Column(Modifier.weight(1f)) {
                                        BotonChevere(
                                            texto = "Confirmar",
                                            colorFondo = VerdeLimon,
                                            colorTexto = Fondo1,
                                            onClick = {
                                                mostrarDialogoPago = true
                                            }
                                        )
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
                                    },
                                    cameraProvider = cameraProvider,
                                    previewView = previewView,
                                    imageAnalyzer = imageAnalyzer
                                )
                            }
                        }
                    }
                    if (mostrarDialogo) {
                        Dialog(
                            onDismissRequest = { mostrarDialogo = false }
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Fondo1),
                                modifier = Modifier.fillMaxWidth() // O un ancho específico con .width(300.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(15.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row (
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Column (
                                            modifier = Modifier.weight(1f)
                                        ){
                                            Text("Anular Venta",
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text("¿Estas seguro de anular la venta?" +
                                                    " Si anulas la venta el stock se repondrá y no se podra deshacer.",
                                                color = Color.White,
                                                fontSize = 15.sp)
                                        }
                                        Image(
                                            painterResource(R.drawable.devolucion),
                                            contentDescription = "",
                                            colorFilter = ColorFilter.tint(color = VerdeLimon),
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.size(80.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // 4. Botones centrados
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                mostrarDialogo = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Rojito,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) { Text("Cancelar") }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    try {
                                                        // Aseguramos que saleDetail no sea nulo
                                                        saleDetail?.let {
                                                            val response = RetrofitClient.productoService.anularVenta(it.id)
                                                            if (response.success) {
                                                                Toast.makeText(context, "Venta anulada correctamente", Toast.LENGTH_LONG).show()
                                                                val resultIntent = Intent()
                                                                setResult(Activity.RESULT_OK, resultIntent)
                                                                finish()
                                                            } else {
                                                                Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                                                        Log.e("AnularVenta", "Fallo al anular la venta", e)
                                                    } finally {
                                                        mostrarDialogo = false // Cierra el diálogo en cualquier caso
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = VerdeLimon,
                                                contentColor = Fondo1
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) { Text("Si") }
                                    }
                                }
                            }
                        }
                    }
                    if (mostrarDialogoPago) {
                        Dialog(
                            onDismissRequest = { mostrarDialogoPago = false } // Permite cerrar el diálogo tocando fuera
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Fondo1) // Usa el color de fondo de tu app
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Aquí pegamos tu código de selección de método de pago
                                    Text(
                                        text = "Método de Pago",
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            DropdownChevereBasico(
                                                options = ListaMetodos,
                                                selectedValue = metodoSelecionado,
                                                onValueChange = { nuevoMetodo ->
                                                    metodoSelecionado = nuevoMetodo
                                                },
                                                optionToString = { it },
                                                color = GrisClaro,
                                                colorFlecha = Fondo1,
                                                colorTexto = GrisClaro2
                                            )
                                        }
                                        if (metodoSelecionado == "Multiple") {
                                            Row(
                                                Modifier.weight(1f),
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Column(Modifier.weight(1f)) {
                                                    TextFieldChevereBasico(
                                                        value = yape,
                                                        onValueChange = {
                                                            yape = it
                                                            ultimoCampoEditado = "yape"
                                                        },
                                                        placeholder = "Y",
                                                        imeAction = ImeAction.Done,
                                                        keyboarType = KeyboardType.Decimal,
                                                        enabled = true,
                                                        color = GrisClaro,
                                                        colorTexto = GrisClaro2
                                                    )
                                                }
                                                Column(Modifier.weight(1f)) {
                                                    TextFieldChevereBasico(
                                                        value = efectivo,
                                                        onValueChange = {
                                                            efectivo = it
                                                            ultimoCampoEditado = "efectivo"
                                                        },
                                                        placeholder = "E",
                                                        imeAction = ImeAction.Done,
                                                        keyboarType = KeyboardType.Decimal,
                                                        enabled = true,
                                                        color = GrisClaro,
                                                        colorTexto = GrisClaro2
                                                    )
                                                }
                                            }
                                        } else {
                                            Column(Modifier.weight(1f)) {
                                                TextFieldChevereBasico(
                                                    value = "S/ ${"%.2f".format(totalVenta)}",
                                                    onValueChange = { /* NO HACER NADA */ },
                                                    placeholder = "S/0.00",
                                                    imeAction = ImeAction.Done,
                                                    keyboarType = KeyboardType.Decimal,
                                                    enabled = false,
                                                    color = GrisClaro,
                                                    colorTexto = GrisClaro2
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // --- PASO 4: BOTONES DENTRO DEL DIÁLOGO ---
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { mostrarDialogoPago = false },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = Color.White,
                                                containerColor = Rojito
                                            )) {
                                            Text("Cancelar", color = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    try {
                                                        val idVentaOriginal = saleDetail?.id ?: return@launch

                                                        // 1. CONSTRUIR LA LISTA DE PRODUCTOS
                                                        val productosParaEnviar = productosParaDevolucion.map { cartItem ->
                                                            ProductoDevolucion(
                                                                idProducto = cartItem.producto.id!!, // Quitado el !!
                                                                cantidadNueva = cartItem.quantity,
                                                                precioVenta = cartItem.producto.precioVenta,
                                                                subtotalNuevo = cartItem.quantity * cartItem.producto.precioVenta
                                                            )
                                                        }

                                                        // 2. CALCULAR EL NUEVO MONTO TOTAL
                                                        val montoTotalNuevo = productosParaEnviar.sumOf { it.subtotalNuevo }

                                                        // --- INICIO DE LA CORRECCIÓN DE LÓGICA DE PAGO ---
                                                        val pagosParaEnviar = mutableListOf<PagoDevolucion>()

                                                        when (metodoSelecionado) {
                                                            "Multiple" -> {
                                                                val montoYapeBD = yape.toBigDecimalOrNull() ?: BigDecimal.ZERO
                                                                val montoEfectivoBD = efectivo.toBigDecimalOrNull() ?: BigDecimal.ZERO
                                                                val sumaPagosBD = montoYapeBD.add(montoEfectivoBD)
                                                                val totalVentaBD = BigDecimal(montoTotalNuevo.toString()) // Validar contra el nuevo total

                                                                // 1. Validar que la suma de pagos coincide con el TOTAL
                                                                if (sumaPagosBD.compareTo(totalVentaBD) != 0) {
                                                                    Toast.makeText(context, "La suma de pagos ($sumaPagosBD) no coincide con el total ($totalVentaBD).", Toast.LENGTH_LONG).show()
                                                                    return@launch
                                                                }

                                                                // 2. Crear los registros de pago
                                                                if (montoYapeBD > BigDecimal.ZERO) {
                                                                    pagosParaEnviar.add(PagoDevolucion("Yape", yape.toDouble()))
                                                                }
                                                                if (montoEfectivoBD > BigDecimal.ZERO) {
                                                                    pagosParaEnviar.add(PagoDevolucion("Efectivo", efectivo.toDouble()))
                                                                }
                                                            }
                                                            else -> { // Caso para "Efectivo", "Yape", etc. (Pago Único)
                                                                // Para pago único, se registra un solo pago por el total de la venta
                                                                pagosParaEnviar.add(PagoDevolucion(metodoSelecionado, montoTotalNuevo))
                                                            }
                                                        }
                                                        // --- FIN DE LA CORRECCIÓN DE LÓGICA DE PAGO ---

                                                        val datos = DatosProcesoDevolucion(
                                                            idVenta = idVentaOriginal,
                                                            montoTotalNuevo = montoTotalNuevo,
                                                            productosNuevos = productosParaEnviar,
                                                            pagosNuevos = pagosParaEnviar
                                                        )

                                                        // ENVIAR AL SERVIDOR...
                                                        val response = RetrofitClient.productoService.procesarDevolucion(datos)
                                                        if (response.success) {
                                                            Toast.makeText(context, "Devolución procesada con éxito", Toast.LENGTH_LONG).show()

                                                            val resultIntent = Intent()
                                                            setResult(Activity.RESULT_OK, resultIntent)
                                                            finish()
                                                        } else {
                                                            Toast.makeText(context, "Error del servidor: ${response.message}", Toast.LENGTH_LONG).show()
                                                        }

                                                    } catch (e: retrofit2.HttpException) {
                                                        // Tu código de manejo de HttpException es correcto
                                                        val errorBody = e.response()?.errorBody()?.string()
                                                        var errorMessage = "Error HTTP ${e.code()}"
                                                        if (!errorBody.isNullOrEmpty()) {
                                                            try {
                                                                val errorJson = org.json.JSONObject(errorBody)
                                                                errorMessage = errorJson.getString("message")
                                                            } catch (jsonE: org.json.JSONException) {
                                                                errorMessage = errorBody
                                                            }
                                                        }
                                                        Log.e("ProcesarDevolucion", errorMessage)
                                                        Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                                                    } catch (e: Exception) {
                                                        // Tu código de manejo de Exception es correcto
                                                        Log.e("ProcesarDevolucion", "Fallo general al procesar la devolución", e)
                                                        Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                                                    } finally {
                                                        mostrarDialogoPago = false
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = VerdeLimon,
                                                contentColor = Color.White)
                                        ) {
                                            Text("Confirmar Venta")
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

    @Parcelize
    data class ProductoDevolucion(
        @SerializedName("id_producto") val idProducto: Int,
        @SerializedName("cantidad_nueva") val cantidadNueva: Int,
        @SerializedName("precio_venta") val precioVenta: Double,
        @SerializedName("subtotal_nuevo") val subtotalNuevo: Double
    ) : Parcelable

    @Parcelize
     data class PagoDevolucion(
        @SerializedName("metodo_pago") val metodoPago: String,
        @SerializedName("monto") val monto: Double
    ) : Parcelable
    @Parcelize
    data class DatosProcesoDevolucion(
        @SerializedName("id_venta") val idVenta: Int,
        @SerializedName("monto_total_nuevo") val montoTotalNuevo: Double,
        @SerializedName("productos_nuevos") val productosNuevos: List<ProductoDevolucion>,
        @SerializedName("pagos_nuevos") val pagosNuevos: List<PagoDevolucion>
        // Nota: La lógica de pagos la manejaremos en el servidor para simplificar
    ) : Parcelable
}