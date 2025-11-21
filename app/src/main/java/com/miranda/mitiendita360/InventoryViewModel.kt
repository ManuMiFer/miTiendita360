package com.miranda.mitiendita360

import android.util.Log
import androidx.activity.result.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miranda.mitiendita360.models.DeleteProductRequest
import com.miranda.mitiendita360.models.Producto
import com.miranda.mitiendita360.models.StatusUpdateRequest
import com.miranda.mitiendita360.network.RetrofitClient
import kotlinx.coroutines.launch

sealed class DialogState {
    object Hidden : DialogState()
    data class ConfirmDelete(val product: Producto) : DialogState()
    data class ConfirmDeactivate(val product: Producto) : DialogState()
}

class InventoryViewModel : ViewModel() {
    var dialogState by mutableStateOf<DialogState>(DialogState.Hidden)
        private set

    var userMessage by mutableStateOf<String?>(null)
        private set

    var refreshList by mutableStateOf(false)
        private set

    fun onAttemptDelete(product: Producto) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.productoService.checkProductSales(product.id!!)
                if (response.success) {
                    dialogState = if (response.hasSales) {
                        DialogState.ConfirmDeactivate(product)
                    } else {
                        DialogState.ConfirmDelete(product)
                    }
                } else {
                    userMessage = response.message ?: "Error al verificar ventas."
                }
            } catch (e: Exception) {
                userMessage = "Error de conexión: ${e.message}"
            }
        }
    }
    fun confirmDelete(product: Producto) {
        val productId = product.id ?: run {
            userMessage = "Error: ID de producto inválido."
            hideDialog()
            return
        }

        viewModelScope.launch {
            try {
                val request = DeleteProductRequest(idProducto = productId)
                val response = RetrofitClient.productoService.deleteProduct(request)
                if (response.success) {
                    userMessage = "Producto '${product.nombre}' eliminado con éxito."
                    refreshList = true
                } else {
                    // Esto manejará los errores lógicos donde el servidor responde con success: false pero código 200 OK.
                    userMessage = response.message ?: "Error desconocido al eliminar."
                }
            } catch (e: Exception) {
                // --- INICIO DE LA SOLUCIÓN ---
                // Aquí atrapamos las excepciones de red, incluyendo los códigos de error HTTP.
                Log.e("InventoryViewModel", "Error al eliminar producto", e)

                if (e is retrofit2.HttpException && e.code() == 409) {
                    // ¡Atrapamos el error 409 (Conflict)!
                    // Intentamos leer el mensaje que envía el PHP.
                    val errorBody = e.response()?.errorBody()?.string()
                    if (!errorBody.isNullOrEmpty()) {
                        try {
                            // Usamos Gson para convertir el JSON de error en nuestro objeto GenericResponse
                            val gson = com.google.gson.Gson()
                            val errorResponse = gson.fromJson(errorBody, com.miranda.mitiendita360.models.GenericResponse::class.java)
                            // Mostramos el mensaje amigable que viene del servidor.
                            // Ejemplo: "No se puede eliminar: El producto aún tiene X unidades en stock."
                            userMessage = errorResponse.message
                        } catch (jsonError: Exception) {
                            // Si falla el parseo del JSON, mostramos un mensaje genérico pero claro.
                            userMessage = "No se puede eliminar: el producto tiene stock."
                        }
                    } else {
                        // Si el cuerpo del error está vacío, mostramos el mensaje genérico.
                        userMessage = "No se puede eliminar: el producto tiene stock."
                    }
                } else {
                    // Para cualquier otro error (sin conexión, error 500, etc.)
                    userMessage = "Error de conexión o del servidor. Inténtalo más tarde."
                }
                // --- FIN DE LA SOLUCIÓN ---
            } finally {
                hideDialog()
            }
        }
    }
    fun hideDialog() {
        dialogState = DialogState.Hidden
    }
    fun clearUserMessage() {
        userMessage = null
    }
    fun onRefreshComplete() {
        refreshList = false
    }

    fun postMessage(message: String) {
        userMessage = message
    }
}