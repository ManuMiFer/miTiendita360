package com.miranda.mitiendita360.network

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.miranda.mitiendita360.models.PurchaseData

class GeminiAiHelper(apiKey: String) {

    private val generativeModel: GenerativeModel

    init {
        try {
            // Configuración del modelo
            val config = GenerationConfig.Builder().apply {
                temperature = 0.4f
            }.build()

            val safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
            )

            generativeModel = GenerativeModel(
                modelName = "gemini-2.5-flash-lite",
                apiKey = apiKey,
                generationConfig = config,
                safetySettings = safetySettings
            )

        } catch (e: Exception) {
            Log.e("GeminiAI", "Error FATAL al inicializar el modelo de Gemini: ${e.message}", e)
            throw IllegalStateException("No se pudo inicializar GeminiAiHelper", e)
        }
    }

    /**
     * Procesa la imagen y devuelve el resultado como un String JSON limpio.
     * Devolver un String es más seguro para la navegación entre pantallas.
     * @return Un String con el JSON del resultado, o null si hay un error.
     */

    suspend fun getPurchaseDataJsonFromImage(bitmap: Bitmap): String? {
        // 2. ACTUALIZAMOS EL PROMPT PARA PEDIR FECHA Y TOTAL
        val prompt = """
Analiza cuidadosamente la imagen de una boleta o factura de venta del Perú.
Extrae y devuelve la información estrictamente en formato JSON, sin texto adicional ni Markdown.

CAMPOS A EXTRAER:

rucProveedor → Número RUC del proveedor o empresa emisora.

fecha → Fecha de emisión del comprobante (formato YYYY-MM-DD).

total → Importe total cobrado (importe total pagado por el cliente).

productos → Lista de productos consolidados.

REGLAS PARA LOS PRODUCTOS:

Si aparecen varias líneas con el mismo producto o con nombres casi iguales (por ejemplo: “B.AGUA LOA” y “AGUA LOA”), unifícalos como un solo producto, nada de productos duplicados.

Usa el nombre más completo o limpio (sin prefijos como “B.”).

Si no se puede determinar algún dato, usa null.
FORMATO DE SALIDA JSON:
{
  "rucProveedor": "string",
  "fecha": "string",
  "total": double,
  "productos": [
    {
      "nombre": "string"
    }
  ]
}
    """.trimIndent()

        // El resto de la función no necesita cambios...
        return try {
            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            Log.d("GeminiAI", "Enviando prompt a Gemini (Modelo: ${generativeModel.modelName})...")
            val response = generativeModel.generateContent(inputContent)
            val responseText = response.text

            Log.i("GeminiAI", "Respuesta cruda recibida:\n$responseText")

            if (responseText.isNullOrBlank()) {
                Log.e("GeminiAI", "La respuesta de Gemini fue nula o vacía.")
                return null
            }

            val jsonStartIndex = responseText.indexOf('{')
            if (jsonStartIndex == -1) {
                Log.e("GeminiAI", "La respuesta no contenía un JSON válido (no se encontró '{').")
                return null
            }

            val jsonEndIndex = responseText.lastIndexOf('}')
            if (jsonEndIndex == -1) {
                Log.e("GeminiAI", "La respuesta no contenía un JSON válido (no se encontró '}').")
                return null
            }

            val finalJson = responseText.substring(jsonStartIndex, jsonEndIndex + 1)
            Log.d("GeminiAI", "JSON final para devolver:\n$finalJson")

            Gson().fromJson(finalJson, Any::class.java)

            finalJson
        } catch (e: Exception) {
            Log.e("GeminiAI", "Error al procesar la imagen con Gemini: ${e.message}", e)
            null
        }
    }
}