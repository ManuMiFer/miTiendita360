package com.miranda.mitiendita360.network

import com.google.gson.annotations.SerializedName
import com.miranda.mitiendita360.models.Proveedor
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface ProveedorService {
    @POST("insertar_proveedor.php") // El nombre de tu archivo PHP
    suspend fun insertSupplier(@Body supplier: Proveedor): SimpleApiResponse

    @GET("get_proveedores.php")
    suspend fun getProveedores(
        @Query("idUsuario") idUsuario: String
    ): ProveedorListResponse
}

data class SimpleApiResponse(
    val success: Boolean,
    val message: String
)
data class Proveedor(
    // Los nombres deben coincidir con las claves del JSON que devuelve tu PHP
    @SerializedName("ruc") val ruc: String,
    @SerializedName("nombre") val nombre: String
)

data class ProveedorListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<Proveedor>?
)