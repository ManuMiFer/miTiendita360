package com.miranda.mitiendita360.models

import com.google.gson.annotations.SerializedName

data class CompraCompletaRequest(
    val compra: Compra,
    val productos: List<Producto>
)

// Clase para la respuesta específica de esta petición
data class CompraResponse(
    val success: Boolean,
    val message: String,
    val data: List<Int>? // El backend devolverá una lista de IDs de producto
)

// Modelo para los datos de la compra
data class Compra(
    val fechaEntrega: String,
    val montoTotal: Double,
    val idProveedor: String,
    val idUsuario: String
)