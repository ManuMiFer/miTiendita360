package com.miranda.mitiendita360.network

import com.miranda.mitiendita360.UsuarioResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface usuarioService {
    @FormUrlEncoded
    @POST("usuarioinsert.php")
    suspend fun insertUsuario(
        @Field("id") id: String,
        @Field("nombre") nombre: String
    ): String
}
interface ApiService {
    @FormUrlEncoded
    @POST("get_usuario.php")
    suspend fun getUsuario(@Field("id") id: String): UsuarioResponse
}