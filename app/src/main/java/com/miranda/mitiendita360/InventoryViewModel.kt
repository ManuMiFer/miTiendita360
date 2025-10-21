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
                    userMessage = response.message ?: "Error desconocido al eliminar."
                }
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Error al eliminar producto", e)
                userMessage = "Error de conexión: ${e.message}"
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
}