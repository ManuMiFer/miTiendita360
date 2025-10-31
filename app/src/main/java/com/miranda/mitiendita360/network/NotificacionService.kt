package com.miranda.mitiendita360.network

import com.miranda.mitiendita360.models.Producto
import retrofit2.http.GET
import retrofit2.http.Query

data class NotificationResponse(
    val success: Boolean,
    val count: Int
)

data class AlertasData(
    val aVencer: List<Producto>,
    val vencidos: List<Producto>,
    val stockBajo: List<Producto>,
    val sinStock: List<Producto>
)
// Modelo para la respuesta completa de la API
data class AlertasResponse(
    val success: Boolean,
    val data: AlertasData?
)
interface NotificacionService {
    @GET("get_cantidad_notificaciones.php")
    suspend fun getNotificationCount(
        @Query("uid") userId: String
    ): NotificationResponse

    @GET("get_alertas.php")
    suspend fun getAlertas(
        @Query("idUsuario") userId: String
    ): AlertasResponse
}