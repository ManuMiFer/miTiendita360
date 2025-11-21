package com.miranda.mitiendita360

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miranda.mitiendita360.models.Proveedor
import com.miranda.mitiendita360.ui.components.SearchTextField
import com.miranda.mitiendita360.ui.components.TopHeader
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.VerdeLimon

class SupplierActivity : ComponentActivity() {
    private val viewModel: SupplierViewModel by viewModels() //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 2. Observar los estados del ViewModel
            val uiState by viewModel.uiState
            val searchQuery by viewModel.searchQuery

            MiTiendita360Theme {
                val keyboardController = LocalSoftwareKeyboardController.current
                val focusManager = LocalFocusManager.current
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ){
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(color = Fondo1)
                            .padding(top = 30.dp, start = 20.dp, end = 20.dp)
                    ){
                        // Cabecera
                        Box(){
                            TopHeader()
                            Column (
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ){
                                Spacer(modifier = Modifier.height(25.dp))
                                Image(
                                    imageVector = (Icons.Default.ArrowBackIos),
                                    contentDescription = "",
                                    colorFilter = ColorFilter.tint(color = Color.White),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .offset(x = 5.dp)
                                        .size(30.dp)
                                        .clickable {
                                            finish()
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Campo de búsqueda controlado por el ViewModel
                        SearchTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it)},
                            placeholder = "Buscar por RUC o Nombre...",
                            modifier = Modifier.fillMaxWidth(),
                            imeAction = ImeAction.Search,
                            // 2. Define qué hacer cuando se presiona ese botón
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    // 3. Oculta el teclado y quita el foco
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // --- ÁREA DE CONTENIDO DINÁMICO ---
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            when (val state = uiState) {
                                is SupplierUiState.Loading -> {
                                    CircularProgressIndicator(color = VerdeLimon)
                                }
                                is SupplierUiState.Error -> {
                                    Text(
                                        text = state.message,
                                        color = Color.Red,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                                is SupplierUiState.Success -> {
                                    if (state.suppliers.isEmpty()) {
                                        Text(
                                            text = "No se encontraron proveedores.",
                                            color = Color.White
                                        )
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(15.dp),
                                            contentPadding = PaddingValues(bottom = 20.dp)
                                        ) {
                                            items(state.suppliers, key = { it.ruc }) { supplier ->
                                                SupplierCard(supplier = supplier)
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
// Composable para la tarjeta de un solo proveedor (reutilizable)
@Composable
fun SupplierCard(supplier: Proveedor) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(25.dp))
            .background(color = GrisClaro)
            .padding(20.dp)
    ){
        Text(
            text = "RUC: ${supplier.ruc}",
            color = VerdeLimon,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = supplier.razonSocial,
            color = Color.White,
            fontSize = 20.sp,
        )
        // La dirección y el teléfono ahora usan un Composable reutilizable
        if (!supplier.direccion.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            InfoRow(icon = Icons.Default.LocationOn, label = "Dirección:", value = supplier.direccion)
        }
        if (!supplier.telefono.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            InfoRow(icon = Icons.Default.Call, label = "Teléfono:", value = supplier.telefono)
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ){
        Image(
            imageVector = icon,
            contentDescription = label,
            colorFilter = ColorFilter.tint(color = VerdeLimon),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(53.dp)
        )
        Column {
            Text(text = label, color = Color.White, fontSize = 18.sp)
            Text(text = value, color = Color.White, fontSize = 18.sp)
        }
    }
}
