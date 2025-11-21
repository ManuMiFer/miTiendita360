package com.miranda.mitiendita360.network

import com.miranda.mitiendita360.OpenFoodFactsResponse
import com.miranda.mitiendita360.SaleDetailResponse
import com.miranda.mitiendita360.SaleReturnActivity
import com.miranda.mitiendita360.models.DeleteProductRequest
import com.miranda.mitiendita360.models.GenericResponse
import com.miranda.mitiendita360.models.ImageUpdateRequest
import com.miranda.mitiendita360.models.PagoInfo
import com.miranda.mitiendita360.models.ProductInsertResponse
import com.miranda.mitiendita360.models.ProductListResponse
import com.miranda.mitiendita360.models.ProductResponse
import com.miranda.mitiendita360.models.Producto
import com.miranda.mitiendita360.models.SalesCheckResponse
import com.miranda.mitiendita360.models.SingleProductResponse
import com.miranda.mitiendita360.models.StatusUpdateRequest
import com.miranda.mitiendita360.models.VentaRequest
import com.miranda.mitiendita360.models.VentasResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url

interface ProductoService {

    @POST("insertar_producto.php")
    suspend fun insertProduct(@Body productData: Producto): ProductInsertResponse

    @Multipart
    @POST("subir_imagen.php")
    suspend fun uploadImage(
        @Part imagen: MultipartBody.Part,
        @Part("nombre_archivo") nombre_archivo: RequestBody
    ): SimpleApiResponse

    @POST("actualizar_ruta_imagen.php")
    suspend fun updateImagePath(@Body request: ImageUpdateRequest): SimpleApiResponse

    @GET("get_productos.php")
    suspend fun getProducts(
        @Query("idUsuario") userId: String,
        @Query("search") searchTerm: String?,
        @Query("idCategoria") categoryId: String?,
        @Query("estado") estado: Int? // <-- AÑADE ESTA LÍNEA EXACTA
    ): ProductListResponse

    @GET("get_producto.php")
    suspend fun getProductById(
        @Query("id") productId: Int
    ): SingleProductResponse

    @POST("update_producto.php")
    suspend fun updateProduct(@Body productData: Producto): GenericResponse


    @POST("update_product_status.php")
    suspend fun updateProductStatus(@Body request: StatusUpdateRequest): GenericResponse

    @GET("get_product_by_barcode.php")
    suspend fun getProductByBarcode(
        @Query("barcode") barcode: String,
        @Query("idUsuario") userId: String,
    ): ProductResponse

    @POST("insertar_venta.php")
    suspend fun registrarVenta(@Body ventaData: VentaRequest): GenericResponse // Reutilizamos GenericResponse

    @POST("insertar_pago.php")
    suspend fun registrarPago(@Body pagoData: PagoInfo): GenericResponse


    @GET("verificar_ventas_producto.php")
    suspend fun checkProductSales(@Query("idProducto") productId: Int): SalesCheckResponse

    @POST("delete_producto.php")
    suspend fun deleteProduct(@Body request: DeleteProductRequest): GenericResponse
    @GET("get_ventas.php")
    suspend fun getVentas(
        @Query("user_id") userId: String,
        @Query("fecha_inicio") fechaInicio: String? = null,
        @Query("fecha_fin") fechaFin: String? = null,
        @Query("search_term") searchTerm: String? = null,
        @Query("estados") estados: String? = null
    ): VentasResponse
    @GET("get_venta_detalle.php")
    suspend fun getVentaDetalle(
        @Query("id_venta") ventaId: Int
    ): SaleDetailResponse

    @FormUrlEncoded
    @POST("anular_venta.php")
    suspend fun anularVenta(
        @Field("id_venta") idVenta: Int
    ): GenericResponse

    @POST("procesar_devolucion.php")
    suspend fun procesarDevolucion(
        @Body datos: SaleReturnActivity.DatosProcesoDevolucion
    ): GenericResponse

    @GET
    suspend fun getBrandFromBarcode(@Url url: String): OpenFoodFactsResponse
}

