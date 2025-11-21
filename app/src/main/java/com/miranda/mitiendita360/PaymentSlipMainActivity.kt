package com.miranda.mitiendita360


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.graphics.Canvas
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.findViewTreeCompositionContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.miranda.mitiendita360.ui.components.BotonChevere
import com.miranda.mitiendita360.ui.theme.Fondo1
import com.miranda.mitiendita360.ui.theme.GrisClaro
import com.miranda.mitiendita360.ui.theme.GrisClaro2
import com.miranda.mitiendita360.ui.theme.MiTiendita360Theme
import com.miranda.mitiendita360.ui.theme.Rojito
import com.miranda.mitiendita360.ui.theme.VerdeLimon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable

// Modelo de datos para los items de la boleta. Debe existir y ser Serializable.
class PaymentSlipMainActivity : ComponentActivity() {
    private val shareLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        navigateToMainMenu()
    }
    private fun navigateToMainMenu() {
        val intent = Intent(this, MenuActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- 1. OBTENEMOS LOS DATOS ENVIADOS DESDE LA ACTIVITY ANTERIOR ---
        val clienteNombre = intent.getStringExtra("CLIENTE_NOMBRE") ?: "Cliente Varios"
        val fechaVenta = intent.getStringExtra("FECHA_VENTA") ?: "N/A"
        val totalVenta = intent.getDoubleExtra("TOTAL_VENTA", 0.0)
        val itemsVenta = getSerializable(intent, "ITEMS_VENTA", BoletaItem::class.java)

        setContent {
            MiTiendita360Theme {
                val scope = rememberCoroutineScope()
                val context = LocalContext.current
                val activity = (context as? ComponentActivity)
                Scaffold(
                    topBar = {
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Fondo1)
                                .padding(vertical = 40.dp)
                        ){
                            Image(
                                painterResource(R.drawable.whatsapp),
                                contentDescription = "",
                                colorFilter = ColorFilter.tint(color = Color.White),
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.padding(5.dp))
                            Text("Boleta Digital",
                                color = Color.White,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // --- INICIO DE LA CORRECCIÓN ---
                    // Usamos Box para centrar la boleta verticalmente si hay espacio de sobra.
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .background(Fondo1)
                            .padding(horizontal = 25.dp)
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopCenter // Alinea la boleta arriba
                    ) {
                        // Esta Column ahora se ajustará a su contenido.
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .height(450.dp)
                        ){
                            // --- FIN DE LA CORRECCIÓN ---

                            Row (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(GrisClaro)
                                    .padding(25.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ){
                                Image(
                                    painterResource(R.drawable.tienda),
                                    contentDescription = "",
                                    colorFilter = ColorFilter.tint(color = Color.White),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(50.dp)
                                )
                                Spacer(modifier = Modifier.padding(3.dp))
                                Text("MiTiendita360",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Column (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(25.dp)
                            ){
                                Row (
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ){
                                    Column (modifier = Modifier.width(170.dp)){
                                        Text(
                                            "Cliente: ${clienteNombre}",
                                            color = Fondo1,
                                            fontSize = 11.sp,
                                        )
                                    }
                                    Text(fechaVenta,
                                        color = Fondo1,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.padding(10.dp))

                                // --- ENCABEZADOS DE LA TABLA ---
                                Row (
                                    modifier = Modifier.fillMaxWidth()
                                ){
                                    Text("Descripcion", modifier = Modifier.weight(0.4f), color = Fondo1, fontSize = 11.sp)
                                    Text("Precio", modifier = Modifier.weight(0.2f), color = Fondo1, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    Text("Cant.", modifier = Modifier.weight(0.15f), color = Fondo1, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    Text("Subtotal", modifier = Modifier.weight(0.25f), color = Fondo1, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                                }
                                HorizontalDivider(thickness = 1.dp, color = Fondo1)

                                // --- CONTENIDO DE LA TABLA ---
                                Column {
                                    itemsVenta.forEach { item ->
                                        Row(modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)) {
                                            Text(
                                                text = item.descripcion,
                                                modifier = Modifier.weight(0.4f),
                                                color = Fondo1, fontSize = 11.sp
                                            )
                                            Text(
                                                text = String.format("S/%.2f", item.precioUnitario),
                                                modifier = Modifier.weight(0.2f),
                                                color = Fondo1, fontSize = 11.sp,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            Text(
                                                text = item.cantidad.toString(),
                                                modifier = Modifier.weight(0.15f),
                                                color = Fondo1, fontSize = 11.sp,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            Text(
                                                text = String.format("S/%.2f", item.subtotal),
                                                modifier = Modifier.weight(0.25f),
                                                color = Fondo1, fontSize = 11.sp,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                                            )
                                        }
                                        HorizontalDivider(thickness = 1.dp, color = GrisClaro2)
                                    }
                                }

                                HorizontalDivider(thickness = 1.dp, color = Fondo1)

                                // --- TOTAL ---
                                Row (
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ){
                                    Text("Total",
                                        color = Fondo1,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(String.format("S/%.2f", totalVenta),
                                        color = Fondo1,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // --- Spacer para el espacio antes del agradecimiento ---
                                Spacer(modifier = Modifier.height(24.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "! Gracias por su compra !",
                                        color = Fondo1,
                                        fontSize = 11.sp,
                                    )
                                }
                                // Agregamos un padding final para que no quede pegado al borde
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                        Column (
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ){
                            BotonChevere(
                                texto = "Compartir",
                                onClick = {
                                    scope.launch {
                                        activity?.let { act ->
                                            // --- TRABAJO PESADO EN UN HILO SECUNDARIO ---
                                            val pdfFile = withContext(Dispatchers.IO) {
                                                // Variables que necesitamos dentro del hilo secundario
                                                val displayMetrics = act.resources.displayMetrics
                                                val width = (displayMetrics.widthPixels * 0.90).toInt()
                                                val height = (450 * displayMetrics.density).toInt()
                                                var bitmap: Bitmap? = null

                                                withContext(Dispatchers.Main) {
                                                    // 1. Obtenemos la vista raíz de la actividad.
                                                    val decorView = act.window.decorView as ViewGroup

                                                    // 2. Creamos nuestra ComposeView.
                                                    val composeView = ComposeView(act).apply {
                                                        setContent {
                                                            // Ponemos el tema alrededor para asegurar que todos los estilos
                                                            // (colores, fuentes) se apliquen correctamente.
                                                            MiTiendita360Theme {
                                                                BoletaComposable(clienteNombre, fechaVenta, itemsVenta, totalVenta)
                                                            }
                                                        }
                                                    }

                                                    // 3. ¡EL NUEVO PASO CRUCIAL! Adjuntamos la vista a la jerarquía.
                                                    // La hacemos invisible para que el usuario no la vea.
                                                    composeView.visibility = View.INVISIBLE
                                                    decorView.addView(composeView)

                                                    // 4. Medimos y posicionamos la vista para forzar su renderizado.
                                                    val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
                                                    val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                                                    composeView.measure(widthSpec, heightSpec)
                                                    composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

                                                    // 5. Ahora que la vista está adjunta y dibujada, creamos el Bitmap.
                                                    bitmap = createBitmapFromView(composeView, composeView.measuredWidth, composeView.measuredHeight)

                                                    // 6. Limpiamos y quitamos la vista que añadimos.
                                                    decorView.removeView(composeView)
                                                }

// ...el resto del código sigue igual...
                                                if (bitmap != null) {
                                                    createPdfFromBitmap(bitmap!!, act)
                                                } else {
                                                    null
                                                }
                                            }
                                            // Una vez que el archivo PDF está listo, lo compartimos (en el hilo principal)
                                            pdfFile?.let {
                                                val chooserIntent = sharePdf(it, act)
                                                shareLauncher.launch(chooserIntent)
                                            }
                                        }
                                    }
                                },
                                colorTexto = Fondo1,
                                colorFondo = VerdeLimon
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            BotonChevere(
                                texto = "Omitir",
                                onClick = { navigateToMainMenu()},
                                colorTexto = Color.White,
                                colorFondo = GrisClaro
                            )
                            Spacer(modifier = Modifier.height(25.dp))
                        }
                    }
                }
            }
        }
    }
}

// Función auxiliar para obtener la lista de objetos serializables de forma segura
fun <T : Serializable?> getSerializable(intent: Intent, key: String, clazz: Class<T>): List<T> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getSerializableExtra(key, ArrayList::class.java) as? List<T> ?: emptyList()
    } else {
        @Suppress("DEPRECATION")
        intent.getSerializableExtra(key) as? List<T> ?: emptyList()
    }
}
@Composable
fun BoletaComposable(
    clienteNombre: String,
    fechaVenta: String,
    items: List<BoletaItem>,
    totalVenta: Double
) {
    // Es la Column con fondo blanco que ya tienes, pero aislada.
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .height(450.dp) // Mantenemos tu altura fija
    ){
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .background(GrisClaro)
                .padding(25.dp),
            verticalAlignment = Alignment.CenterVertically,
        ){
            Image(
                painterResource(R.drawable.tienda),
                contentDescription = "",
                colorFilter = ColorFilter.tint(color = Color.White),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.padding(3.dp))
            Text("MiTiendita360",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(25.dp)
        ){
            Row (
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ){
                Column (modifier = Modifier.width(170.dp)){
                    Text(
                        "Cliente: $clienteNombre",
                        color = Fondo1,
                        fontSize = 11.sp,
                    )
                }
                Text(fechaVenta,
                    color = Fondo1,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.padding(10.dp))

            Row (
                modifier = Modifier.fillMaxWidth()
            ){
                Text("Descripcion", modifier = Modifier.weight(0.4f), color = Fondo1, fontSize = 11.sp)
                Text("Precio", modifier = Modifier.weight(0.2f), color = Fondo1, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("Cant.", modifier = Modifier.weight(0.15f), color = Fondo1, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("Subtotal", modifier = Modifier.weight(0.25f), color = Fondo1, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
            HorizontalDivider(thickness = 1.dp, color = Fondo1)

            Column {
                items.forEach { item ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)) {
                        Text(
                            text = item.descripcion,
                            modifier = Modifier.weight(0.4f),
                            color = Fondo1, fontSize = 11.sp
                        )
                        Text(
                            text = String.format("S/%.2f", item.precioUnitario),
                            modifier = Modifier.weight(0.2f),
                            color = Fondo1, fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = item.cantidad.toString(),
                            modifier = Modifier.weight(0.15f),
                            color = Fondo1, fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = String.format("S/%.2f", item.subtotal),
                            modifier = Modifier.weight(0.25f),
                            color = Fondo1, fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                    HorizontalDivider(thickness = 1.dp, color = GrisClaro2)
                }
            }

            HorizontalDivider(thickness = 1.dp, color = Fondo1)

            Row (
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ){
                Text("Total",
                    color = Fondo1,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(String.format("S/%.2f", totalVenta),
                    color = Fondo1,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "! Gracias por su compra !",
                    color = Fondo1,
                    fontSize = 11.sp,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun createBitmapFromView(view: View, width: Int, height: Int): Bitmap {
    view.layout(0, 0, width, height)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}

// 2. Crea un PDF a partir del Bitmap
fun createPdfFromBitmap(bitmap: Bitmap, context: ComponentActivity): File? {
    val pdfDocument = PdfDocument()
    val pageInfo =
        PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    canvas.drawBitmap(bitmap, 0f, 0f, null)
    pdfDocument.finishPage(page)

    val pdfFile = File(context.cacheDir, "boleta_digital.pdf")
    try {
        FileOutputStream(pdfFile).use { fos ->
            pdfDocument.writeTo(fos)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    } finally {
        pdfDocument.close()
    }
    return pdfFile
}

// 3. Lanza el Intent para compartir el archivo PDF
fun sharePdf(pdfFile: File, context: ComponentActivity): Intent {
    // Usamos FileProvider para obtener una URI segura para el archivo
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return Intent.createChooser(shareIntent, "Compartir Boleta vía...")
}
