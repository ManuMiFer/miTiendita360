package com.miranda.mitiendita360.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.miranda.mitiendita360.BuildConfig
import com.miranda.mitiendita360.CompletePurchaseActivity
import com.miranda.mitiendita360.models.PurchaseData
import com.miranda.mitiendita360.network.GeminiAiHelper // Importamos el nuevo Helper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cameraController = remember { LifecycleCameraController(context) }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    // 1. Inicializamos nuestro nuevo GeminiAiHelper
    val geminiHelper = remember {
        GeminiAiHelper(apiKey = BuildConfig.GOOGLE_API_KEY)
    }

    // 2. La función de procesamiento es ahora mucho más simple
    fun processImageAndNavigate(bitmap: Bitmap, context: Context) {
        scope.launch(Dispatchers.IO) {
            Log.d("CameraScreen", "Iniciando análisis de imagen con Gemini AI...")

            // 1. LLAMA A LA NUEVA FUNCIÓN QUE DEVUELVE UN STRING
            val purchaseDataJson: String? = geminiHelper.getPurchaseDataJsonFromImage(bitmap)

            if (purchaseDataJson == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "No se pudo analizar la imagen.", Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            // 2. NAVEGA PASANDO EL STRING JSON
            withContext(Dispatchers.Main) {
                Log.d("CameraScreen", "Navegando a CompletePurchaseActivity con JSON.")
                val intent = Intent(context, CompletePurchaseActivity::class.java).apply {
                    // Usamos un nombre de extra diferente para evitar confusiones
                    putExtra("PURCHASE_DATA_JSON", purchaseDataJson)
                }
                context.startActivity(intent)
            }
        }
    }

    // El código de los launchers y la UI no cambia
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            if (bitmap != null) {
                processImageAndNavigate(bitmap, context)
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                processImageAndNavigate(bitmap, context)
            }
        }
    )

    Scaffold { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (cameraPermissionState.status.isGranted) {
                CameraPreview(controller = cameraController, modifier = Modifier.fillMaxSize())
            } else {
                Text(text = "Se necesita permiso de cámara.", modifier = Modifier.align(Alignment.Center))
            }
            FloatingActionButton(
                onClick = { takePhotoLauncher.launch() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Icon(Icons.Default.Camera, contentDescription = "Tomar foto")
            }
            FloatingActionButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 32.dp, end = 32.dp)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = "Abrir galería")
            }
        }
    }
}

@Composable
fun CameraPreview(controller: LifecycleCameraController, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = {
            PreviewView(it).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier
    )
}

