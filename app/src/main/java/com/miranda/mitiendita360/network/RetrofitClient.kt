package com.miranda.mitiendita360.network
import com.google.gson.GsonBuilder
import com.miranda.mitiendita360.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val productoService: ProductoService by lazy {
        instance.create(ProductoService::class.java)
    }
    val categoriaService: CategoriaService by lazy {
        instance.create(CategoriaService::class.java)
    }
    val proveedorService: ProveedorService by lazy {
        instance.create(ProveedorService::class.java)
    }
    val compraService: CompraService by lazy {
        instance.create(CompraService::class.java)
    }

    val notificacionService: NotificacionService by lazy {
        instance.create(NotificacionService::class.java)
    }

    val perdidaService: PerdidaService by lazy {
        instance.create(PerdidaService::class.java)
    }
}