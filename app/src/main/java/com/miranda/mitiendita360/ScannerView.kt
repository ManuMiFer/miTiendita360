import android.Manifest
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerView(
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String?) -> Unit,
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    imageAnalyzer: ImageAnalysis
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Box(modifier = modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            // Se ejecuta solo una vez cuando el Composable entra en la composición
            LaunchedEffect(Unit) {
                try {
                    // Desvincula todo antes de volver a vincular
                    cameraProvider.unbindAll()
                    // Vincula el ciclo de vida de la cámara
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        imageAnalyzer, // Se vincula el analizador que ya existe
                        Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                    )
                } catch (e: Exception) {
                    Log.e("ScannerView", "Error al vincular la cámara", e)
                    onBarcodeScanned(null)
                }
            }
            // Muestra la PreviewView que ya fue creada
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        } else {
            // Si no, solicita el permiso
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
            Text("Se necesita permiso de la cámara para escanear.", modifier = Modifier.align(Alignment.Center))
        }
    }
}

// --- CAMBIO CLAVE 2: Clase Analizadora reutilizable y con control de estado ---
class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // El scanner se inicializa solo una vez
    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    )

    private var isScanning = true


    // Método para resetear el estado y permitir un nuevo escaneo
    fun startScanning() {
        isScanning = true
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: androidx.camera.core.ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        isScanning = false // Detiene el escaneo al encontrar un código
                        barcodes.first().rawValue?.let(onBarcodeDetected)
                    }
                }
                .addOnFailureListener {
                    Log.e("BarcodeAnalyzer", "Algo salió mal", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}