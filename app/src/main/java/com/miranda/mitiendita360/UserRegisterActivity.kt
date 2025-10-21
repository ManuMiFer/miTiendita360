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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.network.usuarioService
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import com.miranda.mitiendita360.ui.theme.xd
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory


class UserRegisterActivity : ComponentActivity() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://manuelmirandafernandez.com/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    private val service = retrofit.create(usuarioService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiTiendita360Theme {
                Scaffold(
                    topBar = {
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                                .clip(shape = WideOvalBottomShape(
                                    arcHeight = 300f, // Profundidad de la curva
                                    horizontalControlOffset = 180f)
                                )
                                .height(300.dp)
                                .background(color = VerdeLimon)

                        ){
                            Image(
                                painterResource(R.drawable.tienda),
                                contentDescription = "",
                                colorFilter = ColorFilter.tint(color = Fondo1),
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(110.dp)
                            )
                            Text("Crear Cuenta",
                                fontSize = 35.sp,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Bold,
                                color = Fondo1
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.background(color= Color.White).padding(innerPadding).padding(horizontal = 40.dp)

                    ){
                        Spacer(modifier = Modifier.padding(5.dp))
                        var clave by remember { mutableStateOf("") }
                        var clave2 by remember { mutableStateOf("") }
                        var nombre by remember { mutableStateOf("") }
                        var correo by remember { mutableStateOf("") }
                        Column {
                            val keyboardController = LocalSoftwareKeyboardController.current
                            val focusManager = LocalFocusManager.current
                            var isFocused by remember { mutableStateOf(false) }
                            Text(
                                "Nombre:",
                                textAlign = TextAlign.Start,
                                color = GrisClaro,
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
                                    value = nombre,
                                    onValueChange = { nombre = it},
                                    textStyle = TextStyle(color = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color = GrisClaro)
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
                                if ( !isFocused && nombre.isEmpty()) {
                                    Text(
                                        text = "User",
                                        color = Color.White,
                                        modifier = Modifier.fillMaxWidth()
                                            .padding( horizontal = 10.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }//Input Nombre

                        Spacer(modifier = Modifier.padding(10.dp))

                        Column {
                            val keyboardController = LocalSoftwareKeyboardController.current
                            val focusManager = LocalFocusManager.current
                            var isFocused by remember { mutableStateOf(false) }
                            Text(
                                "Correo Electrónico:",
                                textAlign = TextAlign.Start,
                                color = GrisClaro,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape = RoundedCornerShape(13.dp))
                                    .background(Color.White),// Padding interno para el contenido
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = correo,
                                    onValueChange = { correo = it },
                                    textStyle = TextStyle(color = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color = GrisClaro)
                                        .padding(15.dp)
                                        .onFocusChanged { focusState ->
                                            isFocused = focusState.isFocused
                                        },
                                    cursorBrush = SolidColor(Color.Gray),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }
                                    )
                                )
                                if (!isFocused && correo.isEmpty()) {
                                    Text(
                                        text = "ejemplo@gmail.com",
                                        color = Color.White,
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 10.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }//Input correo

                        Spacer(modifier = Modifier.padding(10.dp))
                        Column {
                            val keyboardController = LocalSoftwareKeyboardController.current
                            val focusManager = LocalFocusManager.current
                            var isFocused by remember { mutableStateOf(false) }
                            Text(
                                "Contraseña:",
                                textAlign = TextAlign.Start,
                                color = GrisClaro,
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
                                    textStyle = TextStyle(color = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color = GrisClaro)
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
                                        color = Color.White,
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 10.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }// contraseña

                        Spacer(modifier = Modifier.padding(10.dp))

                        Column {
                            val keyboardController = LocalSoftwareKeyboardController.current
                            val focusManager = LocalFocusManager.current
                            var isFocused by remember { mutableStateOf(false) }
                            Text(
                                "Confirmar Contraseña:",
                                textAlign = TextAlign.Start,
                                color = GrisClaro,
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
                                    value = clave2,
                                    onValueChange = { clave2 = it},
                                    textStyle = TextStyle(color = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color = GrisClaro)
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
                                if ( !isFocused && clave2.isEmpty()) {
                                    Text(
                                        text = "••••••••••",
                                        color = Color.White,
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 10.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }// contraseña 2

                        Spacer(modifier = Modifier.padding(13.dp))

                        Button(
                            onClick = {
                                if (correo.isNotEmpty() && clave.isNotEmpty() && nombre.isNotEmpty() && clave2.isNotEmpty()) {
                                    if (clave != clave2) {
                                        Toast.makeText(this@UserRegisterActivity, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                                    } else {
                                        FirebaseAuth.getInstance()
                                            .createUserWithEmailAndPassword(correo, clave)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    val user = FirebaseAuth.getInstance().currentUser
                                                    if (user != null) {
                                                        saveUsuario(user.uid, nombre)
                                                    } else {
                                                        Toast.makeText(this@UserRegisterActivity, "Error: No se pudo obtener el usuario de Firebase.", Toast.LENGTH_LONG).show()
                                                    }
                                                } else {
                                                    Toast.makeText(this@UserRegisterActivity, "Error de Firebase: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                    }
                                } else {
                                    Toast.makeText(this@UserRegisterActivity, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdeLimon, // Color de fondo del botón
                                contentColor = Fondo1        // Color del texto "Ingresar"
                            )
                        ) {
                            Text(
                                "Registrarse",
                                color = Fondo1 ,
                                modifier = Modifier.padding(5.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.padding(10.dp))

                        Text(
                            "360",
                            color = xd,
                            fontWeight = FontWeight.Bold,
                            fontSize = 100.sp
                        )
                    }
                }
            }
        }
    }

    private fun saveUsuario(id: String, nombre: String) {
        lifecycleScope.launch {
            try {
                val  result = service.insertUsuario(id, nombre)
                Toast.makeText(this@UserRegisterActivity,
                    "Usuario Registrado Correctamente: $result" ,
                    Toast.LENGTH_SHORT).show()

                FirebaseAuth.getInstance().signOut()
                // Limpiamos la pila para que no pueda volver a la pantalla de registro
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                val intent = Intent(this@UserRegisterActivity, UserLoginActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e("API Error", "Error genérico: ${e.message}", e)
                Toast.makeText(
                    this@UserRegisterActivity,
                    "Error de red: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

