package com.miranda.mitiendita360


import BarcodeAnalyzer
import ScannerView
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
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
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.models.Categoria
import com.miranda.mitiendita360.models.ImageUpdateRequest
import com.miranda.mitiendita360.models.Producto
import com.miranda.mitiendita360.models.Proveedor
import com.miranda.mitiendita360.network.CategoriaService
import com.miranda.mitiendita360.network.ProductoService

import com.miranda.mitiendita360.network.ProveedorService
import com.miranda.mitiendita360.ui.components.BotonChevere
import com.miranda.mitiendita360.ui.components.DatePickerField
import com.miranda.mitiendita360.ui.components.TextFieldChevere
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import com.miranda.mitiendita360.ui.theme.xd
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

class WideOvalBottomShape(
    private val arcHeight: Float,
    private val horizontalControlOffset: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - arcHeight)

            // Curva Cúbica (dos puntos de control)
            cubicTo(
                // Punto de control 1: a la derecha del centro, controla la curva derecha
                x1 = size.width - horizontalControlOffset, y1 = size.height,
                // Punto de control 2: a la izquierda del centro, controla la curva izquierda
                x2 = horizontalControlOffset, y2 = size.height,
                // Punto final de la curva
                x3 = 0f, y3 = size.height - arcHeight
            )
            close()
        }
        return Outline.Generic(path)
    }
}
class ProductInsertActivity : ComponentActivity() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://manuelmirandafernandez.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val categoriaService = retrofit.create(CategoriaService::class.java)
    val productoService = retrofit.create(ProductoService::class.java)
    private val proveedorService = retrofit.create(ProveedorService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            var nombre by remember { mutableStateOf("") }
            var categoriaId by remember { mutableStateOf<Int?>(null) }
            var categoriaNombre by remember { mutableStateOf("Selecciona una categoría") }
            var proveedorNombre by remember { mutableStateOf("Selecciona un Proveedor") }
            var proveedorId by remember { mutableStateOf<String?>(null) }
            var listaCategorias by remember { mutableStateOf(listOf<Categoria>()) }
            var listaProveedores by remember { mutableStateOf(listOf<Proveedor>()) }
            var precioCompra by remember { mutableStateOf("") }
            var precioVenta by remember { mutableStateOf("") }
            var marca by remember { mutableStateOf("") }
            var FechaVencimiento by remember { mutableStateOf("") }
            var stockActual by remember { mutableStateOf("") }
            var stockMinimo by remember { mutableStateOf("") }
            var codigoBarra by remember { mutableStateOf("") }
            var imagen by remember { mutableStateOf("") }
            var mostrarScanner by remember { mutableStateOf(false) }
            var proveedor by remember { mutableStateOf("") }

            val context = LocalContext.current
            val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
            val previewView = remember { PreviewView(context) }
            val executor = remember { Executors.newSingleThreadExecutor() }

            var imagenUri by remember { mutableStateOf<Uri?>(null) }
            var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

            // 2. Estado para controlar la visibilidad del diálogo de selección
            var mostrarDialogoImagen by remember { mutableStateOf(false) }

            // 3. Lanzador para la galería
            val galeriaLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
                onResult = { uri ->
                    // Cuando el usuario selecciona una imagen, el resultado (su URI) llega aquí
                    if (uri != null) {
                        imagenUri = uri
                    }
                }
            )
            // 4. Lanzador para la cámara (Necesita un URI temporal para guardar el archivo)
            val camaraLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicture(),
                onResult = { success ->
                    if (success) {
                        // Si el usuario cancela, reseteamos el URI para no usar una imagen vacía
                        imagenUri = tempCameraUri
                    }
                }
            )

            val barcodeAnalyzer = remember {
                BarcodeAnalyzer { barcode ->
                    // Esta es la acción que se ejecuta cuando se detecta un código
                    codigoBarra = barcode
                    mostrarScanner = false // Oculta el escáner automáticamente
                    Log.d("Scanner", "Código recibido: $barcode")
                }
            }

            val imageAnalyzer = remember {
                ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor, barcodeAnalyzer)
                    }
            }

            LaunchedEffect (key1 = true){
                // Obtener el idUsuario de Firebase
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    lifecycleScope.launch {
                        // Cargar categorías
                        try {
                            val responseCat = categoriaService.getCategorias()
                            if (responseCat.success && responseCat.data != null) {
                                listaCategorias = responseCat.data
                            }
                        } catch (e: Exception) {
                            Log.e("API_ERROR", "Error al obtener categorías: ${e.message}")
                        }

                        // --- CARGAR PROVEEDORES ---
                        try {
                            val responseProv = proveedorService.getProveedores(uid, "")
                            if (responseProv.success && responseProv.data != null) {
                                listaProveedores = responseProv.data
                            }
                        } catch (e: Exception) {
                            Log.e("API_ERROR", "Error al obtener proveedores: ${e.message}")
                        }
                    }
                }
            }

            fun createImageUri(context: Context): Uri {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "new_image_${System.currentTimeMillis()}.jpg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }
                return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
            }

// --- DIÁLOGO PARA ELEGIR LA FUENTE DE LA IMAGEN ---
            if (mostrarDialogoImagen) {
                AlertDialog(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.image),
                            contentDescription = "Subir Imagen",
                            tint = VerdeLimon
                        )
                    },
                    onDismissRequest = { mostrarDialogoImagen = false },
                    containerColor = Color.White,
                    title = { Text("Seleccionar Imagen",
                        color = Fondo1) },
                    text = { Text("¿Desde dónde quieres obtener la imagen?",
                        color = GrisClaro
                    ) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mostrarDialogoImagen = false
                                // Creamos un URI temporal donde la cámara guardará la foto
                                val uri = createImageUri(this@ProductInsertActivity)
                                // Lo guardamos en nuestra variable temporal
                                tempCameraUri = uri
                                // Lanzamos la cámara y le pasamos el URI temporal
                                camaraLauncher.launch(uri)
                            }
                        ) { Text("Tomar Foto",
                            color = VerdeLimon) }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                mostrarDialogoImagen = false
                                galeriaLauncher.launch("image/*")
                            }
                        ) { Text("Galería",
                            color = VerdeLimon) }
                    }
                )
            }
            MiTiendita360Theme {
                if (mostrarScanner) {
                    // Obtenemos el CameraProvider de forma segura
                    val cameraProvider = cameraProviderFuture.get()
                    ScannerView(
                        onBarcodeScanned = { barcode ->
                            // Esta lambda se usa si hay un error o se cancela
                            if (barcode == null) {
                                mostrarScanner = false
                            }
                        },
                        // Le pasamos los objetos que ya creamos
                        cameraProvider = cameraProvider,
                        previewView = previewView,
                        imageAnalyzer = imageAnalyzer
                    )
                }
                else {
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
                                            imageVector = (Icons.Default.ArrowBackIos),
                                            contentDescription = "",
                                            colorFilter = ColorFilter.tint(color = Color.White),
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clickable {
                                                    finish()
                                                }
                                        )
                                }
                                Row (
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ){
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
                                            "Nuevo",
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
                        LazyColumn(
                            Modifier
                                .fillMaxSize()
                                .background(color = Color.White)
                                .padding(innerPadding),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        )
                        {
                           item {
                               Column (
                                   Modifier
                                       .clip(
                                           shape = WideOvalBottomShape(
                                               arcHeight = 200f, // Profundidad de la curva
                                               horizontalControlOffset = 180f
                                           )
                                       )
                                       .background(color = Fondo1)
                                       .padding(horizontal = 40.dp)

                               )
                               {
                                   // TextField Nombre
                                   Row(
                                       modifier = Modifier.fillMaxWidth(),
                                       horizontalArrangement = Arrangement.spacedBy(8.dp),
                                       verticalAlignment = Alignment.Bottom
                                   ) {
                                       Column(
                                           modifier = Modifier.weight(6f)
                                       ) {
                                           TextFieldChevere(
                                               value = nombre,
                                               onValueChange = { nombre = it },
                                               label = "Nombre:",
                                               placeholder = "nomProducto_Medida",
                                               imeAction = ImeAction.Next
                                           )
                                       }
                                       Column(
                                           modifier = Modifier.weight(1f)
                                       ) {
                                           // Box que contiene el ícono o la imagen
                                           Box(
                                               modifier = Modifier
                                                   .height(50.dp) // 1. Fija la altura del contenedor
                                                   .aspectRatio(1f) // 2. Fuerza a que el ancho sea igual a la altura (1f = 1/1 = cuadrado)
                                                   .clip(shape = RoundedCornerShape(13.dp))
                                                   .background(color = Fondo1)
                                                   .clickable {
                                                       // ¡ACCIÓN CLAVE! Al hacer clic, mostramos el diálogo
                                                       mostrarDialogoImagen = true
                                                   },
                                               contentAlignment = Alignment.Center
                                           ) {
                                               // Si no hay imagen seleccionada, muestra el ícono
                                               if (imagenUri == null) {
                                                   Icon(
                                                       painter = painterResource(id = R.drawable.image),
                                                       contentDescription = "Subir Imagen",
                                                       tint = VerdeLimon,
                                                       modifier = Modifier.size(30.dp)
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

                                   // Dropdown Categoria

                                   DropdownChevere(
                                       label = "Categoria:",
                                       options = listaCategorias,
                                       selectedValue = categoriaNombre,
                                       onValueChange = { categoriaSeleccionada ->
                                           categoriaId = categoriaSeleccionada.id
                                           categoriaNombre = categoriaSeleccionada.tipo
                                       },
                                       optionToString = { it.tipo }
                                   )


                                   Spacer(modifier = Modifier.padding(5.dp))

                                   DropdownChevere(
                                       label = "Proveedor:",
                                       options = listaProveedores,
                                       selectedValue = proveedorNombre,
                                       onValueChange = { proveedorSeleccionado ->
                                           proveedorId = proveedorSeleccionado.ruc
                                           proveedorNombre = proveedorSeleccionado.nombre
                                       },
                                       optionToString = { it.nombre }
                                   )

                                   Spacer(modifier = Modifier.padding(5.dp))

                                   // TextField Marca

                                   TextFieldChevere(
                                       value = marca,
                                       onValueChange = { marca = it },
                                       label = "Marca:",
                                       placeholder = "",
                                       imeAction = ImeAction.Next

                                   )


                                   Spacer(modifier = Modifier.padding(5.dp))

                                   // TextField Precio Compra/Venta

                                   Row(
                                       modifier = Modifier.fillMaxWidth(),
                                       horizontalArrangement = Arrangement.spacedBy(16.dp)
                                   ) {

                                       Column(modifier = Modifier.weight(1f)) { // Envuelve en una Column con weight
                                           TextFieldChevere(
                                               value = precioCompra,
                                               onValueChange = { precioCompra = it },
                                               label = "Precio de Compra:",
                                               placeholder = "S/0.00",
                                               imeAction = ImeAction.Next
                                           )
                                       }

                                       Column(modifier = Modifier.weight(1f)) { // Envuelve en una Column con weight
                                           TextFieldChevere(
                                               value = precioVenta, // Usa la variable de estado correcta
                                               onValueChange = {
                                                   precioVenta = it
                                               }, // Usa el onValueChange correcto
                                               label = "Precio de Venta:",
                                               placeholder = "S/0.00",
                                               imeAction = ImeAction.Next
                                           )
                                       }
                                   }


                                   Spacer(modifier = Modifier.padding(5.dp))

                                   // TextField Fecha de Vencimiento

                                   DatePickerField(
                                       label = "Fecha de Vencimiento:",
                                       selectedDate = FechaVencimiento,
                                       onDateSelected = { FechaVencimiento = it }
                                   )


                                   Spacer(modifier = Modifier.padding(5.dp))

                                   // TextField Stock Actual/Minimo

                                   Row(
                                       modifier = Modifier.fillMaxWidth(),
                                       horizontalArrangement = Arrangement.spacedBy(16.dp)
                                   ) {
                                       // Campo de texto para el Precio
                                       Column(modifier = Modifier.weight(1f)) { // Envuelve en una Column con weight
                                           TextFieldChevere(
                                               value = stockActual,
                                               onValueChange = { stockActual = it },
                                               label = "Stock Actual:",
                                               placeholder = "",
                                               imeAction = ImeAction.Next
                                           )
                                       }
                                       // Campo de texto para el Stock Actual
                                       Column(modifier = Modifier.weight(1f)) { // Envuelve en una Column con weight
                                           TextFieldChevere(
                                               value = stockMinimo, // Usa la variable de estado correcta
                                               onValueChange = {
                                                   stockMinimo = it
                                               }, // Usa el onValueChange correcto
                                               label = "Stock Minimo:",
                                               placeholder = " ",
                                               imeAction = ImeAction.Next
                                           )
                                       }
                                   }


                                   Spacer(modifier = Modifier.padding(5.dp))

                                   // TextField Codigo de Barras

                                   Row(
                                       modifier = Modifier.fillMaxWidth(),
                                       horizontalArrangement = Arrangement.spacedBy(8.dp),
                                       verticalAlignment = Alignment.Bottom
                                   ) {
                                       Column(
                                           modifier = Modifier.weight(6f)
                                       ) {
                                           TextFieldChevere(
                                               value = codigoBarra,
                                               onValueChange = { codigoBarra = it },
                                               label = "Codigo de Barras:",
                                               placeholder = " ",
                                               imeAction = ImeAction.Next
                                           )
                                       }
                                       Column(
                                           modifier = Modifier.weight(1f)
                                       ) {
                                           Column(
                                               Modifier
                                                   .size(50.dp)
                                                   .clip(shape = RoundedCornerShape(13.dp))
                                                   .background(color = Fondo1),
                                               verticalArrangement = Arrangement.Center,
                                               horizontalAlignment = Alignment.CenterHorizontally
                                           ) {
                                               Icon(
                                                   painter = painterResource(id = R.drawable.barcodescan), // Asegúrate de tener este icono
                                                   contentDescription = "Escanear Codigo de barras",
                                                   tint = VerdeLimon,
                                                   modifier = Modifier
                                                       .size(30.dp)
                                                       .clickable {
                                                           barcodeAnalyzer.startScanning()
                                                           mostrarScanner = true
                                                       }
                                               )
                                           }
                                       }
                                   }

                                   Spacer(modifier = Modifier.padding(50.dp))
                               }
                           }
                            // Botones
                            item {
                                Column (
                                    Modifier.padding(horizontal = 40.dp)
                                ){
                                    BotonChevere(
                                        texto = "Guardar Producto",
                                        colorFondo = VerdeLimon,
                                        colorTexto = Fondo1,
                                        onClick = {
                                            val uid = FirebaseAuth.getInstance().currentUser?.uid

                                            // 1. Validaciones en el cliente
                                            if (uid == null) {
                                                Toast.makeText(this@ProductInsertActivity,
                                                    "Error: Sesión de usuario no válida.", Toast.LENGTH_SHORT).show()
                                                return@BotonChevere
                                            }
                                            if (nombre.isBlank() || categoriaId == null || proveedorId == null || precioVenta.isBlank()) {
                                                Toast.makeText(this@ProductInsertActivity,
                                                    "Nombre, categoría, proveedor y precio de venta son obligatorios.", Toast.LENGTH_LONG).show()
                                                return@BotonChevere
                                            }

                                            // 2. Iniciar la corrutina para las operaciones de red
                                            lifecycleScope.launch {
                                                try {
                                                    // 3. Preparar el objeto ProductData
                                                    val productData =
                                                        Producto(
                                                            nombre = nombre,
                                                            precioVenta = precioVenta.toDoubleOrNull() ?: 0.0,
                                                            stockActual = stockActual.toIntOrNull() ?: 0,
                                                            stockMinimo = stockMinimo.toIntOrNull() ?: 0,
                                                            precioCompra = precioCompra.toDoubleOrNull() ?: 0.0,
                                                            fechaCad = FechaVencimiento, // Usando la variable de estado para la fecha
                                                            marca = marca,
                                                            codBarras = codigoBarra,
                                                            idProveedor = proveedorId!!,
                                                            idUsuario = uid,
                                                            idCategoria = categoriaId!!,
                                                            imagen = null
                                                        )

                                                    // 4. (FLUJO PASO 1) Insertar los datos del producto
                                                    val productResponse = productoService.insertProduct(productData)

                                                    if (productResponse.success && productResponse.idProducto != null) {
                                                        val nuevoIdProducto = productResponse.idProducto

                                                        // 5. (FLUJO PASO 2) Si hay una imagen, subirla
                                                        if (imagenUri != null) {
                                                            val nombreImagen = "${uid}_${nuevoIdProducto}.jpg"

                                                            val inputStream = contentResolver.openInputStream(imagenUri!!)
                                                            val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), inputStream!!.readBytes())
                                                            val body = MultipartBody.Part.createFormData("imagen", nombreImagen, requestFile)
                                                            val nombreArchivoBody = RequestBody.create("text/plain".toMediaTypeOrNull(), nombreImagen)

                                                            val uploadResponse = productoService.uploadImage(body, nombreArchivoBody)

                                                            if (uploadResponse.success) {
                                                                // 6. (FLUJO PASO 3) Actualizar la tabla con el nombre de la imagen
                                                                val updateRequest = ImageUpdateRequest(nuevoIdProducto, nombreImagen)
                                                                productoService.updateImagePath(updateRequest)
                                                            } else {
                                                                Toast.makeText(this@ProductInsertActivity, "Producto creado, pero falló la subida de imagen.", Toast.LENGTH_LONG).show()
                                                            }
                                                        }

                                                        Toast.makeText(this@ProductInsertActivity, "¡Producto guardado con éxito!", Toast.LENGTH_SHORT).show()
                                                        finish() // Cierra la actividad y vuelve

                                                    } else {
                                                        // Falló la inserción del producto
                                                        Toast.makeText(this@ProductInsertActivity, "Error al guardar: ${productResponse.message}", Toast.LENGTH_LONG).show()
                                                    }

                                                } catch (e: Exception) {
                                                    Log.e("GUARDAR_PRODUCTO", "Error en el proceso de guardado: ${e.message}", e)
                                                    Toast.makeText(this@ProductInsertActivity, "Error de conexión o datos inválidos.", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.padding(5.dp))
                                    BotonChevere(
                                        texto = "Otro Producto",
                                        colorFondo = xd,
                                        colorTexto = Color.White
                                    ) {

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun <T> DropdownChevere(
        label: String,
        options: List<T>,
        selectedValue: String,
        onValueChange: (T) -> Unit,
        optionToString: (T) -> String
    ) {
        var expanded by remember { mutableStateOf(false) }
        val rotationState by animateFloatAsState(
            targetValue = if (expanded) 180f else 0f,
            label = "Dropdown Arrow Rotation" // Etiqueta para herramientas de depuración
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = label,
                textAlign = TextAlign.Start,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                BasicTextField(
                    value = selectedValue,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .height(50.dp),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Start
                    )
                ) { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = selectedValue,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = remember { MutableInteractionSource() },
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        trailingIcon = { Icon(
                            imageVector = Icons.Default.ArrowDropDown ,
                            contentDescription = "Desplegar menú",
                            tint = VerdeLimon,
                            modifier = Modifier.rotate(rotationState)
                        ) },
                        container = {
                            OutlinedTextFieldDefaults.ContainerBox(
                                enabled = true,
                                isError = false,
                                interactionSource = remember { MutableInteractionSource() },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(13.dp),
                            )
                        }
                    )
                }
                // Este es el menú desplegable que aparece
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(
                                text = optionToString(option),
                                color = Color.Black)
                                },
                            onClick = {
                                onValueChange(option) // Actualiza el valor seleccionado
                                expanded = false      // Cierra el menú
                            }
                        )
                    }
                }
            }
        }
    }
}

