package com.miranda.mitiendita360.network

import com.google.gson.annotations.SerializedName
import com.miranda.mitiendita360.models.Proveedor
import com.miranda.mitiendita360.models.SunatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url


interface ProveedorService {
    @POST("insertar_proveedor.php") // El nombre de tu archivo PHP
    suspend fun insertSupplier(@Body supplier: Proveedor): SimpleApiResponse

    @GET("get_proveedores.php")
    suspend fun getProveedores(
        @Query("idUsuario") idUsuario: String,
        @Query("searchTerm") searchTerm: String?
    ): ProveedorListResponse

    @GET
    suspend fun getSunatInfo(
        @Url url: String, // Usamos @Url para pasar la URL completa
        @Header("Authorization") apiKey: String
    ): SunatResponse

}

data class SimpleApiResponse(
    val success: Boolean,
    val message: String
)

data class ProveedorListResponse(
    val success: Boolean,
    val message: String,
    val data: List<Proveedor>?
)

