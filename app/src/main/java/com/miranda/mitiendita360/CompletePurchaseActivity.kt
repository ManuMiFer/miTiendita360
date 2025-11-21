package com.miranda.mitiendita360

import BarcodeAnalyzer
import ScannerView
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.miranda.mitiendita360.models.*
import com.miranda.mitiendita360.network.ProductoService

import com.miranda.mitiendita360.network.RetrofitClient
import com.miranda.mitiendita360.ui.components.BotonChevere
import com.miranda.mitiendita360.ui.components.DatePickerField
import com.miranda.mitiendita360.ui.components.DropdownChevere
import com.miranda.mitiendita360.ui.components.DropdownChevere2
import com.miranda.mitiendita360.ui.components.TextFieldChevere
import com.miranda.mitiendita360.ui.components.TextFieldChevere2
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.GrisClaro2
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import com.miranda.mitiendita360.ui.theme.xd
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.Executors

class CompletePurchaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Lógica de parseo inicial ---
        val purchaseDataJson = intent.getStringExtra("PURCHASE_DATA_JSON")
        val purchaseData: PurchaseData = try {
            Gson().fromJson(purchaseDataJson, PurchaseData::class.java)
        } catch (e: Exception) {
            Log.e("CompletePurchase", "Error al parsear el JSON recibido", e)
            finish()
            return
        }
        val scannedProducts = purchaseData.productos
        if (scannedProducts.isNullOrEmpty()) {
            Toast.makeText(this, "No se encontraron productos para registrar.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {

            MiTiendita360Theme {
                // --- Estados ---
                var listaProveedores by remember { mutableStateOf(listOf<Proveedor>()) }
                var listaCategorias by remember { mutableStateOf(listOf<Categoria>()) }
                var currentProductIndex by remember { mutableIntStateOf(0) }
                val productsToSave = remember { mutableStateListOf<Producto>() }
                val imageUrisToUpload = remember { mutableMapOf<Int, Uri>() }

                var imagenExistente by remember { mutableStateOf("") }
                var nombre by remember { mutableStateOf("") }
                var isExistingProduct by remember { mutableStateOf(false) }
                var stockToAdd by remember { mutableStateOf("") }
                var existingProductId by remember { mutableStateOf<Int?>(null) }
                var categoriaId by remember { mutableStateOf<Int?>(null) }
                var categoriaNombre by remember { mutableStateOf("Selecciona una categoría") }
                var proveedorId by remember { mutableStateOf<String?>(null) }
                var proveedorNombre by remember { mutableStateOf("Selecciona un Proveedor") }
                var precioCompra by remember { mutableStateOf("") }
                var precioVenta by remember { mutableStateOf("") }
                var marca by remember { mutableStateOf("") }
                var fechaVencimiento by remember { mutableStateOf("") }
                var stockActual by remember { mutableStateOf("") }
                var stockMinimo by remember { mutableStateOf("") }
                var codigoBarra by remember { mutableStateOf("") }

                // --- Estados de cámara e imagen ---
                var imagenUri by remember { mutableStateOf<Uri?>(null) }
                var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
                var mostrarDialogoImagen by remember { mutableStateOf(false) }
                var mostrarScanner by remember { mutableStateOf(false) }

                val context = LocalContext.current
                val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
                val previewView = remember { PreviewView(context) }
                val executor = remember { Executors.newSingleThreadExecutor() }

                val galeriaLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent(),
                    onResult = { uri ->
                        if (uri != null) {
                            imagenUri = uri
                            imageUrisToUpload[currentProductIndex] = uri
                        }
                    }
                )
                val camaraLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicture(),
                    onResult = { success ->
                        if (success && tempCameraUri != null) {
                            // --- ¡CORRECCIÓN CLAVE! ---
                            // En lugar de usar la URI temporal directamente, creamos una copia segura.
                            val safeUri = createCopyAndGetUri(context, tempCameraUri!!)
                            if (safeUri != null) {
                                imagenUri = safeUri // Actualiza la UI
                                imageUrisToUpload[currentProductIndex] = safeUri // Guarda la URI segura para la subida
                            } else {
                                // Si la copia falla, informa al usuario.
                                Toast.makeText(context, "Error al procesar la foto.", Toast.LENGTH_SHORT).show()
                                imagenUri = null
                            }
                        }
                    }
                )
                val barcodeAnalyzer = remember { BarcodeAnalyzer { barcode ->
                    codigoBarra = barcode
                    mostrarScanner = false
                } }
                val imageAnalyzer = remember { ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build().also { it.setAnalyzer(executor, barcodeAnalyzer) }
                }

                LaunchedEffect(codigoBarra) {
                    // Solo ejecutar si el código no está vacío
                    if (codigoBarra.isNotBlank()) {
                        val apiUrl = "https://world.openfoodfacts.org/api/v2/product/$codigoBarra.json?fields=brands"
                        try {
                            // Usamos la misma instancia de RetrofitClient
                            val response = RetrofitClient.productoService.getBrandFromBarcode(apiUrl)
                            if (response.status == 1 && !response.product?.brands.isNullOrBlank()) {
                                // Si se encuentra una marca, la asignamos a nuestra variable de estado.
                                marca = response.product!!.brands!!
                                Log.d("API_CALL", "Marca encontrada: ${response.product.brands}")
                            } else {
                                Log.d("API_CALL", "Producto no encontrado o sin marca en OpenFoodFacts.")
                            }
                        } catch (e: Exception) {
                            Log.e("API_CALL", "Error al llamar a OpenFoodFacts API", e)
                        }
                    }
                }
                LaunchedEffect(currentProductIndex) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

                    // Carga de proveedores y categorías (sin cambios)
                    if (listaProveedores.isEmpty()) {
                        try {
                            val responseProv =
                                RetrofitClient.proveedorService.getProveedores(uid, "")
                            if (responseProv.success && !responseProv.data.isNullOrEmpty()) {
                                listaProveedores = responseProv.data
                                val rucFromScan = purchaseData.rucProveedor?.trim()
                                if (!rucFromScan.isNullOrEmpty()) {
                                    listaProveedores.find { it.ruc?.trim() == rucFromScan }?.let {
                                        proveedorId = it.ruc
                                        proveedorNombre = it.razonSocial
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("API_ERROR", "Error al cargar proveedores: ${e.message}")
                        }
                    }
                    if (listaCategorias.isEmpty()) {
                        try {
                            val responseCat = RetrofitClient.categoriaService.getCategorias()
                            if (responseCat.success && !responseCat.data.isNullOrEmpty()) {
                                listaCategorias = responseCat.data
                            }
                        } catch (e: Exception) {
                            Log.e("API_ERROR", "Error al cargar categorías: ${e.message}")
                        }
                    }

                    if (currentProductIndex < scannedProducts.size) {
                        val currentScannedProduct = scannedProducts[currentProductIndex]

                        // 1. Busca si ya hemos guardado datos para este producto en la memoria
                        val savedState = productsToSave.find { it.nombre.equals(currentScannedProduct.nombre, ignoreCase = true) }

                        if (savedState != null) {
                            // 2. SI HAY DATOS GUARDADOS, RELLENA EL FORMULARIO CON ELLOS
                            Log.d("StateRestore", "Restaurando estado para: ${savedState.nombre}")
                            // (Toda esta lógica de restauración ya está correcta y no necesita cambios)
                            isExistingProduct = savedState.id != null
                            existingProductId = savedState.id
                            nombre = savedState.nombre
                            precioCompra = if (savedState.precioCompra > 0) String.format(Locale.US, "%.2f", savedState.precioCompra) else ""
                            precioVenta = if (savedState.precioVenta > 0) String.format(Locale.US, "%.2f", savedState.precioVenta) else ""
                            marca = savedState.marca ?: ""
                            stockMinimo = if (savedState.stockMinimo > 0) savedState.stockMinimo.toString() else ""
                            codigoBarra = savedState.codBarras ?: ""
                            categoriaId = savedState.idCategoria
                            proveedorId = savedState.idProveedor
                            fechaVencimiento = savedState.fechaCad?.let { fecha ->
                                try {
                                    val parser = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.US)
                                    formatter.format(parser.parse(fecha)!!)
                                } catch (e: Exception) { "" }
                            } ?: ""
                            if (isExistingProduct) {
                                stockToAdd = if (savedState.stockActual > 0) savedState.stockActual.toString() else ""
                                try {
                                    val response = RetrofitClient.productoService.getProducts(uid, nombre, null, 1)
                                    stockActual = response.data?.find { it.id == existingProductId }?.stockActual?.toString() ?: "0"
                                } catch (e: Exception) { stockActual = "0" }
                            } else {
                                stockActual = if (savedState.stockActual > 0) savedState.stockActual.toString() else ""
                            }
                            listaCategorias.find { it.id == categoriaId }?.let { categoriaNombre = it.tipo }
                            listaProveedores.find { it.ruc == proveedorId }?.let { proveedorNombre = it.razonSocial }

                        } else {

                            Log.d("StateRestore", "No hay estado, inicializando para: ${currentScannedProduct.nombre}")


                            isExistingProduct = false; existingProductId = null; nombre = currentScannedProduct.nombre; precioCompra = ""; stockActual = ""; categoriaId = null; categoriaNombre = "Selecciona una categoría"; precioVenta = ""; marca = ""; fechaVencimiento = ""; stockMinimo = ""; codigoBarra = ""; stockToAdd = ""

                            try {
                                val searchResponse = RetrofitClient.productoService.getProducts(uid, currentScannedProduct.nombre, null, 1)
                                val productoExistente = searchResponse.data?.find { it.nombre.equals(currentScannedProduct.nombre, ignoreCase = true) }

                                if (productoExistente != null) {
                                    Log.d("StateRestore", "Producto encontrado en la API: ${productoExistente.nombre}")
                                    imagenExistente = productoExistente.imagen ?: ""
                                    isExistingProduct = true
                                    existingProductId = productoExistente.id
                                    precioVenta = String.format(Locale.US, "%.2f", productoExistente.precioVenta)
                                    stockActual = productoExistente.stockActual.toString()
                                    precioCompra = String.format(Locale.US, "%.2f", productoExistente.precioCompra)
                                    marca = productoExistente.marca ?: ""
                                    stockMinimo = productoExistente.stockMinimo.toString()
                                    codigoBarra = productoExistente.codBarras ?: ""
                                    categoriaId = productoExistente.idCategoria
                                    fechaVencimiento = productoExistente.fechaCad?.let { fecha ->
                                        try {
                                            val parser = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                            val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.US)
                                            formatter.format(parser.parse(fecha)!!)
                                        } catch (e: Exception) { fecha }
                                    } ?: ""

                                    listaCategorias.find { it.id == productoExistente.idCategoria }?.let {
                                        categoriaNombre = it.tipo
                                    }

                                    // 2. Asigna el proveedor del producto existente y actualiza el Dropdown
                                    // (Esto sobrescribe el proveedor de la boleta si es diferente, lo cual es correcto)
                                    proveedorId = productoExistente.idProveedor
                                    listaProveedores.find { it.ruc == productoExistente.idProveedor }?.let {
                                        proveedorNombre = it.razonSocial
                                    }
                                }

                            } catch (e: Exception) {
                                Log.w("ProductSearch", "Error buscando producto, continuando como si fuera nuevo: ${e.message}")
                            }
                        }
                        imagenUri = imageUrisToUpload[currentProductIndex]
                    }
                }
                fun saveCurrentProductState() {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

                    // Convierte la fecha del DatePicker (dd/MM/yyyy) a formato de BD (yyyy-MM-dd)
                    val fechaParaBD = if (fechaVencimiento.isNotBlank()) {
                        try {
                            val parser = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.US)
                            val date = parser.parse(fechaVencimiento)
                            date?.let { java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it) }
                        } catch (e: Exception) {
                            // Si falla, asume que ya estaba en yyyy-MM-dd (útil si viene de la API)
                            fechaVencimiento
                        }
                    } else {
                        null
                    }

                    // Determina qué valor de stock enviar a la API
                    val stockAenviar = if (isExistingProduct) {
                        stockToAdd.toIntOrNull() ?: 0
                    } else {
                        stockActual.toIntOrNull() ?: 0
                    }

                    // Crea un objeto Producto con los datos actuales del formulario
                    val productState = Producto(
                        id = existingProductId,
                        nombre = nombre,
                        precioVenta = precioVenta.toDoubleOrNull() ?: 0.0,
                        stockActual = stockAenviar, // Este es el stock a *sumar*
                        stockMinimo = stockMinimo.toIntOrNull() ?: 0,
                        precioCompra = precioCompra.toDoubleOrNull() ?: 0.0,
                        fechaCad = fechaParaBD,
                        marca = if (marca.isBlank()) null else marca,
                        codBarras = if (codigoBarra.isBlank()) null else codigoBarra,
                        idProveedor = proveedorId ?: "", // Evita nulls
                        idUsuario = uid,
                        idCategoria = categoriaId ?: 0, // Evita nulls
                        imagen = null, // La imagen se maneja por separado
                        estado = 1
                    )

                    // Revisa si ya tenemos un estado para este índice, si sí, lo actualiza. Si no, lo añade.
                    val existingEntryIndex = productsToSave.indexOfFirst { it.nombre.equals(productState.nombre, ignoreCase = true) }
                    if (existingEntryIndex != -1) {
                        productsToSave[existingEntryIndex] = productState
                    } else {
                        productsToSave.add(productState)
                    }

                    // Guarda también la URI de la imagen seleccionada para este índice
                    if(imagenUri != null) {
                        imageUrisToUpload[currentProductIndex] = imagenUri!!
                    }
                }
                fun handleNextProduct() {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (nombre.isBlank() || categoriaId == null || proveedorId == null) {
                        Toast.makeText(this@CompletePurchaseActivity, "Completa los campos de Nombre, Categoría y Proveedor.", Toast.LENGTH_LONG).show()
                        return
                    }

                    if (!isExistingProduct) {
                        val pCompra = precioCompra.toDoubleOrNull()
                        val pVenta = precioVenta.toDoubleOrNull()
                        val sActual = stockActual.toDoubleOrNull()
                        val sMinimo = stockMinimo.toDoubleOrNull()

                        if (pVenta != null && pCompra != null && pVenta <= pCompra) {
                            Toast.makeText(this@CompletePurchaseActivity, "El precio de venta debe ser mayor al de compra.", Toast.LENGTH_SHORT).show()
                            return // Detiene la ejecución
                        }
                        if (sMinimo != null && sActual != null && sMinimo >= sActual) {
                            Toast.makeText(this@CompletePurchaseActivity, "El stock mínimo debe ser menor al actual.", Toast.LENGTH_SHORT).show()
                            return // Detiene la ejecución
                        }
                    }

                    saveCurrentProductState()

                    if (currentProductIndex < scannedProducts.size - 1) {
                        currentProductIndex++
                    } else {
                        // El resto de tu lógica para agrupar y enviar es correcta
                        // ... (el bloque del else no necesita cambios)
                        Toast.makeText(this@CompletePurchaseActivity, "Guardando compra completa...", Toast.LENGTH_LONG).show()
                        val nuevaCompra = Compra(
                            fechaEntrega = purchaseData.fecha ?: java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                            montoTotal = purchaseData.total ?: 0.0,
                            idProveedor = proveedorId!!,
                            idUsuario = uid.toString()
                        )
                        val productosAgrupados = productsToSave.groupBy { it.id }.flatMap { (id, lista) ->
                            if (id == null) lista
                            else {
                                val p = lista.first()
                                val stockTotal = lista.sumOf { it.stockActual }
                                listOf(p.copy(stockActual = stockTotal))
                            }
                        }
                        Log.d("GuardarFecha", "Objeto final enviado a la API: ${Gson().toJson(productosAgrupados)}")
                        val request = CompraCompletaRequest(compra = nuevaCompra, productos = productosAgrupados)
                        // En handleNextProduct
                        lifecycleScope.launch {
                            try {
                                val response = RetrofitClient.compraService.registrarCompraCompleta(request)
                                if (response.success && response.data != null) {
                                    Toast.makeText(this@CompletePurchaseActivity, "Compra registrada. Subiendo imágenes...", Toast.LENGTH_LONG).show()

                                    val idsProductosProcesados = response.data
                                    if (idsProductosProcesados.size == productosAgrupados.size) {
                                        coroutineScope {
                                            productosAgrupados.forEachIndexed { index, productoAgrupado ->
                                                // ... (la lógica para encontrar la URI es la misma)
                                                val idRealDelProducto = idsProductosProcesados[index]
                                                val productoOriginalEnLista = productsToSave.find { it.nombre == productoAgrupado.nombre }
                                                val indiceOriginal = productsToSave.indexOf(productoOriginalEnLista)
                                                val uriDeEsteProducto = imageUrisToUpload[indiceOriginal]

                                                if (uriDeEsteProducto != null) {
                                                    // Lanza cada subida en una corrutina separada dentro del scope.
                                                    // El scope no terminará hasta que todas estas corrutinas finalicen.
                                                    launch {
                                                        uploadImageAndUpdateProduct(uriDeEsteProducto, idRealDelProducto, uid.toString(), this@CompletePurchaseActivity)
                                                    }
                                                }
                                            }
                                        }
                                        Log.d("ImageUpload", "Todas las subidas de imágenes han finalizado.")
                                    }

                                    // 4. Solo llamamos a finish() DESPUÉS de que todas las subidas han completado.
                                    Toast.makeText(this@CompletePurchaseActivity, "¡Proceso completado!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@CompletePurchaseActivity, PurchaseDetailActivity::class.java).apply {
                                        // Limpia el stack para que el usuario no pueda volver a esta pantalla con el botón de atrás.
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(this@CompletePurchaseActivity, "Error al guardar la compra: ${response.message}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Log.e("API_ERROR", "Error al registrar la compra completa: ${e.message}", e)
                            }
                        }
                    }
                }
                fun handlePreviousProduct() {
                    // Solo retrocedemos si no estamos en el primer producto (índice 0)
                    if (currentProductIndex > 0) {
                        currentProductIndex--
                    }
                }


                if (mostrarDialogoImagen) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoImagen = false },
                        // 1. Color de fondo explícito para el diálogo
                        containerColor = Color.White,
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.image),
                                contentDescription = "Subir Imagen",
                                tint = VerdeLimon
                            )
                        },
                        title = {
                            Text("Seleccionar Imagen", color = Fondo1)
                        },
                        text = {
                            Text("¿Desde dónde quieres obtener la imagen?", color = GrisClaro)
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    mostrarDialogoImagen = false
                                    // Creamos un URI temporal donde la cámara guardará la foto
                                    val uri = createImageUri(context)
                                    tempCameraUri = uri
                                    // Lanzamos la cámara y le pasamos el URI temporal
                                    camaraLauncher.launch(uri)
                                }
                            ) {
                                // 2. Color explícito para el texto de los botones
                                Text("Tomar Foto", color = VerdeLimon, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    mostrarDialogoImagen = false
                                    galeriaLauncher.launch("image/*")
                                }
                            ) {
                                Text("Galería", color = VerdeLimon, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
                if (mostrarScanner) {
                    val cameraProvider = cameraProviderFuture.get()
                    ScannerView(
                        onBarcodeScanned = { barcode ->
                            if (barcode == null) {
                                mostrarScanner = false
                            }
                        },
                        cameraProvider = cameraProvider,
                        previewView = previewView,
                        imageAnalyzer = imageAnalyzer
                    )
                } else {
                    // El resto de tu UI no necesita cambios
                    Scaffold(
                        topBar = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .background(color = Fondo1)
                                    .padding(25.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                ) {
                                    Image(
                                        imageVector = (Icons.AutoMirrored.Filled.ArrowBackIos),
                                        contentDescription = "Volver",
                                        colorFilter = ColorFilter.tint(color = Color.White),
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clickable { finish() }
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Image(
                                        painterResource(R.drawable.tienda),
                                        contentDescription = "",
                                        colorFilter = ColorFilter.tint(color = VerdeLimon),
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.size(70.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(5.dp))
                                    Column {
                                        Text(
                                            "Producto ${currentProductIndex + 1} de ${scannedProducts.size}",
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Detallar Compra",
                                            color = Color.White,
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        LazyColumn(
                            Modifier
                                .fillMaxSize()
                                .background(color = Color.White)
                                .padding(innerPadding),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item {
                                Column(
                                    Modifier
                                        .clip(
                                            shape = WideOvalBottomShape(
                                                arcHeight = 200f,
                                                horizontalControlOffset = 180f
                                            )
                                        )
                                        .background(color = Fondo1)
                                        .padding(horizontal = 40.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column(modifier = Modifier.weight(6f)) {
                                            TextFieldChevere2(
                                                value = nombre,
                                                onValueChange = { nombre = it },
                                                label = "Nombre:",
                                                placeholder = "nomProducto_Medida",
                                                imeAction = ImeAction.Next,
                                                color = Color.Yellow,
                                                enabled = if(isExistingProduct) false else true
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .height(50.dp)
                                                    .aspectRatio(1f)
                                                    .clip(shape = RoundedCornerShape(13.dp))
                                                    .background(color = Fondo1)
                                                    .clickable { if (!isExistingProduct) mostrarDialogoImagen = true },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (imagenUri == null) {
                                                    val imageRequest =
                                                        ImageRequest.Builder(LocalContext.current)
                                                            .data("${BuildConfig.API_BASE_URL}imagenes/${imagenExistente}")
                                                            .crossfade(true) // Opcional: añade una bonita transición de fundido
                                                            .diskCachePolicy(CachePolicy.ENABLED) // Permite guardar en caché del disco
                                                            .memoryCachePolicy(CachePolicy.ENABLED) // Permite guardar en caché de memoria (más rápido)
                                                            .build()

                                                    AsyncImage(
                                                        model = if (isExistingProduct) imageRequest else R.drawable.image, // Pasamos la petición de imagen, no solo la URL
                                                        contentDescription = "Imagen seleccionada",
                                                        modifier = Modifier.fillMaxSize(), // Rellena el Box cuadrado
                                                        contentScale = ContentScale.Crop, // Recorta para llenar el espacio,
                                                        colorFilter = if (isExistingProduct) null  else ColorFilter.tint(VerdeLimon),

                                                    )
                                                } else {
                                                    // Si hay una imagen, muéstrala usando Coil
                                                    AsyncImage(
                                                        model = imagenUri,
                                                        contentDescription = "Imagen seleccionada",
                                                        modifier = Modifier.fillMaxSize(), // Rellena el Box cuadrado
                                                        contentScale = ContentScale.Crop // Recorta para llenar el espacio
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.padding(5.dp))
                                    DropdownChevere(
                                        label = "Categoria:",
                                        options = listaCategorias,
                                        selectedValue = categoriaNombre,
                                        onValueChange = { c ->
                                            categoriaId = c.id; categoriaNombre = c.tipo
                                        },
                                        optionToString = { it.tipo })
                                    Spacer(modifier = Modifier.padding(5.dp))
                                    DropdownChevere2(
                                        label = "Proveedor:",
                                        options = listaProveedores,
                                        selectedValue = proveedorNombre,
                                        onValueChange = { p ->
                                            proveedorId = p.ruc; proveedorNombre = p.razonSocial
                                        },
                                        optionToString = { it.razonSocial },
                                        color = Color.Yellow,
                                        colorFlecha = VerdeLimon
                                    )
                                    Spacer(modifier = Modifier.padding(5.dp))
                                    TextFieldChevere2(
                                        value = marca,
                                        onValueChange = { marca = it },
                                        label = "Marca:",
                                        imeAction = ImeAction.Next,
                                        placeholder = "",
                                        color = Color.White,
                                        enabled = if(isExistingProduct) false else true
                                    )
                                    Spacer(modifier = Modifier.padding(5.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            TextFieldChevere2(
                                                value = precioCompra,
                                                onValueChange = { precioCompra = it },
                                                label = "Precio de Compra:",
                                                placeholder = "S/0.00",
                                                imeAction = ImeAction.Next,
                                                color = Color.White,
                                                enabled = if(isExistingProduct) false else true
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            val pCompra = precioCompra.toDoubleOrNull()
                                            val pVenta = precioVenta.toDoubleOrNull()
                                            TextFieldChevere2(
                                                value = precioVenta,
                                                onValueChange = { precioVenta = it },
                                                label = "Precio de Venta:",
                                                placeholder = "S/0.00",
                                                imeAction = ImeAction.Next,
                                                color=
                                                    if (pCompra == null || pVenta == null) {
                                                        Color.White
                                                    } else {
                                                        // 3. Ahora sí, compara los números. El color será Rojo si la venta es menor que la compra.
                                                        if (pVenta > pCompra) Color.White else Color.Red
                                                    },
                                                enabled = if(isExistingProduct) false else true
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.padding(5.dp))
                                    DatePickerField(
                                        label = "Fecha de Vencimiento:",
                                        selectedDate = fechaVencimiento,
                                        onDateSelected = { fechaVencimiento = it })
                                    Spacer(modifier = Modifier.padding(5.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        if (isExistingProduct) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                // 1. Muestra el stock actual (no editable)
                                                TextFieldChevere2(
                                                    value = stockActual,
                                                    onValueChange = { /* No hacer nada */ },
                                                    label = "Stock Actual:",
                                                    imeAction = ImeAction.Next,
                                                    placeholder = "",
                                                    enabled = false,
                                                    color = Color.White,
                                                    keyboarType = KeyboardType.Number
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                // 2. Campo para añadir el nuevo stock
                                                TextFieldChevere2(
                                                    value = stockToAdd,
                                                    onValueChange = { stockToAdd = it },
                                                    label = "Stock a Añadir:",
                                                    imeAction = ImeAction.Next,
                                                    placeholder = "0",
                                                    color = Color.White // Lo marcamos como importante
                                                )
                                            }
                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                TextFieldChevere2(
                                                    value = stockMinimo,
                                                    onValueChange = { stockMinimo = it },
                                                    label = "Stock Minimo:",
                                                    imeAction = ImeAction.Next,
                                                    placeholder = "",
                                                    color = Color.White,
                                                    enabled = if (isExistingProduct) false else true
                                                )
                                            }
                                        } else {
                                            // --- SI EL PRODUCTO ES NUEVO (comportamiento original) ---
                                            Column(modifier = Modifier.weight(1f)) {
                                                TextFieldChevere2(
                                                    value = stockActual,
                                                    onValueChange = { stockActual = it },
                                                    label = "Stock a Añadir:",
                                                    imeAction = ImeAction.Next,
                                                    placeholder = "",
                                                    color = Color.White
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                val sActual = stockActual.toDoubleOrNull()
                                                val sMinimo = stockMinimo.toDoubleOrNull()
                                                TextFieldChevere2(
                                                    value = stockMinimo,
                                                    onValueChange = { stockMinimo = it },
                                                    label = "Stock Minimo:",
                                                    imeAction = ImeAction.Next,
                                                    placeholder = "",
                                                    color = if (sActual == null || sMinimo == null) {
                                                        Color.White
                                                    } else {
                                                        // 3. Ahora sí, compara los números. El color será Rojo si la venta es menor que la compra.
                                                        if (sActual > sMinimo) Color.White else Color.Red
                                                    },
                                                )
                                            }
                                        }
                                    }



                                    Spacer(modifier = Modifier.padding(5.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column(modifier = Modifier.weight(6f)) {
                                            TextFieldChevere2(
                                                value = codigoBarra,
                                                onValueChange = { codigoBarra = it },
                                                label = "Codigo de Barras:",
                                                imeAction = ImeAction.Done,
                                                placeholder = "",
                                                color = Color.White,
                                                enabled = if(isExistingProduct) false else true

                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Column(
                                                Modifier
                                                    .size(50.dp)
                                                    .clip(shape = RoundedCornerShape(13.dp))
                                                    .background(color = Fondo1)
                                                    .clickable {
                                                        if (!isExistingProduct){
                                                            barcodeAnalyzer.startScanning()
                                                            mostrarScanner = true
                                                        } else{
                                                            return@clickable
                                                        }
                                                    },
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.barcodescan),
                                                    contentDescription = "Escanear Codigo de barras",
                                                    tint = VerdeLimon,
                                                    modifier = Modifier.size(30.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.padding(45.dp))
                                }
                            }
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 15.dp)
                                ) {
                                    if (currentProductIndex > 0) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            BotonChevere(
                                                texto = "Atrás",
                                                colorFondo = GrisClaro2, // Un color diferente para distinguirlo
                                                colorTexto = Color.White,
                                                onClick = { handlePreviousProduct() }
                                            )
                                        }
                                        Spacer(modifier = Modifier.padding(2.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        BotonChevere(
                                            texto = if (currentProductIndex < scannedProducts.size - 1) "Siguiente" else "Guardar",
                                            colorFondo = VerdeLimon,
                                            colorTexto = Fondo1,
                                            onClick = {
                                                if(isExistingProduct){
                                                    if(stockToAdd.isBlank() || stockToAdd.isEmpty()){
                                                    Toast.makeText(this@CompletePurchaseActivity, "Completa el campo Stock a Añadir.", Toast.LENGTH_LONG).show()}
                                                }else{
                                                    handleNextProduct()
                                                }
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.padding(2.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        BotonChevere(
                                            texto = "Cancelar",
                                            colorFondo = xd,
                                            colorTexto = Color.White,
                                            onClick = { finish() }
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

    private fun createImageUri(context: Context): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "new_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }
    // En CompletePurchaseActivity.kt

    // --- AÑADE ESTA NUEVA FUNCIÓN COMPLETA ---
    private fun createCopyAndGetUri(context: Context, originalUri: Uri): Uri? {
        return try {
            // Abre un flujo de entrada para leer la imagen original (de la cámara)
            val inputStream = context.contentResolver.openInputStream(originalUri)
            // Crea un nuevo archivo en el directorio de caché de tu app
            val tempFile = File.createTempFile(
                "camera_copy_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )
            // Abre un flujo de salida para escribir en tu nuevo archivo
            val outputStream = FileOutputStream(tempFile)

            // Copia los bytes del archivo original al nuevo
            inputStream?.copyTo(outputStream)

            // Cierra los flujos
            inputStream?.close()
            outputStream.close()

            // Obtiene la URI de tu copia local, que tendrá permisos permanentes
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider", // Asegúrate de que coincida con tu Manifest
                tempFile
            )
        } catch (e: Exception) {
            Log.e("FileCopyError", "Error al copiar la imagen de la cámara: ${e.message}", e)
            null
        }
    }

    // En CompletePurchaseActivity.kt

    // --- CORRECCIÓN #1 ---
// Conviértela en una suspend fun y quita el lifecycleScope
    private suspend fun uploadImageAndUpdateProduct(uri: Uri, productId: Int, uid: String, context: Context) {
        // Quita el "lifecycleScope.launch {" de aquí
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val imageBytes = inputStream.readBytes()
                val requestFile = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

                val nombreImagen = "${uid}_${productId}.jpg"
                val body = MultipartBody.Part.createFormData("imagen", nombreImagen, requestFile)
                val nombreArchivoBody = nombreImagen.toRequestBody("text/plain".toMediaTypeOrNull())

                // Sube la imagen
                val uploadResponse = RetrofitClient.productoService.uploadImage(body, nombreArchivoBody)

                if (uploadResponse.success) {
                    Log.d("ImageUpload", "Imagen subida con éxito: $nombreImagen")

                    // Actualiza la BD con el nombre de la imagen
                    val updateRequest = ImageUpdateRequest(idProducto = productId, nombreImagen = nombreImagen)
                    val updateResponse = RetrofitClient.productoService.updateImagePath(updateRequest)

                    if (updateResponse.success) {
                        Log.d("ImageUpload", "Ruta de imagen actualizada en BD para el producto ID: $productId")
                    } else {
                        Log.e("ImageUpload", "Error al actualizar ruta en BD: ${updateResponse.message}")
                    }
                } else {
                    Log.e("ImageUpload", "Error al subir la imagen: ${uploadResponse.message}")
                }
            }
        } catch (e: Exception) {
            // Si el Job es cancelado por el finish(), este catch se activará
            if (e is kotlinx.coroutines.CancellationException) {
                // Es normal que se cancele si el usuario sale, no mostramos error.
                Log.w("ImageUpload", "Subida cancelada: ${e.message}")
            } else {
                Log.e("ImageUpload", "Excepción al subir la imagen: ${e.message}", e)
            }
        }
        // Quita el cierre del "lifecycleScope.launch" de aquí
    }

}

