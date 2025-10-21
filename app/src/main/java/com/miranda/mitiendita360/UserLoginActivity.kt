package com.miranda.mitiendita360

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.VerdeLimon


class UserLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiTiendita360Theme (dynamicColor = false) {
                Scaffold(
                    topBar = {
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = Fondo1)
                                .height(290.dp)
                                .padding(top = 100.dp)
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
                            Text("Gestiona tu negocio fácilmente",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Light
                            )
                        }
                    }
                ) { innerPadding ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .background(color = Fondo1)
                            .padding(horizontal = 40.dp)
                    ){
                        Spacer(modifier = Modifier.padding(20.dp))

                        var correo by remember { mutableStateOf("") }
                        var clave by remember { mutableStateOf("") }

                        Column {
                            val keyboardController = LocalSoftwareKeyboardController.current
                            val focusManager = LocalFocusManager.current
                            var isFocused by remember { mutableStateOf(false) }
                            Text(
                                "Correo electrónico:",
                                textAlign = TextAlign.Start,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape = RoundedCornerShape(13.dp))
                                    .background(Color.White),// Padding interno para el contenido
                                contentAlignment = Alignment.CenterStart
                            ){
                                BasicTextField(
                                    value = correo,
                                    onValueChange = { correo = it},
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                        .padding(15.dp)
                                        .onFocusChanged { focusState ->
                                            isFocused = focusState.isFocused},
                                    cursorBrush = SolidColor( Color.Gray),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }
                                    )
                                )
                                if ( !isFocused && correo.isEmpty()) {
                                    Text(
                                        text = "ejemplo@mail.com",
                                        color = Color.Gray,
                                        modifier = Modifier.fillMaxWidth()
                                            .padding( horizontal = 10.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }//Input 1

                        Spacer(modifier = Modifier.padding(10.dp))

                        Column {
                            val keyboardController = LocalSoftwareKeyboardController.current
                            val focusManager = LocalFocusManager.current
                            var isFocused by remember { mutableStateOf(false) }
                            Text(
                                "Contraseña:",
                                textAlign = TextAlign.Start,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape = RoundedCornerShape(13.dp))
                                    .background(Color.White),// Padding interno para el contenido
                                contentAlignment = Alignment.CenterStart
                            ){
                                BasicTextField(
                                    value = clave,
                                    onValueChange = { clave = it},
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                        .padding(15.dp)
                                        .onFocusChanged { focusState ->
                                            isFocused = focusState.isFocused},
                                    cursorBrush = SolidColor( Color.Gray),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Done,
                                        keyboardType = KeyboardType.Password
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }
                                    ),
                                    visualTransformation = PasswordVisualTransformation()
                                )
                                if ( !isFocused && clave.isEmpty()) {
                                    Text(
                                        text = "••••••••••",
                                        color = Color.Gray,
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 10.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }//imput 2

                        Spacer(modifier = Modifier.padding(20.dp))

                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Button(
                                onClick = {
                                    if (correo.isNotEmpty() && clave.isNotEmpty()) {
                                        FirebaseAuth.getInstance()
                                            .signInWithEmailAndPassword(correo, clave)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Toast.makeText(this@UserLoginActivity, "Bienvenido", Toast.LENGTH_SHORT).show()
                                                    val intent = Intent(this@UserLoginActivity,
                                                        WelcomeActivity::class.java)
                                                    startActivity(intent)
                                                    finish()
                                                } else {
                                                    Toast.makeText(this@UserLoginActivity, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                    } else {
                                        Toast.makeText(this@UserLoginActivity, "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    "Ingresar",
                                    color = Fondo1 ,
                                    modifier = Modifier.padding(5.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            } // Botón 1

                            Spacer(modifier = Modifier.padding(5.dp))

                            Button(
                                onClick = {
                                    val intent = Intent(this@UserLoginActivity, UserRegisterActivity::class.java)
                                    startActivity(intent)
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GrisClaro, // Color de fondo del botón
                                    contentColor = Fondo1        // Color del texto "Ingresar"
                                )
                            ) {
                                Text(
                                    "Crear Cuenta",
                                    color = Color.White,
                                    modifier = Modifier.padding(5.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            } // Botón 2

                        }
                    }
                }
            }
        }
    }
}

