package com.miranda.mitiendita360.network

import Perdida
import PerdidaHistorialResponse
import PerdidaResponse
import com.google.gson.annotations.SerializedName
import com.miranda.mitiendita360.models.ImageUpdateRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query


data class ImageUpdateRequest(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nombre_imagen")
    val nombre_imagen: String
)
data class SubidaResponse(
    val success: Boolean,
    val message: String
)
interface PerdidaService {
    @Multipart // La petición ahora siempre es Multipart
    @POST("registrar_perdida.php") // Solo usaremos este script
    suspend fun registrarPerdidaUnificada(
        @Part imagen: MultipartBody.Part?, // La imagen es opcional (?)
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>
    ): PerdidaResponse

    @GET("get_perdidas.php")
    suspend fun getPerdidas(
        @Query("id_usuario") idUsuario: String
    ): PerdidaHistorialResponse
}