
package com.miranda.mitiendita360.network

import com.miranda.mitiendita360.models.Compra // Necesitarás crear este data class
import com.miranda.mitiendita360.models.CompraCompletaRequest
import com.miranda.mitiendita360.models.CompraResponse
import com.miranda.mitiendita360.models.GenericResponse
import com.miranda.mitiendita360.models.Producto
import com.miranda.mitiendita360.models.Purchase
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

// Representa el cuerpo del JSON que enviaremos a la API

interface CompraService {

    // CORRECCIÓN 1: Se usa CompraResponse y la ruta del endpoint se corrige.
    @POST("registrarCompraCompleta.php")
    suspend fun registrarCompraCompleta(@Body request: CompraCompletaRequest): CompraResponse

    @Multipart
    @POST("subirImagen.php") // Tu script ya se llama así
    suspend fun subirImagen(
        @Part imagen: MultipartBody.Part,
        @Part("nombre_archivo") nombreArchivo: RequestBody
    ): GenericResponse

    // CORRECCIÓN 2: Tu script PHP de actualización espera 'nombreImagen', no un mapa genérico.
    // Además, tu script de ejemplo usa POST, no PUT. Lo ajustamos.
    @POST("actualizar_ruta_imagen.php") // Basado en el nombre de tu archivo en el servicio de producto
    suspend fun actualizarUrlImagen(
        @Body body: Map<String, String> // Enviaremos {"idProducto": "123", "nombreImagen": "nombre.jpg"}
    ): GenericResponse

    @GET("obtenerDetalleCompras.php")
    suspend fun getPurchaseDetails(
        @Query("uid") uid: String,
        @Query("q") searchQuery: String? = null,
        @Query("fechaInicio") startDate: String? = null,
        @Query("fechaFin") endDate: String? = null
    ): List<Purchase>
}