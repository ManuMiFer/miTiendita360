package com.miranda.mitiendita360

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.models.Proveedor
import com.miranda.mitiendita360.network.ProveedorService
import com.miranda.mitiendita360.network.RetrofitClient
import com.miranda.mitiendita360.ui.components.BotonChevere
import com.miranda.mitiendita360.ui.components.TextFieldChevere
import com.miranda.mitiendita360.ui.components.TextFieldChevere2
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import kotlinx.coroutines.launch
import kotlin.getValue

class SupplierInsertActivity : ComponentActivity() {

    private val proveedorService by lazy {
        RetrofitClient.instance.create(ProveedorService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusRequester
            val telefonoFocusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current
            val focusManager = LocalFocusManager.current
            var uid = FirebaseAuth.getInstance().currentUser
            var ruc by remember { mutableStateOf("") }
            var nombre by remember { mutableStateOf("") }
            var direccion by remember { mutableStateOf("") }
            var telefono by remember { mutableStateOf("") }
            var distrito by remember { mutableStateOf("") }
            var provincia by remember { mutableStateOf("") }
            var departamento by remember { mutableStateOf("") }
            var razonSocial by remember { mutableStateOf("") }

            LaunchedEffect(ruc) {
                // 1. Verificamos que el RUC tenga exactamente 11 dígitos.
                if (ruc.length == 11) {
                    Toast.makeText(this@SupplierInsertActivity, "Buscando RUC...", Toast.LENGTH_SHORT).show()

                    // 2. Construimos la URL completa y la clave de la API.
                    val urlCompleta = "https://api.decolecta.com/v1/sunat/ruc?numero=$ruc"
                    val apiKey = "Bearer sk_11327.EGtAwzG0M7HOweYRoJzlOhQsBRTYP9Xq"

                    // 3. Usamos el scope de la Activity para lanzar la llamada de red.
                    lifecycleScope.launch {
                        try {
                            // 4. Llamamos a la función del servicio con la URL y la clave.
                            val response = proveedorService.getSunatInfo(urlCompleta, apiKey)

                            // 5. Actualizamos los campos de la UI con los datos recibidos.
                            // El operador '?:' (elvis) mantiene el valor actual si la API devuelve nulo.
                            razonSocial = response.razonSocial ?: razonSocial
                            direccion = response.direccion ?: direccion
                            distrito = response.distrito ?: distrito
                            provincia = response.provincia ?: provincia
                            departamento = response.departamento ?: departamento

                            // Asignamos la Razón Social al Nombre comercial por defecto.
                            nombre = response.razonSocial ?: nombre

                            Toast.makeText(this@SupplierInsertActivity, "Datos de RUC encontrados.", Toast.LENGTH_SHORT).show()

                        } catch (e: Exception) {
                            // 6. Manejamos cualquier error de red o de la API.
                            Log.e("SUNAT_API_ERROR", "Fallo al obtener datos del RUC: ${e.message}", e)
                            Toast.makeText(this@SupplierInsertActivity, "RUC no encontrado o error de red.", Toast.LENGTH_LONG).show()
                        }
                    }
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
                                .height(200.dp)
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
                                        "Registrar",
                                        color = Color.White,
                                        fontSize = 25.sp
                                    )
                                    Text(
                                        "Proveedor",
                                        color = Color.White,
                                        fontSize = 25.sp,
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    LazyColumn  (
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.White)
                            .padding(innerPadding)

                    ) {
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
                            ){
                                TextFieldChevere2(
                                    value = ruc,
                                    onValueChange = { ruc = it },
                                    label = "Ruc:",
                                    placeholder = "",
                                    imeAction = ImeAction.Next,
                                    enabled = true,
                                    color = Color.White,
                                    keyboarType = KeyboardType.Number,
                                    keyboardActions = KeyboardActions(
                                        onNext = {
                                            telefonoFocusRequester.requestFocus()
                                        }
                                    )
                                )

                                Spacer(modifier = Modifier.padding(5.dp))


                                TextFieldChevere2(
                                    value = razonSocial,
                                    onValueChange = { razonSocial = it },
                                    label = "Razón Social:",
                                    placeholder = "",
                                    imeAction = ImeAction.Next,
                                    enabled = true,
                                    color = Color.White,
                                    keyboarType = KeyboardType.Text
                                )

                                Spacer(modifier = Modifier.padding(5.dp))

                                TextFieldChevere2(
                                    value = direccion,
                                    onValueChange = { direccion = it },
                                    label = "Dirección:",
                                    placeholder = "",
                                    imeAction = ImeAction.Next,
                                    enabled = true,
                                    color = Color.White,
                                    keyboarType = KeyboardType.Text
                                )


                                Spacer(modifier = Modifier.padding(5.dp))

                                Row (
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ){
                                    Column(modifier = Modifier.weight(1f)){
                                        TextFieldChevere2(
                                            value = departamento,
                                            onValueChange = { departamento = it },
                                            label = "Departamento:",
                                            placeholder = "",
                                            imeAction = ImeAction.Next,
                                            enabled = true,
                                            color = Color.White,
                                            keyboarType = KeyboardType.Text
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)){
                                        TextFieldChevere2(
                                            value = provincia,
                                            onValueChange = { provincia = it },
                                            label = "Provincia:",
                                            placeholder = "",
                                            imeAction = ImeAction.Next,
                                            enabled = true,
                                            color = Color.White,
                                            keyboarType = KeyboardType.Text
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.padding(5.dp))

                                TextFieldChevere2(
                                    value = distrito,
                                    onValueChange = { distrito = it },
                                    label = "Distrito:",
                                    placeholder = "",
                                    imeAction = ImeAction.Next,
                                    enabled = true,
                                    color = Color.White,
                                    keyboarType = KeyboardType.Text
                                )

                                Spacer(modifier = Modifier.padding(5.dp))

                                TextFieldChevere2(
                                    value = telefono,
                                    onValueChange = {  telefono = it },
                                    label = "Celular:",
                                    placeholder = "",
                                    imeAction = ImeAction.Done,
                                    enabled = true,
                                    color = Color.White,
                                    keyboarType = KeyboardType.Number,
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            keyboardController?.hide()
                                            focusManager.clearFocus() // Opcional: limpia el foco
                                        }
                                    ),
                                    modifier = Modifier.focusRequester(telefonoFocusRequester)

                                )
                                Spacer(modifier = Modifier.padding(70.dp))
                            }
                            Column(
                                Modifier.padding(horizontal = 40.dp)
                            ) {
                                BotonChevere(
                                    texto = "Guardar Proveedor",    colorFondo = VerdeLimon,
                                    colorTexto = GrisClaro,
                                ){
                                    val idUsuario = uid?.uid

                                    val esRucValido = ruc.length == 11 && (ruc.startsWith("10") || ruc.startsWith("20"))
                                    // 3. Añadimos la validación para el celular
                                    val esTelefonoValido = telefono.isBlank() || (telefono.length == 9 && telefono.startsWith("9"))

                                    if (idUsuario == null) {
                                        Toast.makeText(this@SupplierInsertActivity, "Error de autenticación.", Toast.LENGTH_SHORT).show()
                                    } else if (!esRucValido) {
                                        Toast.makeText(this@SupplierInsertActivity, "Por favor, ingrese un RUC válido.", Toast.LENGTH_LONG).show()
                                    } else if (razonSocial.isBlank()) {
                                        Toast.makeText(this@SupplierInsertActivity, "La Razón Social es obligatoria.", Toast.LENGTH_SHORT).show()
                                    } else if (!esTelefonoValido) {
                                        // 4. Mensaje de error si el celular es incorrecto
                                        Toast.makeText(this@SupplierInsertActivity, "El celular debe tener 9 dígitos y empezar con 9.", Toast.LENGTH_LONG).show()
                                    } else {

                                        val proveedor = Proveedor(
                                            ruc = ruc,
                                            telefono = telefono,
                                            direccion = direccion,
                                            idUsuario = idUsuario,
                                            razonSocial = razonSocial,
                                            distrito = distrito,
                                            provincia = provincia,
                                            departamento = departamento
                                        )

                                        lifecycleScope.launch {
                                            try {
                                                // 1. Se intenta insertar el proveedor.
                                                val response =
                                                    proveedorService.insertSupplier(proveedor)

                                                // 2. Si el servidor responde con 'success: true', la operación fue exitosa.
                                                if (response.success) {
                                                    Toast.makeText(
                                                        this@SupplierInsertActivity,
                                                        "Proveedor guardado con éxito",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    val intent = Intent(
                                                        this@SupplierInsertActivity,
                                                        MenuActivity::class.java
                                                    ).apply {
                                                        flags =
                                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    }
                                                    startActivity(intent)
                                                    finish()
                                                } else {
                                                    // 3. Si el servidor responde con 'success: false', muestra el mensaje de error específico.
                                                    //    Este es el caso para validaciones como "Datos incompletos".
                                                    Toast.makeText(
                                                        this@SupplierInsertActivity,
                                                        "Fallo al guardar: ${response.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }

                                            } catch (e: Exception) {
                                                // 4. Si la llamada de red falla (ej. error 500 por RUC duplicado), entra aquí.
                                                Log.e(
                                                    "SUPPLIER_INSERT_ERROR",
                                                    "Fallo en la llamada de red",
                                                    e
                                                )

                                                // 5. Verificamos si es un error HTTP para poder leer el mensaje del servidor.
                                                if (e is retrofit2.HttpException) {
                                                    try {
                                                        // Intenta leer el cuerpo del error que envía el PHP.
                                                        val errorBody =
                                                            e.response()?.errorBody()?.string()
                                                        if (!errorBody.isNullOrEmpty()) {
                                                            val gson = com.google.gson.Gson()
                                                            val errorResponse = gson.fromJson(
                                                                errorBody,
                                                                com.miranda.mitiendita360.network.SimpleApiResponse::class.java
                                                            )
                                                            // Muestra el mensaje específico, como "Error: El RUC ya está registrado".
                                                            Toast.makeText(
                                                                this@SupplierInsertActivity,
                                                                errorResponse.message,
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        } else {
                                                            // Si no hay mensaje, muestra el código de error.
                                                            Toast.makeText(
                                                                this@SupplierInsertActivity,
                                                                "Error del servidor (HTTP ${e.code()})",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                    } catch (jsonError: Exception) {
                                                        Log.e(
                                                            "SUPPLIER_INSERT_ERROR",
                                                            "Fallo al decodificar JSON de error",
                                                            jsonError
                                                        )
                                                        Toast.makeText(
                                                            this@SupplierInsertActivity,
                                                            "Respuesta del servidor no válida.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                } else {
                                                    // 6. Para otros errores (ej. sin conexión a internet), muestra un mensaje genérico.
                                                    Toast.makeText(
                                                        this@SupplierInsertActivity,
                                                        "Error de red. Revisa tu conexión.",
                                                        Toast.LENGTH_LONG
                                                    ).show()
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