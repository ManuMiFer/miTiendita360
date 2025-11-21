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
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.network.RetrofitClient
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.GrisClaro2
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.Rojito
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import com.miranda.mitiendita360.ui.theme.xd
import kotlin.reflect.KClass

data class AccionRapidaItem(
    val nombre: String,
    @param:DrawableRes val imagenResId: Int,
    val destinoActivity: KClass<out ComponentActivity>
)

class MenuActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, UserLoginActivity::class.java))
            finish()
            return
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var notificaciones by remember { mutableStateOf(0) }
            var mostrarVentanaPersonalizada by remember { mutableStateOf(false) }
            var itemPresionado by remember { mutableStateOf<AccionRapidaItem?>(null) }
            LaunchedEffect(key1 = user.uid) {
                try {
                    // Llama al servicio que creaste. Reemplaza `productoService` si tu función está en otra parte.
                    val response = RetrofitClient.notificacionService.getNotificationCount(user.uid)
                    if (response.success) {
                        notificaciones = response.count
                    }
                } catch (e: Exception) {
                    // Manejo de error silencioso para no molestar al usuario
                    // si falla la carga de notificaciones.
                    Log.e("NotificationError", "No se pudo cargar el conteo de notificaciones: ${e.message}")
                }
            }

            val activityLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Operación completada.", Toast.LENGTH_SHORT).show()
                }
            }
             val accionesRapidas = listOf(
                AccionRapidaItem("Nuevo Producto", R.drawable.add, ProductInsertActivity::class), // Reemplaza con tus drawables reales
                AccionRapidaItem("Inventario", R.drawable.box, InventoryActivity::class),
                AccionRapidaItem("Nueva Venta", R.drawable.cashregister, SaleActivity::class),
                AccionRapidaItem("Historial Ventas", R.drawable.check, SaleHistoryActivity::class),
                AccionRapidaItem("Proveedor", R.drawable.supplier, SupplierInsertActivity::class), // Reemplaza con tus drawables reales
                AccionRapidaItem("Mis Proveedores", R.drawable.suppliers, SupplierActivity::class),
            )
            val context = LocalContext.current
            MiTiendita360Theme {
                Scaffold(
                    topBar = {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp, horizontal = 40.dp)
                                .background(color = Fondo1)
                        ) {
                            if (notificaciones > 0) {
                                Box (
                                    Modifier.clickable{
                                        val intent =
                                            Intent(context, AlertActivity::class.java)
                                        activityLauncher.launch(intent)
                                    }
                                ){
                                    Image(
                                        painterResource(R.drawable.campana),
                                        contentDescription = "",
                                        colorFilter = ColorFilter.tint(color = Color.White),
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .size(50.dp)
                                    )
                                    Column( // --- 1. Envolvemos el Text en un Box para controlar el alineamiento y tamaño ---
                                        modifier = Modifier
                                            .align(Alignment.TopEnd) // Alineamos el Box, no el Text directamente // --- 2. Forzamos un tamaño cuadrado para asegurar un círculo perfecto ---
                                            .clip(CircleShape)
                                            .size(25.dp)
                                            .background(Rojito)
                                            .padding(1.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center // CentramosKotlin el texto dentro del Box
                                    ) {
                                        Text(
                                            text = notificaciones.toString(),
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold // Opcional: hace que el número se vea mejor
                                        )
                                    }
                                }
                            }
                            else{
                                Box {
                                    Image(
                                        painterResource(R.drawable.campana),
                                        contentDescription = "",
                                        colorFilter = ColorFilter.tint(color = Color.White),
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .size(50.dp)
                                    )
                                    Column( // --- 1. Envolvemos el Text en un Box para controlar el alineamiento y tamaño ---
                                        modifier = Modifier
                                            .align(Alignment.TopEnd) // Alineamos el Box, no el Text directamente // --- 2. Forzamos un tamaño cuadrado para asegurar un círculo perfecto ---
                                            .clip(CircleShape)
                                            .size(25.dp)
                                            .background(Color.Green)
                                            .padding(1.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center // CentramosKotlin el texto dentro del Box
                                    ) {
                                        Icon(
                                            imageVector = (Icons.Default.Check),
                                            contentDescription = "",
                                            tint =  Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                            Box(
                                Modifier
                                    .clip(shape = RoundedCornerShape(100.dp))
                                    .background(color = GrisClaro)
                                    .size(50.dp)
                                    .clickable(
                                        onClick = {

                                            FirebaseAuth.getInstance().signOut()
                                            startActivity(
                                                Intent(
                                                    this@MenuActivity,
                                                    UserLoginActivity::class.java
                                                )
                                            )


                                        }
                                    ),
                                contentAlignment = Alignment.Center

                            ) {
                                Image(
                                    painterResource(R.drawable.user),
                                    contentDescription = "",
                                    colorFilter = ColorFilter.tint(color = xd),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .offset(y = 4.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Fondo1)
                            .padding(innerPadding)
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            "Acciones rapidas",
                            color = Color.White,
                            fontSize = 30.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(accionesRapidas) { item ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (itemPresionado == item) VerdeLimon else GrisClaro2)
                                        .clickable(
                                            interactionSource =
                                                remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {if (item.nombre == "Nuevo Producto") {
                                                mostrarVentanaPersonalizada = true
                                            } else {
                                                val intent =
                                                    Intent(context, item.destinoActivity.java)
                                                activityLauncher.launch(intent)
                                            }}
                                        )
                                        .pointerInput(item) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    // Espera a que el usuario presione el botón
                                                    val down = awaitFirstDown(requireUnconsumed = false)
                                                    itemPresionado = item // Cambia el estado al item presionado

                                                    // Espera a que el usuario levante el dedo o cancele el gesto
                                                    waitForUpOrCancellation()
                                                    itemPresionado = null // Limpia el estado
                                                }
                                            }
                                        }
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Image(
                                        painter = painterResource(id = item.imagenResId),
                                        contentDescription = item.nombre,
                                        colorFilter = ColorFilter.tint(color = Fondo1),
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.size(60.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.nombre,
                                        color = Fondo1,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold

                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(color = GrisClaro2)
                                .clickable {

                                    val intent =
                                        Intent(context, PurchaseDetailActivity::class.java)
                                    activityLauncher.launch(intent)
                                }
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Column(modifier = Modifier.weight(1f)
                                , horizontalAlignment = Alignment.CenterHorizontally
                            ){
                                Image(
                                    painter = painterResource(R.drawable.supplier),
                                    contentDescription = "",
                                    colorFilter = ColorFilter.tint(color = Fondo1),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(92.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)
                                , horizontalAlignment = Alignment.CenterHorizontally
                            ){
                                Text(
                                    text = "Detalle",
                                    color = Fondo1,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Compras",
                                    color = Fondo1,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (mostrarVentanaPersonalizada) {
                        Dialog(
                            onDismissRequest = { mostrarVentanaPersonalizada = false }
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Fondo1),
                                modifier = Modifier.fillMaxWidth() // O un ancho específico con .width(300.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(15.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row (
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Column (
                                            modifier = Modifier.weight(1f)
                                        ){
                                            Text("Registro de Productos",
                                                color = Color.White,
                                                fontSize = 18.sp)
                                            Text("¿Como deseas registrar los productos de tu inventario?",
                                                color = Color.White,
                                                fontSize = 15.sp)
                                        }
                                        Image(
                                            painterResource(R.drawable.tienda),
                                            contentDescription = "",
                                            colorFilter = ColorFilter.tint(color = VerdeLimon),
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.size(80.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // 4. Botones centrados
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                val intent = Intent(context, ProductInsertActivity::class.java)
                                                activityLauncher.launch(intent)
                                                mostrarVentanaPersonalizada = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = GrisClaro2,
                                                contentColor = Fondo1
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) { Text("Manualmente") }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Button(
                                            onClick = {
                                                val intent = Intent(context, ScanReceiptActivity::class.java) // <-- Nueva Activity
                                                activityLauncher.launch(intent)
                                                mostrarVentanaPersonalizada = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = VerdeLimon,
                                                contentColor = Fondo1
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) { Text("Por Boleta") }
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

