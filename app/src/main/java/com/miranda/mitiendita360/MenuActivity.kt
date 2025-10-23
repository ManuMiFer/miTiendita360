package com.miranda.mitiendita360

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
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
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
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
                AccionRapidaItem("Nueva Venta", R.drawable.cashregister, SaleInsertActivity::class),
                AccionRapidaItem("Historial Ventas", R.drawable.check, SaleHistoryActivity::class),
                AccionRapidaItem("Proveedor", R.drawable.supplier, SupplierInsertActivity::class),
                 AccionRapidaItem("Mis Proveedores", R.drawable.suppliers, SupplierActivity::class)
            )
            val context = LocalContext.current
            MiTiendita360Theme {
                Scaffold(
                    topBar = {
                        Row (
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp)
                                .background(color = Fondo1)
                        ){
                            Box (
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
                                    )
                                ,
                                contentAlignment = Alignment.Center

                            ){
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
                    modifier = Modifier.fillMaxSize()) {
                    innerPadding ->

                    Column (modifier = Modifier
                        .fillMaxSize()
                        .background(color = Fondo1)
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp)
                    ) {
                        Text("Acciones rapidas",
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
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(color = GrisClaro)
                                        .clickable {
                                            val intent = Intent(context, item.destinoActivity.java)
                                            activityLauncher.launch(intent)
                                        }
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Image(
                                        painter = painterResource(id = item.imagenResId),
                                        contentDescription = item.nombre,
                                        colorFilter = ColorFilter.tint(color = VerdeLimon),
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.size(60.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.nombre,
                                        color = VerdeLimon,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
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
