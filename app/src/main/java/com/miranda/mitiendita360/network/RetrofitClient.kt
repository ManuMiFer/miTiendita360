package com.miranda.mitiendita360.network
import com.miranda.mitiendita360.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://manuelmirandafernandez.com/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val productoService: ProductoService by lazy {
        instance.create(ProductoService::class.java)
    }
    val categoriaService: CategoriaService by lazy {
        instance.create(CategoriaService::class.java)
    }
}