package com.miranda.mitiendita360.network

import com.miranda.mitiendita360.models.Categoria
import retrofit2.http.GET


data class CategoriaListResponse(
    val success: Boolean,
    val data: List<Categoria>?, // La lista de nombres
    val message: String?
)
interface CategoriaService {
    @GET("get_categorias.php") // Llama al script que devuelve la lista de categorías
    suspend fun getCategorias(): CategoriaListResponse
}