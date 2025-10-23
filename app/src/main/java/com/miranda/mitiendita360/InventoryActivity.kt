package com.miranda.mitiendita360

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoDisturbAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.models.Categoria
import com.miranda.mitiendita360.models.Producto
import com.miranda.mitiendita360.models.StatusUpdateRequest
import com.miranda.mitiendita360.network.RetrofitClient
import com.miranda.mitiendita360.ui.components.ActionButton
import com.miranda.mitiendita360.ui.components.TextFieldChevere2
import com.miranda.mitiendita360.ui.theme.Celeste
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.GrisClaro2
import com.miranda.mitiendita360.ui.theme.Lila
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


sealed interface InventoryUiState {
    data class Success(val products: List<Producto>) : InventoryUiState
    object Error : InventoryUiState
    object Loading : InventoryUiState
}
    sealed interface CategoryUiState {
        data class Success(val categories: List<Categoria>) : CategoryUiState
        object Error : CategoryUiState
        object Loading : CategoryUiState
}

class InventoryActivity : ComponentActivity() {
    private val viewModel: InventoryViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val message = viewModel.userMessage
            val selectedStatus = remember { mutableStateOf<Int?>(1) }
            LaunchedEffect(message) {
                if (message != null) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    viewModel.clearUserMessage() // Limpia el mensaje después de mostrarlo
                }
            }

            // Observa la petición de refresco desde el ViewModel
            val shouldRefresh = viewModel.refreshList

            MiTiendita360Theme {
                val keyboardController = LocalSoftwareKeyboardController.current
                val context = LocalContext.current
                val message = viewModel.userMessage
                LaunchedEffect(message) {
                    if (message != null) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        viewModel.clearUserMessage() // Limpia el mensaje después de mostrarlo
                    }
                }

                var uiState by remember { mutableStateOf<InventoryUiState>(InventoryUiState.Loading) }
                var searchQuery by remember { mutableStateOf("") }
                var searchJob by remember { mutableStateOf<Job?>(null) }
                var categoryUiState by remember { mutableStateOf<CategoryUiState>(CategoryUiState.Loading) }
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                var selectedCategory by remember { mutableStateOf<String?>(null) }
                var selectedStatus by remember { mutableStateOf(1) }

                val fetchCategories: () -> Unit = {
                    // No necesitamos comprobar el userId aquí, ya que no se usa
                    categoryUiState = CategoryUiState.Loading
                    lifecycleScope.launch {
                        categoryUiState = try {
                            val response = RetrofitClient.categoriaService.getCategorias()
                            if (response.success) {
                                // El '?' en List<Categoria>? nos obliga a manejar el caso nulo
                                CategoryUiState.Success(response.data ?: emptyList())
                            } else {
                                Log.e("InventoryActivity", "API categorías: ${response.message}")
                                CategoryUiState.Error
                            }
                        } catch (e: Exception) {
                            Log.e("InventoryActivity", "Network error categorías: ${e.message}", e)
                            CategoryUiState.Error
                        }
                    }
                }

                val fetchProducts: (String?, String?, Int) -> Unit = { query, categoryId, status  ->
                    if (userId.isBlank()) {
                        uiState = InventoryUiState.Error
                    } else {
                        uiState = InventoryUiState.Loading
                        lifecycleScope.launch {
                            Log.d(
                                "FetchProducts",
                                "Buscando con query: '$query', categoryId: '$categoryId'"
                            )

                            uiState = try {
                                // ¡LLAMADA CORREGIDA! Pasamos los parámetros por separado.
                                val response = RetrofitClient.productoService.getProducts(
                                    userId = userId,
                                    searchTerm = query?.ifBlank { null }, // Envía null si la búsqueda está vacía
                                    categoryId = categoryId,
                                    estado = status

                                )

                                if (response.success) {
                                    InventoryUiState.Success(response.data)
                                } else {
                                    Log.e(
                                        "InventoryActivity",
                                        "API devolvió un error: ${response.message}"
                                    )
                                    InventoryUiState.Error
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "InventoryActivity",
                                    "Error de red o parsing: ${e.message}",
                                    e
                                )
                                InventoryUiState.Error
                            }
                        }
                    }
                }
                val updateProductLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    // Este bloque se ejecuta CUANDO VUELVES de ProductUpdateActivity
                    if (result.resultCode == Activity.RESULT_OK) {
                        // Si la actividad de edición dice que algo cambió (RESULT_OK),
                        // recargamos la lista para ver los cambios.
                        Log.d("LauncherResult", "Producto actualizado. Refrescando la lista...")
                        Toast.makeText(context, "Lista de productos actualizada.", Toast.LENGTH_SHORT).show()
                        fetchProducts(searchQuery, selectedCategory, selectedStatus)
                    } else {
                        // Si el usuario simplemente presionó "atrás" sin guardar, no hacemos nada.
                        Log.d("LauncherResult", "Actualización cancelada. No se refresca la lista.")
                    }
                }
                val onUpdateStatusClick: (Int, Int) -> Unit = { productId, currentStatus ->
                    lifecycleScope.launch {
                        try {
                            // El nuevo estado es el opuesto al actual
                            val newStatus = if (currentStatus == 1) 0 else 1

                            val response = RetrofitClient.productoService.updateProductStatus(
                                StatusUpdateRequest(id = productId, estado = newStatus)
                            )

                            if (response.success) {
                                Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
                                // Recargamos la lista para que el producto desaparezca de la vista actual
                                fetchProducts(searchQuery, selectedCategory, selectedStatus)
                            } else {
                                Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Log.e("UpdateStatus", "Error al actualizar estado: ", e)
                            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                val toggleProductStatus: (Int, Int) -> Unit = { productId, currentStatus ->
                    val newStatus = if (currentStatus == 1) 0 else 1 // Lógica de interruptor
                    lifecycleScope.launch {
                        try {
                            val response = RetrofitClient.productoService.updateProductStatus(
                                StatusUpdateRequest(id = productId, estado = newStatus)
                            )
                            if (response.success) {
                                Toast.makeText(context, "Estado actualizado con éxito", Toast.LENGTH_SHORT).show()
                                // Refresca la lista para que el producto desaparezca/aparezca
                                fetchProducts(searchQuery, selectedCategory, selectedStatus)
                            } else {
                                Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Log.e("ToggleStatus", "Error de conexión: ", e)
                            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

// 2. FUNCIÓN PARA EL DIÁLOGO DE "ACCIÓN REQUERIDA"
//    Esta función SIEMPRE desactiva (pone estado 0).
                val deactivateProduct: (Producto) -> Unit = { productToDeactivate ->
                    lifecycleScope.launch {
                        try {
                            val response = RetrofitClient.productoService.updateProductStatus(
                                StatusUpdateRequest(id = productToDeactivate.id!!, estado = 0) // Siempre 0
                            )
                            if (response.success) {
                                Toast.makeText(context, "'${productToDeactivate.nombre}' desactivado", Toast.LENGTH_SHORT).show()
                                // Refresca la lista para que el producto desaparezca de la vista "Activos"
                                fetchProducts(searchQuery, selectedCategory, selectedStatus)
                            } else {
                                Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Log.e("DeactivateStatus", "Error de conexión: ", e)
                            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                        } finally {
                            // Importante: Cierra el diálogo después de la operación
                            viewModel.hideDialog()
                        }
                    }
                }

                val onSearchQueryChange: (String) -> Unit = { newQuery ->
                    searchQuery = newQuery
                    searchJob?.cancel()
                    searchJob = lifecycleScope.launch {
                        delay(350L)
                        // Ahora la búsqueda respeta el filtro de categoría
                        fetchProducts(newQuery, selectedCategory,
                            selectedStatus)
                    }
                }

                LaunchedEffect(Unit) {
                    fetchProducts(null, null, selectedStatus) // Carga inicial de productos activos
                    fetchCategories()
                }
                val productDetailLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    // No necesitamos hacer nada cuando volvemos, pero el launcher debe existir.
                    Log.d("ProductDetail", "Regresando de la vista de detalle.")
                }
                LaunchedEffect(searchQuery, selectedCategory, selectedStatus) {
                    fetchProducts(searchQuery, selectedCategory, selectedStatus)
                }

                // Observa los mensajes del ViewModel
                LaunchedEffect(message) {
                    if (message != null) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        viewModel.clearUserMessage()
                    }
                }

                // --- CAMBIO CLAVE: Observa la petición de refresco y llama a fetchProducts ---
                val shouldRefresh = viewModel.refreshList
                LaunchedEffect(shouldRefresh) {
                    if (shouldRefresh) {
                        Log.d("InventoryActivity", "Refrescando la lista por petición del ViewModel...")
                        fetchProducts(searchQuery, selectedCategory, selectedStatus)
                        viewModel.onRefreshComplete()
                    }
                }
                // 5. Cargar los datos iniciales la primera vez que se muestra la pantalla
                LaunchedEffect(key1 = userId) {
                    if (userId.isNotEmpty()) {
                        fetchProducts(null, null, selectedStatus) // Carga inicial sin filtros
                        fetchCategories()
                    }
                }
                Scaffold(
                    topBar = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(
                                    shape = WideOvalBottomShape(
                                        arcHeight = 300f, // Profundidad de la curva
                                        horizontalControlOffset = 180f
                                    )
                                )
                                .height(250.dp)
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
                                Image(
                                    painterResource(R.drawable.tienda),
                                    contentDescription = "",
                                    colorFilter = ColorFilter.tint(color = VerdeLimon),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(70.dp)
                                )
                                Text(
                                    "Inventario",
                                    color = Color.White,
                                    fontSize = 25.sp
                                )

                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    InventoryDialogs(
                        state = viewModel.dialogState,
                        onDismiss = { viewModel.hideDialog() },
                        onConfirmDelete = { viewModel.confirmDelete(it) },
                        onConfirmDeactivate = { deactivateProduct(it) }
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(innerPadding)
                            .padding(horizontal = 20.dp)

                    ) {
                        Row (){
                            TextFieldChevere2(
                                modifier = Modifier.weight(1f),
                                value = searchQuery,
                                onValueChange = onSearchQueryChange, // Conectamos la función de búsqueda
                                placeholder = "Buscar producto...",
                                imeAction = ImeAction.Search,
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        // Cuando el usuario presiona "Buscar" en el teclado:
                                        fetchProducts(searchQuery, selectedCategory, selectedStatus) // Inicia la búsqueda inmediatamente
                                        keyboardController?.hide() // Oculta el teclado
                                    }
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            ActionButton(
                                onClick = {
                                    // Cambia entre estado 1 (activos) y 0 (inactivos)
                                    val newStatus = if (selectedStatus == 1) 0 else 1
                                    selectedStatus = newStatus
                                    // Recarga los productos con el nuevo estado
                                    fetchProducts(searchQuery, selectedCategory, newStatus)
                                },
                                // Elige el ImageVector correcto basado en el estado
                                icon = if (selectedStatus == 1) {
                                    // Si vemos activos, el icono es "ir a inactivos"
                                    Icons.Default.RemoveRedEye
                                } else {
                                    // Si vemos inactivos, el icono es "ir a activos"
                                    Icons.Filled.VisibilityOff
                                },
                                // Cambia el color de fondo para que actúe como un "toggle"
                                backgroundColor =  VerdeLimon,
                                iconColor = if (selectedStatus == 0) Fondo1 else Fondo1,
                                buttonSize = 50.dp, // Tamaño estándar para un Icon Button
                                iconSize = 24.dp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Categorias",
                            color = Fondo1,
                            fontSize = 25.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))


                        LazyRow (
                            horizontalArrangement = Arrangement.spacedBy(10.dp), // Aumenté el espacio para que se vea mejor
                            modifier = Modifier.fillMaxWidth()
                        ){
                            item {
                                CategoryItem(
                                    imageUrl = null,
                                    isSelected = selectedCategory == null,
                                    onClick = {
                                        selectedCategory = null
                                        fetchProducts(searchQuery, null,
                                            selectedStatus)
                                    },
                                    isAllButton = true
                                )
                            }
                            // Contenido dinámico
                            when (val state = categoryUiState) {
                                is CategoryUiState.Loading -> {
                                    item { CircularProgressIndicator(modifier = Modifier.size(55.dp)) }
                                }
                                is CategoryUiState.Success -> {
                                    items(state.categories, key = { it.id }) { categoria ->
                                        val imageUrl = "https://manuelmirandafernandez.com/imagenes/categoria${categoria.id}.png"

                                        // --- LÓGICA CORREGIDA AQUÍ ---

                                        CategoryItem(
                                            imageUrl = imageUrl,
                                            // 1. La selección ahora se basa en el ID
                                            isSelected = selectedCategory == categoria.id.toString(),
                                            onClick = {
                                                // 2. Si ya estaba seleccionada, la quitamos (null). Si no, la ponemos.
                                                val newCategory = if (selectedCategory == categoria.id.toString()) null else categoria.id.toString()
                                                selectedCategory = newCategory

                                                // 3. Pasamos el ID de la categoría al filtro
                                                fetchProducts(searchQuery, selectedCategory,
                                                    selectedStatus)
                                            }
                                        )
                                    }
                                }
                                is CategoryUiState.Error -> { /* ... */ }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            when (uiState) {
                                is InventoryUiState.Loading -> {
                                    CircularProgressIndicator(modifier = Modifier.size(50.dp))
                                }
                                is InventoryUiState.Error -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Warning, "Error", tint = Color.Red, modifier = Modifier.size(60.dp))
                                        Text("Error al cargar productos")
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(onClick = { fetchProducts(searchQuery, selectedCategory, selectedStatus) }) { Text("Reintentar") }
                                    }
                                }
                                is InventoryUiState.Success -> {
                                    val products = (uiState as InventoryUiState.Success).products
                                    if (products.isEmpty()) {
                                        Text("No se encontraron productos.", textAlign = TextAlign.Center)
                                    } else {
                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(15.dp),
                                            modifier = Modifier.fillMaxHeight()// Espacio entre items
                                        ) {
                                            items(
                                                items = products,
                                                key = { product -> product.id!! } // Clave única para cada item
                                            ) { product ->
                                                ProductItemCard(product = product,
                                                    onEditClick = { productId ->
                                                        val intent = Intent(context, ProductUpdateActivity::class.java)
                                                        intent.putExtra("productId", productId)
                                                        updateProductLauncher.launch(intent)
                                                    },
                                                    onUpdateStatusClick = { onUpdateStatusClick(product.id!!, product.estado!!) },
                                                    onDeletClick = {
                                                        viewModel.onAttemptDelete(product)
                                                    },
                                                    onCardClick = {
                                                            productId ->
                                                        val intent = Intent(context, ProductDetailActivity::class.java).apply {
                                                            putExtra("PRODUCT_ID", productId)
                                                        }
                                                        productDetailLauncher.launch(intent)
                                                    }
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
        }
    }
}
@Composable
fun ProductItemCard(
    product: Producto,
    onEditClick: (Int) -> Unit,
    onUpdateStatusClick: () -> Unit,
    onDeletClick: () -> Unit,
    onCardClick: () -> Unit
    ) {
    val context = LocalContext.current
    val imageUrl = if (!product.imagen.isNullOrBlank()) {
        "https://manuelmirandafernandez.com/imagenes/${product.imagen}"
    } else {
        R.drawable.box
    }

    Log.d("ProductImage", "Cargando imagen para '${product.nombre}': $imageUrl") // Log para depurar

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable{onCardClick(product.id!!)}
            .clip(shape = RoundedCornerShape(30.dp))
            .background(color = GrisClaro)
            .padding(15.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // --- CARGA DE LA IMAGEN CON COIL ---
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageUrl,
                    error = painterResource(id = R.drawable.box)
                ),
                contentDescription = product.nombre,
                contentScale = ContentScale.Crop, // Esto asegura que la imagen llene el espacio sin deformarse
                modifier = Modifier
                    .size(120.dp) // 1. Forza la imagen a ser un cuadrado de 100x100 dp
                    .clip(shape = RoundedCornerShape(15.dp)) // 2. Le aplica las esquinas redondeadas
                // Ya no hay .background(Color.White)
            )
            // --- Resto de la información del producto ---
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    product.nombre,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text("Stock:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VerdeLimon)
                    Text(product.stockActual.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Aquí tus botones de acción (ver, editar, borrar)
                    ActionButton(onClick = {
                        onUpdateStatusClick()
                    }, icon =  if (product.estado == 1) Icons.Default.VisibilityOff else Icons.Default.Visibility, backgroundColor = Celeste)
                    Spacer(modifier = Modifier.padding(3.dp))
                    ActionButton( onClick = {
                        onEditClick(product.id!!)
                    }, icon = Icons.Default.Edit, backgroundColor = Lila)
                    Spacer(modifier = Modifier.padding(3.dp))
                    ActionButton(onClick = {
                        onDeletClick()
                    }, icon = Icons.Default.Delete, backgroundColor = Color.Red)
                }
            }
        }
    }
}
@Composable
fun CategoryItem(
    imageUrl: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    isAllButton: Boolean = false // Nuevo parámetro para identificar el botón "Todos"
) {
    val borderColor = if (isSelected) VerdeLimon else Color.Transparent
    val imagePadding = if (isSelected) 3.dp else 0.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clickable { onClick() }
            .clip(shape = CircleShape)
            .size(55.dp)
            .border(2.dp, borderColor, CircleShape) // Borde para indicar selección
            .padding(imagePadding) // Padding interno para que la imagen se achique un poco
            .clip(CircleShape) // Volvemos a cortar por si el padding afecta
            .background(GrisClaro)
            .padding(10.dp)
    ) {
        if (isAllButton) {
            // Es el botón de "Todos"
            Icon(
                Icons.Default.Apps,
                contentDescription = "Todas las categorías",
                tint = if (isSelected) VerdeLimon else Color.White
            )
        } else if (imageUrl != null) {
            // Es una categoría con imagen
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageUrl,
                    error = painterResource(id = R.drawable.box)
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(if (isSelected) VerdeLimon else GrisClaro2)
            )
        }
    }
}
@Composable
fun InventoryDialogs(
    state: DialogState,
    onDismiss: () -> Unit,
    onConfirmDelete: (Producto) -> Unit,
    onConfirmDeactivate: (Producto) -> Unit
) {
    when (state) {
        is DialogState.Hidden -> { /* No hacer nada */ }

        is DialogState.ConfirmDelete -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = Color.White,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Icono de Eliminar",
                        tint = Fondo1,
                        modifier = Modifier.size(100.dp)
                    )
                },
                title = {
                    Column {
                        Row {
                            Column {
                                Text("Eliminar Producto",
                                    color = Fondo1,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold

                                    )
                                Text("¿Estas seguro que desea eliminar el producto ${state.product.nombre}?",
                                        color = GrisClaro,
                                    fontSize = 20.sp,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // 4. Botones centrados
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón de Cancelar
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("CANCELAR") }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Botón de Eliminar
                            Button(
                                onClick = { onConfirmDelete(state.product) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("ELIMINAR") }
                        }
                    }
                },
                confirmButton = {
                },
                dismissButton = {

                }
            )
        }

        is DialogState.ConfirmDeactivate -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(
                        imageVector = Icons.Default.DoDisturbAlt,
                        contentDescription = "Icono de Eliminar",
                        tint = Fondo1,
                        modifier = Modifier.size(100.dp)
                    )
                },
                containerColor = Color.White,
                text = {
                    Column {
                        Row {
                            Column {
                                Text("Desactivar Producto",
                                    color = Fondo1,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold

                                )
                                Text("El producto ${state.product.nombre} tiene ventas, solo puedes desactivarlo.",
                                    color = GrisClaro,
                                    fontSize = 20.sp,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // 4. Botones centrados
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón de Cancelar
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("CANCELAR") }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Botón de Eliminar
                            Button(
                                onClick = { onConfirmDeactivate(state.product) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("DESACTIVAR") }
                        }
                    }

                },
                confirmButton = {
                },
                dismissButton = {
                }
            )
        }
    }
}
