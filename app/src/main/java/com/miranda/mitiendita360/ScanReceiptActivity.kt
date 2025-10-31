package com.miranda.mitiendita360

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miranda.mitiendita360.ui.screens.CameraScreen

import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme

class ScanReceiptActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiTiendita360Theme {
                // El NavHost es necesario para que el CameraScreen (que usa un NavController) funcione sin problemas.
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "camera_screen") {
                    composable("camera_screen") {
                        // Aquí mostramos el Composable que tiene toda la lógica de Cloud Vision.
                        CameraScreen(navController)
                    }
                }
            }
        }
    }
}