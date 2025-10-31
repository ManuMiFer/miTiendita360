package com.miranda.mitiendita360

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.network.ApiService
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UsuarioResponse(
    val success: Boolean,
    val nombre: String?,
    val message: String?
)
class WelcomeActivity : ComponentActivity() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("${BuildConfig.API_BASE_URL}") // Revisa que tu URL base sea correcta
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(this, UserLoginActivity::class.java))
            finish()
            return
        }

        setContent {
            MiTiendita360Theme {
                var nombreUsuario: String by remember { mutableStateOf("") }

                LaunchedEffect(key1 = currentUser.uid) {
                    val response = apiService.getUsuario(currentUser.uid)
                    nombreUsuario = response.nombre.toString()
                }

                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Fondo1)
                        .padding(20.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            CircleDrawing(35)
                            Spacer(modifier = Modifier.padding(3.dp))
                            CircleDrawing(25)
                        }
                        Spacer(modifier = Modifier.padding(3.dp))
                        Column(Modifier.padding(top = 10.dp)) {
                            CircleDrawing(65)
                        }

                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),

                        ) {
                        Row {
                            Text(
                                "Hola", color = Color.White, fontSize = 30.sp
                            )

                            Text(
                                nombreUsuario,
                                color = Color.White,
                                fontSize = 30.sp,
                                modifier = Modifier.padding(start = 5.dp)
                            )
                        }

                        Text("Te damos la bienvenida a", color = Color.White)

                        Spacer(modifier = Modifier.padding(10.dp))

                        Loguito()

                    }
                    Row(
                        Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(top = 10.dp)) {
                            CircleDrawing(120)
                        }
                        Spacer(modifier = Modifier.padding(3.dp))
                        Column(
                        ) {
                            CircleDrawing(60)
                            Spacer(modifier = Modifier.padding(3.dp))
                            CircleDrawing(30)
                        }
                    }
                }
            }
        }
    }
    @Composable
    fun Loguito() {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Fondo1)
                .height(290.dp)
                .padding(horizontal = 55.dp)
        ){
            Image(
                painterResource(R.drawable.tienda),
                contentDescription = "",
                colorFilter = ColorFilter.tint(color = VerdeLimon),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(130.dp)
            )
            Row {
                Text("MiTiendita",
                    fontSize = 35.sp,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text("360", color = VerdeLimon,
                    modifier = Modifier.padding(start = 5.dp),
                    fontSize = 35.sp,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("¡Tu negocio aun mas simple!",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.height(45.dp))

            Button(
                onClick = {
                    startActivity(Intent(this@WelcomeActivity, MenuActivity::class.java))
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeLimon,
                    contentColor = GrisClaro
                )
            ) {
                Text("Continuar")
            }
        }
    }
    @Composable
    fun CircleDrawing( tamaño : Int) {
        Canvas(
            modifier = Modifier.size(tamaño.dp) // Define el tamaño del Canvas
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            drawCircle(
                color = GrisClaro,
                center = androidx.compose.ui.geometry.Offset(x = canvasWidth / 2, y = canvasHeight / 2),
                radius = size.minDimension / 2, // O un radio específico

            )
        }
    }
}

