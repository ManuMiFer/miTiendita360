package com.miranda.mitiendita360.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date


@Parcelize
data class ScannedProduct(
    val nombre: String
) : Parcelable

@Parcelize
data class PurchaseData(
    @SerializedName("rucProveedor")
    val rucProveedor: String?,

    // 1. AÑADIMOS FECHA Y TOTAL
    @SerializedName("fecha")
    val fecha: String?,

    @SerializedName("total")
    val total: Double?,

    @SerializedName("productos")
    val productos: List<ScannedProduct>?
) : Parcelable

data class ProductDetail(
    val productId: Int,
    val productName: String, // Asumimos que podemos obtener el nombre del producto
    val quantity: Int,
    val unitPrice: Double
)

// Representa una compra completa (tCompras + su lista de tDetalleCompra)
data class Purchase(
    val id: Int,
    val providerRuc: String,
    val providerName: String, // Es crucial tener el nombre para buscar
    val deliveryDate: String,   // Usamos el tipo Date de Java/Kotlin
    val totalAmount: Double,
    val products: List<ProductDetail>
)

