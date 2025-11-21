
package com.miranda.mitiendita360

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Usamos tus modelos existentes que me mostraste
import com.miranda.mitiendita360.models.VentaRequest
import com.miranda.mitiendita360.models.DetalleVentaRequest
import com.miranda.mitiendita360.models.PagoInfo
import com.miranda.mitiendita360.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Los estados de resultado no cambian
sealed class SaleResult {
    object Loading : SaleResult()
    data class Success(val message: String) : SaleResult()
    data class Error(val message: String) : SaleResult()
    object Idle : SaleResult()
}

class SaleViewModel : ViewModel() {

    private val _saleResult = MutableStateFlow<SaleResult>(SaleResult.Idle)
    val saleResult: StateFlow<SaleResult> = _saleResult

    fun registrarVentaConPagos(
        totalVenta: Double,
        idUsuario: String,
        idCliente: String?,
        cartItems: List<CartItem>,
        metodoPago: String,
        montoYape: Double?,
        montoEfectivo: Double?
    ) {
        viewModelScope.launch {
            _saleResult.value = SaleResult.Loading
            // --- INICIO DE LA CORRECCIÓN: MEJORAR EL MANEJO DE ERRORES ---
            try {
                // 1. Construir la lista de detalles
                val detalles = cartItems.map {
                    DetalleVentaRequest(
                        idProducto = it.producto.id,
                        precioVenta = it.producto.precioVenta,
                        cantidad = it.quantity,
                        subtotal = it.producto.precioVenta * it.quantity
                    )
                }

                // 2. Construir la lista de pagos
                val pagos = mutableListOf<PagoInfo>()
                when (metodoPago) {
                    "Efectivo" -> pagos.add(PagoInfo(metodoPago = "Efectivo", monto = totalVenta))
                    "Yape" -> pagos.add(PagoInfo(metodoPago = "Yape", monto = totalVenta))
                    "Multiple" -> {
                        if (montoYape != null && montoYape > 0) {
                            pagos.add(PagoInfo(metodoPago = "Yape", monto = montoYape))
                        }
                        if (montoEfectivo != null && montoEfectivo > 0) {
                            pagos.add(PagoInfo(metodoPago = "Efectivo", monto = montoEfectivo))
                        }
                    }
                }

                // 3. Obtener la fecha y hora actual
                val fechaHoraActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                // 4. Construir el objeto VentaRequest COMPLETO
                val ventaRequest = VentaRequest(
                    montoTotal = totalVenta,
                    fechaHora = fechaHoraActual,
                    idUsuario = idUsuario,
                    idCliente = idCliente,
                    detalles = detalles,
                    pagos = pagos
                )

                // 5. Llamar a registrarVenta
                val response = RetrofitClient.productoService.registrarVenta(ventaRequest)

                if (response.success) {
                    _saleResult.value = SaleResult.Success(response.message)
                } else {
                    // Si el servidor devuelve success: false, pero no es un error HTTP
                    _saleResult.value = SaleResult.Error("Error del servidor: ${response.message}")
                }

            } catch (e: retrofit2.HttpException) {
                // ESTE ES EL BLOQUE MÁS IMPORTANTE PARA ERRORES 500, 404, etc.
                val errorBody = e.response()?.errorBody()?.string() ?: "Cuerpo del error no disponible"
                val errorMessage = "Error HTTP ${e.code()}: $errorBody"

                // Imprimimos el error completo en el Logcat para depuración
                android.util.Log.e("SaleViewModel_PHP_ERROR", errorMessage)

                // Mostramos un mensaje más amigable al usuario
                _saleResult.value = SaleResult.Error("Error del servidor (código ${e.code()}). Revisa el Logcat para más detalles.")

            } catch (e: Exception) {
                // Captura otros errores (problemas de red, JSON malformado, etc.)
                android.util.Log.e("SaleViewModel_GENERAL_ERROR", "Error general: ${e.message}", e)
                _saleResult.value = SaleResult.Error("Error de red o datos: ${e.message}")
            }
            // --- FIN DE LA CORRECCIÓN ---
        }
    }

    fun resetResult() {
        _saleResult.value = SaleResult.Idle
    }
}