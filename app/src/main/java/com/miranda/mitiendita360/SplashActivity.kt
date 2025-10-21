package com.miranda.mitiendita360

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiTiendita360Theme {
                MiTiendita360Theme {
                    SplashScreen {
                        // Esta lambda se ejecutará cuando la animación termine
                        decideNextActivity()
                    }
                }
            }
        }
    }
        private fun decideNextActivity() {
            val firebaseAuth = FirebaseAuth.getInstance()
            if (firebaseAuth.currentUser != null) {
                // Si HAY sesión, envía al usuario a WelcomeActivity
                navigateTo(MenuActivity::class.java)
            } else {
                // Si NO HAY sesión, envía al usuario a LoginActivity
                navigateTo(UserLoginActivity::class.java)
            }
        }

        private fun navigateTo(activityClass: Class<*>) {
            val intent = Intent(this, activityClass)
            // Limpia el historial para que no se pueda volver a la Splash Screen
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    @Composable
    fun SplashScreen(onAnimationFinished: () -> Unit) {
        // Estado para controlar la posición de la animación
        var startAnimation by remember { mutableStateOf(false) }

        // Anima el valor del 'offset' (desplazamiento vertical)
        val yOffset by animateDpAsState(
            targetValue = if (startAnimation) 0.dp else -300.dp, // Cae desde -300dp hasta 0dp
            animationSpec = tween(
                durationMillis = 1000, // Duración de 1 segundo
                delayMillis = 200      // Empieza después de 200ms
            ),
            label = "fall_animation"
        )

        // LaunchedEffect para iniciar la animación y la navegación
        LaunchedEffect(key1 = true) {
            startAnimation = true  // Inicia la animación
            delay(2000)            // Espera 2 segundos en total
            onAnimationFinished()  // Llama a la función para navegar
        }

        // UI de la Splash Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Fondo1),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Loguito envuelto en un Modifier que aplica la animación
            Column(
                modifier = Modifier.offset(y = yOffset)
            ) {
                LoguitoSplash()
            }
        }
    }

    @Composable
    fun LoguitoSplash() {
        // Es una versión de tu Loguito sin el botón de "Continuar"
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 55.dp)
        ){
            Image(
                painterResource(R.drawable.tienda),
                contentDescription = "Logo de la tienda",
                colorFilter = ColorFilter.tint(color = VerdeLimon),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(130.dp)
            )
            Row {
                Text("MiTiendita",
                    fontSize = 35.sp,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
                Text("360", color = VerdeLimon,
                    modifier = Modifier.padding(start = 5.dp),
                    fontSize = 35.sp,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("¡Tu negocio aun mas simple!",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light
            )
        }
    }

