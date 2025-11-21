package com.miranda.mitiendita360.models

import com.google.gson.annotations.SerializedName

data class Venta(
    val id: Int,
    val fecha: String,
    @SerializedName("total_venta")
    val totalVenta: Double,
    @SerializedName("cantidad_productos")
    val cantidadProductos: Int,
    @SerializedName("productos_nombres")
    val productosNombres: String?
)

    data class DetalleVentaRequest(
        val idProducto: Int?,
        val precioVenta: Double,
        val cantidad: Int,
        val subtotal: Double
)

data class PagoInfo(
    val metodoPago: String,
    val monto: Double
)

// El objeto principal que enviaremos a la API
data class VentaRequest(
    val montoTotal: Double,
    val idUsuario: String,
    val idCliente: String?,
    val fechaHora: String,
    val detalles: List<DetalleVentaRequest>,
    val pagos: List<PagoInfo>
)

data class VentasResponse(
    val success: Boolean,
    val message: String?,
    val data: List<Venta>
)
