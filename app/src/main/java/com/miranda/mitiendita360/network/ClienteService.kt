package com.miranda.mitiendita360.network

import com.miranda.mitiendita360.models.Cliente
import com.miranda.mitiendita360.models.ClienteRequest
import com.miranda.mitiendita360.models.GenericResponse
import com.miranda.mitiendita360.models.ReniecResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ClienteService {
    @POST("insertar_cliente.php") // Asegúrate que la ruta sea correcta en tu servidor
    suspend fun registrarCliente(@Body cliente: ClienteRequest): Response<GenericResponse>

    @GET("get_clientes.php")
    suspend fun getClientes(@Query("idUsuario") idUsuario: String): Response<List<Cliente>>
}

interface ReniecApiService {
    // --- CORRECCIÓN 1: El @GET debe estar vacío, ya que la URL base lo contiene todo ---
    @GET(".") // Usamos un punto para indicar que queremos usar la URL base tal cual
    suspend fun consultarDni(
        @Query("numero") dni: String,
        // --- CORRECCIÓN 2: El token SIN "Bearer" ---
        @Header("Authorization") token: String = "sk_11327.EGtAwzG0M7HOweYRoJzlOhQsBRTYP9Xq",
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<ReniecResponse>
}