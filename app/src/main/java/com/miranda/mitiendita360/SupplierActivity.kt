package com.miranda.mitiendita360

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.miranda.mitiendita360.ui.components.BotonChevere
import com.miranda.mitiendita360.ui.components.TextFieldChevere2
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hola $name!",
        modifier = modifier
    )
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

// Composable para la cabecera estática
@Composable
fun TopHeader() {
    Box(
        modifier = Modifier.fillMaxWidth()
    ){
        Image(
            painterResource(R.drawable.suppliers),
            contentDescription = "Header Image",
            colorFilter = ColorFilter.tint(color = VerdeLimon),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(250.dp)
        )
        Column(
            modifier = Modifier.padding(top = 145.dp, start = 210.dp),
        ){
            Text("Detalle", color = Color.White, fontSize = 25.sp)
            Text("Proveedor", color = Color.White, fontSize = 25.sp)
        }
    }
}