package com.miranda.mitiendita360

import android.app.Activity
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.models.Producto
import com.miranda.mitiendita360.network.RetrofitClient
import com.miranda.mitiendita360.ui.components.BotonChevere
import com.miranda.mitiendita360.ui.components.SearchTextField
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import kotlinx.coroutines.launch

class SearchProductActivity : ComponentActivity() {
    private fun returnSelectedProducts(selectedProducts: List<Producto>) { // <-- Ahora recibe List<Producto>
        val resultIntent = Intent().apply {
            // Usamos putParcelableArrayListExtra para listas de objetos Parcelable
            putParcelableArrayListExtra("SELECTED_PRODUCTS", ArrayList(selectedProducts))
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val excludedIds = intent.getStringArrayListExtra("EXCLUDED_IDS") ?: arrayListOf()
        enableEdgeToEdge()
        setContent {
            MiTiendita360Theme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                var searchQuery by remember { mutableStateOf("") }
                val allProducts = remember { mutableStateListOf<Producto>() }
                val selectedProductIds = remember { mutableStateListOf<String>() }

                // --- FUNCIÓN PARA LLAMAR A LA API ---
                fun searchProducts(query: String) {
                    if (userId == null) {
                        Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                        return
                    }
                    scope.launch {
                        try {
                            // Llamamos a getProducts pasando el 'query' como 'searchTerm'
                            // y null para los otros filtros que no usamos aquí.
                            val response = RetrofitClient.productoService.getProducts(
                                userId = userId,
                                searchTerm = query, // <-- 'query' se pasa como el parámetro 'searchTerm'
                                categoryId = null,  // <-- No filtramos por categoría aquí
                                estado = null       // <-- No filtramos por estado aquí
                            )

                            // Ahora que la llamada es correcta, 'response' es del tipo correcto
                            // y el compilador encontrará la propiedad 'products'.
                            if (response.success && response.data != null) {
                                allProducts.clear()
                                allProducts.addAll(response.data)
                            } else {
                                // Si la búsqueda no arroja resultados, 'products' puede ser null.
                                // Limpiamos la lista para que el usuario vea que no hay resultados.
                                allProducts.clear()
                                Log.w("SearchProduct", "La búsqueda no tuvo éxito o no devolvió productos: ${response.message}")
                            }
                        } catch (e: Exception) {
                            Log.e("SearchProduct", "Error crítico al buscar productos", e)
                            Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                LaunchedEffect(Unit) {
                    searchProducts("")
                }
                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Column(
                            verticalArrangement = Arrangement.Center,
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
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            )
                            {
                                Image(
                                    imageVector = (Icons.Default.Search),
                                    contentDescription = "",
                                    colorFilter = ColorFilter.tint(color = Color.White),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.padding(5.dp))
                                Column {
                                    Text(
                                        "Buscar",
                                        color = Color.White,
                                        fontSize = 25.sp
                                    )
                                    Text(
                                        "Productos",
                                        color = Color.White,
                                        fontSize = 25.sp
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                            Modifier
                                .fillMaxSize()
                                .background(color = Color.White)
                                .padding(innerPadding)
                                .padding(horizontal = 20.dp)
                    ){
                        SearchTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                searchProducts(it) // Llama a la búsqueda mientras escribes
                            },
                            placeholder = "Buscar producto...",
                            imeAction = ImeAction.Search,
                            keyboardActions = KeyboardActions(
                                onSearch = { searchProducts(searchQuery) }
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Productos",
                            color = Fondo1,
                            fontSize = 25.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val productosDisponibles = allProducts.filter { producto ->
                            producto.stockActual > 0 && !excludedIds.contains(producto.id .toString())
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f), // <-- AÑADE ESTO para que ocupe el espacio disponible
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = productosDisponibles,
                                key = { it.id!! }
                            ) { producto ->
                                val isSelected = selectedProductIds.contains(producto.id.toString())

                                ProductRow(
                                    producto = producto,
                                    isSelected = isSelected,
                                    onToggleSelection = {
                                        val idStr = producto.id.toString()
                                        if (isSelected) {
                                            selectedProductIds.remove(idStr)
                                        } else {
                                            selectedProductIds.add(idStr)
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        BotonChevere(
                            texto = "Agregar al carrito (${selectedProductIds.size})",
                            colorFondo = VerdeLimon,
                            colorTexto = Fondo1,
                        ) {
                            // 1. Filtra la lista de 'allProducts' para obtener los objetos completos de los seleccionados
                            val selectedProducts = allProducts.filter { producto ->
                                selectedProductIds.contains(producto.id.toString())
                            }
                            // 2. Devuelve la lista de objetos Producto completos
                            returnSelectedProducts(selectedProducts)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductRow(
    producto: Producto,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(15.dp))
            .background(color = GrisClaro)
            .clickable { onToggleSelection() } // <-- HACE TODA LA FILA CLICKEABLE
            .padding(10.dp)
    ) {
        // --- Lógica del Icono Check/No-Check ---
        Icon(
            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Circle, // Usa un círculo vacío si no está seleccionado
            contentDescription = if (isSelected) "Seleccionado" else "No seleccionado",
            tint = if (isSelected) VerdeLimon else Color.White,
            modifier = Modifier
                .size(40.dp)
                .clip(shape = RoundedCornerShape(100.dp))
                .background(Color.White)
                .padding(5.dp)
        )

        // --- Imagen del Producto (usando Coil para cargar desde URL) ---
        AsyncImage( // Reemplaza Image con AsyncImage
            model = "${BuildConfig.API_BASE_URL}/imagenes/${producto.imagen}",
            contentDescription = producto.nombre,
            modifier = Modifier
                .size(70.dp)
                .clip(shape = RoundedCornerShape(15.dp))
                .background(Color.White),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.box) // Imagen de fallback
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                producto.nombre,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                "Precio: S/ ${"%.2f".format(producto.precioVenta)}",
                color = VerdeLimon,
                fontSize = 18.sp
            )
            Text(
                "Stock: ${producto.stockActual}",
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}