package com.miranda.mitiendita360

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.models.Cliente
import com.miranda.mitiendita360.models.ClienteRequest
import com.miranda.mitiendita360.network.ReniecApiService
import com.miranda.mitiendita360.network.RetrofitClient
import com.miranda.mitiendita360.ui.components.BotonChevere
import com.miranda.mitiendita360.ui.components.DropdownChevereBasico
import com.miranda.mitiendita360.ui.components.SearchTextField2
import com.miranda.mitiendita360.ui.components.SearchableDropdown
import com.miranda.mitiendita360.ui.components.TextAreaChevere
import com.miranda.mitiendita360.ui.components.TextFieldChevere
import com.miranda.mitiendita360.ui.components.TextFieldChevereBasico
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.GrisClaro2
import com.miranda.mitiendita360.ui.theme.Lila
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.Rojito
import com.miranda.mitiendita360.ui.theme.Verde
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import com.miranda.mitiendita360.ui.theme.Verdecito
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.Serializable
import java.text.SimpleDateFormat
import kotlin.text.format
import kotlin.text.map
import kotlin.text.sumOf

data class BoletaItem(
    val cantidad: Int,
    val descripcion: String,
    val precioUnitario: Double,
    val subtotal: Double
) : Serializable
class SaleInsertActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val cartItems = intent.getParcelableArrayListExtra<CartItem>("CART_ITEMS") ?: arrayListOf()
        val totalVenta = intent.getDoubleExtra("TOTAL_VENTA", 0.0)

        // 2. Verificación (opcional pero recomendada)
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Error: No se recibieron productos.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        setContent {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            val saleViewModel: SaleViewModel = viewModel()
            val saleResult by saleViewModel.saleResult.collectAsState()
            var selectedCliente by remember { mutableStateOf<Cliente?>(null) }

            val clienteViewModel: ClienteViewModel = viewModel()

            LaunchedEffect(saleResult) {
                when (val result = saleResult) {
                    is SaleResult.Success -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                        saleViewModel.resetResult() // Limpia el estado para futuras operaciones
                        // Cierra la actividad y vuelve a la pantalla anterior

                        val itemsParaBoleta = cartItems.map { cartItem ->
                            BoletaItem(
                                cantidad = cartItem.quantity,
                                descripcion = cartItem.producto.nombre, // <-- Ahora sí podemos usarlo
                                precioUnitario = cartItem.producto.precioVenta,
                                subtotal = cartItem.quantity * cartItem.producto.precioVenta
                            )
                        }
                        val totalFinal = cartItems.sumOf { it.quantity * it.producto.precioVenta }
                        val fechaActual = SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(java.util.Date())

                        val intent = Intent(context, PaymentSlipMainActivity::class.java).apply {
                            putExtra("CLIENTE_NOMBRE", selectedCliente?.nombre ?: "Cliente Varios")
                            putExtra("FECHA_VENTA", fechaActual)
                            putExtra("ITEMS_VENTA", ArrayList(itemsParaBoleta))
                            putExtra("TOTAL_VENTA", totalFinal)
                        }
                        context.startActivity(intent)
                        (context as? android.app.Activity)?.finish()
                    }
                    is SaleResult.Error -> {
                        Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                        saleViewModel.resetResult() // Limpia el estado para poder reintentar
                    }
                    is SaleResult.Loading -> {
                        Toast.makeText(context, "Registrando venta...", Toast.LENGTH_SHORT).show()
                    }
                    SaleResult.Idle -> { }
                }
            }
            // 2. Recoge los datos del ViewModel como estados
            val clientes by clienteViewModel.clientes
            val isLoading by clienteViewModel.isLoading
            val error by clienteViewModel.error

            // 3. Estados para el dropdown
            var searchQuery by remember { mutableStateOf("") }

            // Estado para el TEXTO que se MUESTRA en el campo
            var textoDelCampo by remember { mutableStateOf("") }



            // La lista se filtra usando el 'searchQuery'
            val clientesFiltrados = remember(searchQuery, clientes) {
                if (searchQuery.isBlank()) {
                    clientes
                } else {
                    clientes.filter {
                        val nombreCompleto = "${it.nombre} ${it.apellidop} ${it.apellidom}"
                        nombreCompleto.contains(searchQuery, ignoreCase = true) ||
                                it.dni.contains(searchQuery, ignoreCase = true)
                    }
                }
            }
            var metodoSelecionado by remember { mutableStateOf("Efectivo") }
            val ListaMetodos = listOf("Efectivo", "Yape","Multiple")
            var monto by remember { mutableStateOf(totalVenta) }
            var yape by remember { mutableStateOf("") }
            var ultimoCampoEditado by remember { mutableStateOf<String?>(null) }
            var efectivo by remember { mutableStateOf("") }
            var nuevoCliente by remember { mutableStateOf("") }

            LaunchedEffect(yape, efectivo) {
                if (metodoSelecionado == "Multiple") {
                    val yapeDouble = yape.toDoubleOrNull() ?: 0.0
                    val efectivoDouble = efectivo.toDoubleOrNull() ?: 0.0

                    if (ultimoCampoEditado == "yape") {
                        if (yapeDouble <= totalVenta) {
                            val restante = totalVenta - yapeDouble
                            // Actualizamos 'efectivo' sin disparar un nuevo cambio en 'yape'
                            efectivo = String.format("%.2f", restante)
                        }
                    }
                    else if (ultimoCampoEditado == "efectivo") {
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
                    ultimoCampoEditado = null
                }
            }
            MiTiendita360Theme {
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
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.padding(5.dp))
                                Column {
                                    Text(
                                        "Finalizar",
                                        color = Color.White,
                                        fontSize = 25.sp
                                    )
                                    Text(
                                        "Venta",
                                        color = Color.White,
                                        fontSize = 25.sp
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Column (
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .background(Fondo1)
                            .padding(20.dp)
                    ){
                        Text(text = "Cliente:",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column (
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(GrisClaro)
                        ){
                            SearchableDropdown<Cliente>(
                                options = clientesFiltrados,onValueChange = { clienteSeleccionado ->
                                    selectedCliente = clienteSeleccionado
                                    // Cuando seleccionas, el estado se actualiza a SOLO el DNI.
                                    searchQuery = clienteSeleccionado.dni
                                },
                                optionToString = { cliente ->
                                    // En la lista se muestra el nombre largo y DNI. Correcto.
                                    "${cliente.nombre} ${cliente.apellidop} ${cliente.apellidom} DNI: ${cliente.dni}"
                                },
                                // El campo muestra el valor de 'searchQuery'. Correcto.
                                searchQuery = searchQuery,
                                onSearchQueryChange = { nuevaQuery ->
                                    // Cuando escribes, actualizas 'searchQuery'. Correcto.
                                    searchQuery = nuevaQuery
                                    selectedCliente = null
                                },
                                placeholder = "Buscar cliente por DNI o nombre",
                                color = GrisClaro,
                                colorFlecha = Fondo1,
                                colorTexto = GrisClaro2
                            )

                            HorizontalDivider(
                                thickness = 1.dp,        // Grosor de la línea
                                color = GrisClaro2,
                                modifier = Modifier.padding(horizontal = 10.dp)        // Color de la línea, puedes usar el que prefieras
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(
                                    modifier = Modifier.weight(6f)
                                ) {
                                    TextFieldChevereBasico(
                                        value = nuevoCliente,
                                        onValueChange = {nuevoCliente = it},
                                        placeholder = "Añadir nuevo Cliente",
                                        imeAction = ImeAction.Next,
                                        keyboarType = KeyboardType.Number,
                                        enabled = true ,
                                        color = GrisClaro,
                                        colorTexto = GrisClaro2
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(50.dp)
                                            .aspectRatio(1f)
                                            .clip(shape = RoundedCornerShape(13.dp))
                                            .clickable {
                                                scope.launch {
                                                    val dni = nuevoCliente
                                                    // 1. Validar DNI
                                                    if (dni.length != 8 || !dni.all { it.isDigit() }) {
                                                        Toast.makeText(
                                                            context,
                                                            "Por favor, ingrese un DNI válido de 8 dígitos.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        return@launch
                                                    }

                                                    try {
                                                        // --- 2. CREACIÓN DE RETROFIT "AL VUELO" PARA RENIEC ---
                                                        val reniecApi: ReniecApiService =
                                                            Retrofit.Builder()
                                                                .baseUrl("https://api.decolecta.com/v1/reniec/dni/")
                                                                .addConverterFactory(
                                                                    GsonConverterFactory.create()
                                                                )
                                                                .build()
                                                                .create(ReniecApiService::class.java)

                                                        // 3. Consultar API de Reniec
                                                        val reniecResponse =
                                                            reniecApi.consultarDni(dni)

                                                        // --- INICIO DE LA CORRECCIÓN ---
                                                        if (reniecResponse.isSuccessful && reniecResponse.body() != null) {
                                                            // --- FIN DE LA CORRECCIÓN ---
                                                            val data = reniecResponse.body()!!
                                                            val userId =
                                                                FirebaseAuth.getInstance().currentUser?.uid

                                                            if (userId == null) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Error: Usuario no autenticado.",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                return@launch
                                                            }

                                                            // 4. Preparar los datos para nuestro servidor
                                                            val clienteRequest = ClienteRequest(
                                                                dni = data.dni,
                                                                nombre = data.nombres,
                                                                apellidop = data.apellidoPaterno,
                                                                apellidom = data.apellidoMaterno,
                                                                idUsuario = userId
                                                            )

                                                            // 5. Llamar a nuestro script PHP para registrar
                                                            val registroResponse =
                                                                RetrofitClient.clienteService.registrarCliente(
                                                                    clienteRequest
                                                                )
                                                            if (registroResponse.isSuccessful) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Cliente '${data.nombres}' registrado.",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                                nuevoCliente = ""
                                                                clienteViewModel.getClientes()
                                                            } else {
                                                                val errorMsg =
                                                                    registroResponse.errorBody()
                                                                        ?.string()
                                                                        ?: "Error al registrar."
                                                                Toast.makeText(
                                                                    context,
                                                                    "Error: $errorMsg",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }

                                                        } else {
                                                            // --- CORRECCIÓN DEL MENSAJE DE ERROR ---
                                                            // Si la llamada no fue exitosa, el error está en errorBody()
                                                            val errorMsg =
                                                                reniecResponse.errorBody()?.string()
                                                                    ?: "DNI no encontrado."
                                                            Toast.makeText(
                                                                context,
                                                                "Error API Reniec: $errorMsg",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }

                                                    } catch (e: Exception) {
                                                        Toast.makeText(
                                                            context,
                                                            "Error de red: ${e.message}",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Si no hay imagen seleccionada, muestra el ícono
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Subir Imagen",
                                            tint = Fondo1,
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(shape = CircleShape)
                                                .background(color = VerdeLimon)
                                                .padding(5.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))


                        Text(text = "Metodo:",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ){
                            Column (Modifier.weight(1f)){
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
                            if (metodoSelecionado == "Multiple"){
                                Row(
                                    Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    // --- CAMPO DE YAPE CORREGIDO ---
                                    Column (Modifier.weight(1f)){
                                        TextFieldChevereBasico(
                                            value = yape, // <-- CORRECCIÓN 1: Usa 'yape' directamente
                                            onValueChange = {
                                                yape = it
                                                ultimoCampoEditado = "yape"
                                            },
                                            placeholder = "Y",
                                            imeAction = ImeAction.Done,
                                            keyboarType = KeyboardType.Decimal,
                                            enabled = true ,
                                            color = GrisClaro,
                                            colorTexto = GrisClaro2
                                        )
                                    }
                                    // --- CAMPO DE EFECTIVO CORREGIDO ---
                                    Column (Modifier.weight(1f)){
                                        TextFieldChevereBasico(
                                            value = efectivo, // <-- CORRECCIÓN 2: Usa 'efectivo' directamente
                                            onValueChange = {
                                                efectivo = it
                                                ultimoCampoEditado = "efectivo"
                                            },
                                            placeholder = "E",
                                            imeAction = ImeAction.Done,
                                            keyboarType = KeyboardType.Decimal,
                                            enabled = true ,
                                            color = GrisClaro,
                                            colorTexto = GrisClaro2
                                        )
                                    }
                                }
                            } else {
                                // --- CAMPO DE MONTO ÚNICO CORREGIDO ---
                                Column (Modifier.weight(1f)){
                                    TextFieldChevereBasico(
                                        value = "S/ ${"%.2f".format(totalVenta)}",
                                        onValueChange = { /* NO HACER NADA */ }, // <-- CORRECCIÓN 3
                                        placeholder = "S/0.00",
                                        imeAction = ImeAction.Done,
                                        keyboarType = KeyboardType.Decimal,
                                        enabled = false ,
                                        color = GrisClaro,
                                        colorTexto = GrisClaro2
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        HorizontalDivider(
                            thickness = 1.dp,        // Grosor de la línea
                            color = Color.White        // Color de la línea, puedes usar el que prefieras
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            Text(
                                text = "Subtotal",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "S/ ${"%.2f".format(totalVenta)}",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            Text(
                                text = "Descuentos",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "S/ 0.00",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Verdecito.copy(alpha = 0.2f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            Text(
                                text = "Total",
                                color = Verdecito,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "S/ ${"%.2f".format(totalVenta)}",
                                color = Verdecito,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Column (
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.End)
                        ){
                            BotonChevere(
                                texto = "Cancelar",
                                colorFondo = Rojito,
                                colorTexto = Color.White,
                                onClick = {
                                    finish()
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            BotonChevere(
                                texto = "Registrar Venta",
                                colorFondo = VerdeLimon,
                                colorTexto = Fondo1,
                                onClick = {
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    if (userId == null) {
                                        Toast.makeText(context, "Error de usuario. Vuelva a iniciar sesión.", Toast.LENGTH_SHORT).show()
                                        return@BotonChevere
                                    }

                                    // --- INICIO DE LA CORRECCIÓN ---
                                    // Ahora permitimos que el cliente sea nulo
                                    val clienteDni = selectedCliente?.dni

                                    // Si el campo de búsqueda tiene texto pero no hay cliente seleccionado,
                                    // podría significar que el usuario no completó la selección.
                                    if (searchQuery.isNotEmpty() && clienteDni == null) {
                                        Toast.makeText(context, "Cliente no seleccionado. Para continuar sin cliente, vacíe el campo de búsqueda.", Toast.LENGTH_LONG).show()
                                        return@BotonChevere
                                    }
                                    // --- FIN DE LA CORRECCIÓN ---

                                    // Llamar al ViewModel con los datos necesarios
                                    saleViewModel.registrarVentaConPagos(
                                        totalVenta = totalVenta,
                                        idUsuario = userId,
                                        idCliente = clienteDni, // <-- Pasamos el DNI o null
                                        cartItems = cartItems,
                                        metodoPago = metodoSelecionado,
                                        montoYape = yape.toDoubleOrNull(),
                                        montoEfectivo = efectivo.toDoubleOrNull()
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
