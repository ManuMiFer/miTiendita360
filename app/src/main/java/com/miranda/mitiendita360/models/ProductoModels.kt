package com.miranda.mitiendita360.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Producto(
    val nombre: String,
    val precioVenta: Double,
    val stockActual: Int,
    val stockMinimo: Int,
    val precioCompra: Double,
    val fechaCad: String?,
    val marca: String?,
    val codBarras: String?,
    val idProveedor: String,
    val idUsuario: String,
    val idCategoria: Int,

    val estado: Int? = 1,
    val id: Int? = null,
    val imagen: String?
): Parcelable

data class ProductInsertResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("idProducto") val idProducto: Int?
)

data class ImageUpdateRequest(
    val idProducto: Int,
    val nombreImagen: String
)

data class GenericResponse(
    val success: Boolean,
    val message: String
)

data class ProductListResponse(
    val success: Boolean,
    val data: List<Producto>,
    val message: String?
)
data class SingleProductResponse(
    val success: Boolean,
    val data: Producto?,
    val message: String?
)
data class StatusUpdateRequest(
    val id: Int, val estado: Int
)
data class ProductResponse(
    val success: Boolean,
    val product: Producto?,
    val message: String?
)

data class SalesCheckResponse(
    val success: Boolean,
    val hasSales: Boolean,
    val message: String?   )
data class DeleteProductRequest(val idProducto: Int)

