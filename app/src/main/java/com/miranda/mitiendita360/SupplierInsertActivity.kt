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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

            var uid = FirebaseAuth.getInstance().currentUser
            var ruc by remember { mutableStateOf("") }
            var nombre by remember { mutableStateOf("") }
            var direccion by remember { mutableStateOf("") }
            var telefono by remember { mutableStateOf("") }

            MiTiendita360Theme {
                Scaffold(
                    topBar = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
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
                    Column (
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.White)
                            .padding(innerPadding)

                    ) {
                        Column (
                            Modifier
                                .clip(shape = WideOvalBottomShape(
                                    arcHeight = 200f, // Profundidad de la curva
                                    horizontalControlOffset = 180f)
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
                                keyboarType = KeyboardType.Number
                            )

                            Spacer(modifier = Modifier.padding(5.dp))

                            TextFieldChevere2(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = "Nombre:",
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

                            TextFieldChevere2(
                                value = telefono,
                                onValueChange = {  telefono = it },
                                label = "Teléfono:",
                                placeholder = "",
                                imeAction = ImeAction.Next,
                                enabled = true,
                                color = Color.White,
                                keyboarType = KeyboardType.Number
                            )
                            Spacer(modifier = Modifier.padding(70.dp))
                        }
                        Column(
                            Modifier.padding(horizontal = 40.dp)
                        ) {
                            BotonChevere(
                                texto = "Guardar Proveedor",
                                colorFondo = VerdeLimon,
                                colorTexto = GrisClaro,
                            ){
                                val idUsuario = uid?.uid // Obtiene el ID del usuario de Firebase
                                if (idUsuario != null && ruc.isNotBlank() && nombre.isNotBlank()) {

                                    val proveedor = Proveedor(
                                        ruc = ruc,
                                        nombre = nombre,
                                        telefono = telefono,
                                        direccion = direccion,
                                        idUsuario = idUsuario
                                    )

                                    lifecycleScope.launch {
                                        try {
                                            val response = proveedorService.insertSupplier(proveedor)
                                            if (response.success) {
                                                Toast.makeText(this@SupplierInsertActivity, "Proveedor guardado", Toast.LENGTH_SHORT).show()
                                                // Opcional: Navegar de vuelta al menú
                                                startActivity(Intent(this@SupplierInsertActivity, MenuActivity::class.java))
                                                finish() // Cierra la actividad actual
                                            } else {
                                                Toast.makeText(this@SupplierInsertActivity, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("API_ERROR", "Fallo al guardar proveedor: ${e.message}")
                                            Toast.makeText(this@SupplierInsertActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                } else {
                                    Toast.makeText(this@SupplierInsertActivity, "RUC y Nombre son obligatorios", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}