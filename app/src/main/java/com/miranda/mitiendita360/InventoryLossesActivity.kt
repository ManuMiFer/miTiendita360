package com.miranda.mitiendita360

import Perdida
import PerdidaHistorial
import PerdidaResponse
import TextFieldCantidadChevere
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.models.Categoria
import com.miranda.mitiendita360.models.ImageUpdateRequest
import com.miranda.mitiendita360.models.Producto
import com.miranda.mitiendita360.network.RetrofitClient
import com.miranda.mitiendita360.ui.components.ActionButton
import com.miranda.mitiendita360.ui.components.ActionButton2
import com.miranda.mitiendita360.ui.components.BotonChevere
import com.miranda.mitiendita360.ui.components.DatePickerField
import com.miranda.mitiendita360.ui.components.DatePickerField2
import com.miranda.mitiendita360.ui.components.DropdownChevere2
import com.miranda.mitiendita360.ui.components.TextAreaChevere
import com.miranda.mitiendita360.ui.components.TextFieldChevere2
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.Rojo
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class InventoryLossesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val productoService = RetrofitClient.productoService
            val perdidaService = RetrofitClient.perdidaService

            // --- ESTADOS EXISTENTES ---
            var listaProducto by remember { mutableStateOf(listOf<Producto>()) }
            var productoId by remember { mutableIntStateOf(0) }
            var productoNombre by remember { mutableStateOf("") }
            var cantidadPerdida by remember { mutableIntStateOf(1) }
            var stockDisponible by remember { mutableIntStateOf(0) }
            var fecha by remember { mutableStateOf("") }

            // --- NUEVOS ESTADOS PARA EL FORMULARIO ---
            val razones = listOf("Vencimiento", "Deterioro", "Robo", "Otro")
            var razonPerdida by remember { mutableStateOf("") }
            var detalles by remember { mutableStateOf("") }
            var evidenciaUri by remember { mutableStateOf<Uri?>(null) }

            // --- LAUNCHER PARA SELECCIONAR IMAGEN ---
            val galeriaLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
                onResult = { uri -> if (uri != null) evidenciaUri = uri }
            )
            var historialPerdidas by remember { mutableStateOf<List<PerdidaHistorial>>(emptyList()) }

            // --- MODIFICAR LaunchedEffect ---
            // Usaremos una clave que cambie para forzar la recarga
            var recargarHistorial by remember { mutableIntStateOf(0) }

            LaunchedEffect(key1 = recargarHistorial) { // <--- La clave ahora es una variable
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid == null) { /* ... */ return@LaunchedEffect }

                // Corrutina para cargar productos (esta ya la tienes)
                lifecycleScope.launch { /* ... tu código para cargar productos ... */ }

                // --- NUEVA CORRUTINA PARA CARGAR EL HISTORIAL ---
                lifecycleScope.launch {
                    try {
                        val response = perdidaService.getPerdidas(uid)
                        if (response.success) {
                            historialPerdidas = response.data
                        } else {
                            Log.e("HISTORIAL_PERDIDAS", "API falló: ${response.message}")
                        }
                    } catch (e: Exception) {
                        Log.e("HISTORIAL_PERDIDAS", "Error de red: ${e.message}", e)
                    }
                }
            }
            LaunchedEffect(key1 = true) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid == null) {
                    Log.e("API_ERROR", "Usuario no autenticado.")
                    // Opcional: mostrar un Toast
                    // Toast.makeText(this@InventoryLossesActivity, "Error de sesión", Toast.LENGTH_SHORT).show()
                    return@LaunchedEffect
                }

                lifecycleScope.launch {
                    try {
                        // Llama al servicio para obtener los productos del usuario
                        // Asumo que tu endpoint `getProductos` puede recibir el ID de usuario y una query vacía
                        val response = productoService.getProducts(uid, "", "",1)
                        if (response.success && response.data != null) {
                            listaProducto = response.data
                            Log.d("API_SUCCESS", "Productos cargados: ${listaProducto.size}")
                        } else {
                            Log.e("API_ERROR", "La API respondió con un error: ${response.message}")
                        }
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Excepción al cargar productos: ${e.message}", e)
                        // Opcional: mostrar un Toast al usuario
                        // Toast.makeText(this@InventoryLossesActivity, "Error al cargar productos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            MiTiendita360Theme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Box {

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .background(color = Fondo1)
                                    .padding(top = 45.dp, start = 25.dp, end = 25.dp)
                            ) {
                                Row (
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.fillMaxWidth()
                                ){
                                    Image(
                                        painterResource(R.drawable.losses),
                                        contentDescription = "",
                                        colorFilter = ColorFilter.tint(color = Color.White),
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(5.dp))
                                    Column {
                                        Text(
                                            "Registro",
                                            color = Color.White,
                                            fontSize = 25.sp
                                        )
                                        Text(
                                            "de Perdidas",
                                            color = Color.White,
                                            fontSize = 25.sp
                                        )
                                    }
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .padding(start = 25.dp, top = 35.dp)
                            ) {
                                Image(
                                    imageVector = (Icons.Default.ArrowBackIos),
                                    contentDescription = "",
                                    colorFilter = ColorFilter.tint(color = Color.White),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(35.dp)
                                        .clickable {
                                            finish()
                                        }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    LazyColumn(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(Fondo1)
                        .padding(25.dp)
                    )
                    {
                        item {
                            Column (
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(GrisClaro)
                                    .padding(20.dp)
                            ){
                                DropdownChevere2(
                                    label = "Producto",
                                    options = listaProducto,
                                    selectedValue = productoNombre,
                                    onValueChange = { productoSeleccionado ->
                                        productoId = productoSeleccionado.id!!
                                        productoNombre = productoSeleccionado.nombre
                                        // --- LÍNEAS CLAVE AÑADIDAS ---
                                        stockDisponible = productoSeleccionado.stockActual // Actualiza el stock máximo
                                        cantidadPerdida = if (productoSeleccionado.stockActual > 0) 1 else 0 // Resetea la cantidad
                                    },
                                    optionToString = { it.nombre},
                                    color = Fondo1,
                                    colorFlecha = Color.White// Deshabilita si no hay productos cargados
                                )

                                Spacer(modifier = Modifier.padding(5.dp))


                                Row (
                                    modifier = Modifier.fillMaxWidth(),     // 2. Solución al botón aplanado: Alinear todo en la parte inferior.
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ){
                                    Column(modifier = Modifier.weight(1f)) {
                                        TextFieldCantidadChevere(
                                            value = cantidadPerdida,
                                            onValueChange = { cantidadPerdida = it },
                                            label = "Cantidad:",
                                            stockDisponible = stockDisponible,
                                            color = Fondo1,
                                        )
                                    }

                                    // 1. Solución a la altura del DatePicker: El weight va en la Column.
                                    Column(modifier = Modifier.weight(1f)){
                                        Text("Fecha Perdida",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.padding(2.dp))
                                        DatePickerField2(
                                            selectedDate = fecha,
                                            onDateSelected = {fecha = it},
                                            placeholderText = "",
                                            containerColor = Fondo1,
                                            contentColor = Color.White,
                                            selectedTextColor = Color.White,
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.padding(5.dp))

                                Row (
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ){
                                    Column (modifier = Modifier.weight(1f)){
                                        DropdownChevere2(
                                            label = "Razón de Pérdida",
                                            options = razones, // <-- CORRECTO: Usa la lista de razones
                                            selectedValue = razonPerdida, // <-- CORRECTO: Usa el estado de razón
                                            onValueChange = { razonPerdida = it }, // <-- CORRECTO: Actualiza la razón seleccionada
                                            optionToString = { it }, // Muestra el String directamente
                                            color = Fondo1,
                                            colorFlecha = Color.White// Se habilita al seleccionar un producto
                                        )
                                    }
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(50.dp) // Tamaño del botón
                                            .clip(CircleShape) // <-- La clave para el diseño circular
                                            .background(VerdeLimon) // <-- Color de fondo del botón
                                            .clickable {
                                                // La ACCIÓN: Simple y directa
                                                galeriaLauncher.launch("image/*")
                                            }
                                    ) {
                                        // La LÓGICA DE LA UI: qué mostrar dentro del círculo
                                        if (evidenciaUri == null) {
                                            // Si no hay imagen, muestra el icono
                                            Icon(
                                                imageVector = Icons.Default.Upload,
                                                contentDescription = "Subir Evidencia",
                                                tint = Color.White, // <-- Color del icono
                                                modifier = Modifier.size(40.dp) // Tamaño del icono
                                            )
                                        } else {
                                            // Si hay imagen, muéstrala, ocupando todo el círculo
                                            AsyncImage(
                                                model = evidenciaUri,
                                                contentDescription = "Evidencia seleccionada",
                                                modifier = Modifier.fillMaxSize(), // Rellena el círculo
                                                contentScale = ContentScale.Crop // Recorta para encajar
                                            )
                                        }
                                    }
                                }

                                TextAreaChevere(
                                    value = detalles,
                                    onValueChange = {detalles = it},
                                    label = "Detalles Adicionales",
                                    placeholder = "",
                                    imeAction = ImeAction.Done,
                                    keyboarType = KeyboardType.Text,
                                    enabled = true,
                                    color = Fondo1,
                                    minHeight = 70.dp
                                )

                                Spacer(modifier = Modifier.padding(10.dp))

                                BotonChevere(
                                    texto = "Registrar",
                                    colorFondo = VerdeLimon,
                                    colorTexto = Fondo1,
                                    onClick = {
                                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                                        // --- VALIDACIONES ---
                                        if (uid == null) {
                                            Toast.makeText(this@InventoryLossesActivity, "Error de sesión.", Toast.LENGTH_SHORT).show()
                                            return@BotonChevere
                                        }
                                        if (productoId == 0 || fecha.isBlank() || razonPerdida.isBlank()) {
                                            Toast.makeText(this@InventoryLossesActivity, "Producto, fecha y razón son obligatorios.", Toast.LENGTH_LONG).show()
                                            return@BotonChevere
                                        }
                                        if (cantidadPerdida > stockDisponible || cantidadPerdida <= 0) {
                                            Toast.makeText(this@InventoryLossesActivity, "La cantidad perdida debe ser válida y no mayor al stock.", Toast.LENGTH_LONG).show()
                                            return@BotonChevere
                                        }

                                        lifecycleScope.launch {
                                            var response: PerdidaResponse? = null // Declara la variable aquí, como opcional

                                            try {
                                                // --- PREPARAR DATOS ---
                                                val partesFecha = fecha.split("/")
                                                val fechaFormatoApi = if (partesFecha.size == 3) {
                                                    "${partesFecha[2]}-${partesFecha[1]}-${partesFecha[0]}"
                                                } else {
                                                    fecha
                                                }

                                                val dataMap = mutableMapOf<String, RequestBody>()
                                                dataMap["id_producto"] = productoId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                                dataMap["cantidad"] = cantidadPerdida.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                                dataMap["fecha"] = fechaFormatoApi.toRequestBody("text/plain".toMediaTypeOrNull())
                                                dataMap["razon"] = razonPerdida.toRequestBody("text/plain".toMediaTypeOrNull())
                                                dataMap["detalles"] = (detalles ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
                                                dataMap["id_usuario"] = uid.toRequestBody("text/plain".toMediaTypeOrNull())

                                                var imagePart: MultipartBody.Part? = null
                                                if (evidenciaUri != null) {
                                                    val inputStream = contentResolver.openInputStream(evidenciaUri!!)
                                                    val requestFile = inputStream!!.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
                                                    imagePart = MultipartBody.Part.createFormData("imagen", "evidencia.jpg", requestFile)
                                                }

                                                // --- LLAMADA ÚNICA A LA API ---
                                                response = perdidaService.registrarPerdidaUnificada(imagePart, dataMap)

                                                if (response?.success == true) {
                                                    Toast.makeText(this@InventoryLossesActivity, "¡Pérdida registrada!", Toast.LENGTH_SHORT).show()
                                                    // Limpiar el formulario INMEDIATAMENTE después del éxito
                                                    productoId = 0
                                                    productoNombre = ""
                                                    cantidadPerdida = 1
                                                    stockDisponible = 0
                                                    fecha = ""
                                                    razonPerdida = ""
                                                    detalles = ""
                                                    evidenciaUri = null

                                                    // Forzar la recarga del historial
                                                    recargarHistorial++

                                                } else {
                                                    Toast.makeText(this@InventoryLossesActivity, "Error: ${response?.message}", Toast.LENGTH_LONG).show()
                                                }

                                            } catch (e: Exception) {
                                                Log.e("REGISTRO_PERDIDA", "Excepción CRÍTICA: ${e.message}", e)
                                                Toast.makeText(this@InventoryLossesActivity, "Error de conexión o del servidor.", Toast.LENGTH_LONG).show()
                                            }
                                            // Ya no necesitamos el bloque 'finally' para esta lógica
                                        }
                                    }
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.padding(10.dp))
                            Text(
                            "Historial Reciente",
                            color = Color.White,
                            fontSize = 30.sp
                            )
                            Spacer(modifier = Modifier.padding(10.dp))
                        }
                        // --- NUEVA LÓGICA DE LISTADO ---
                        if (historialPerdidas.isEmpty()) {
                            item {
                                Text(
                                    text = "No hay pérdidas registradas.",
                                    color = GrisClaro,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(historialPerdidas) { perdida ->
                                PerdidaCard(perdida = perdida)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PerdidaCard(perdida: PerdidaHistorial) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(GrisClaro)
            .padding(10.dp),
    ) {
        // Indicador de color (si quieres)
        Column(
            Modifier
                .height(56.dp)
                .width(10.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(color = Rojo)
        ) {}
        AsyncImage(
            model = "${BuildConfig.API_BASE_URL}imagenes/${perdida.ruta_imagen}",
            contentDescription = "Imagen de la pérdida",
            modifier = Modifier.size(50.dp)
                .clip(RoundedCornerShape(13.dp)),
            contentScale = ContentScale.Crop
        )

        // Columna con Nombre y Razón
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = perdida.nombre_producto,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis // Para nombres largos
            )
            Text(
                text = "Razón: ${perdida.razon}",
                color = Color.White,
                fontSize = 15.sp
            )
        }

        // Columna con Cantidad y Fecha
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${perdida.cantidad} Unidades",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = perdida.fecha, // La fecha ya viene formateada desde la BD
                color = Color.White,
                fontSize = 15.sp
            )
        }
    }
}