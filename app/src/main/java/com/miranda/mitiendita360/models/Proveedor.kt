package com.miranda.mitiendita360.models

import com.google.gson.annotations.SerializedName

data class Proveedor(
    val ruc: String,
    val razonSocial: String,
    val telefono: String,
    val direccion: String,
    val idUsuario: String,
    val distrito: String,
    val provincia: String,
    val departamento: String
)

data class SunatResponse(
    @SerializedName("razon_social")
    val razonSocial: String?,

    @SerializedName("direccion")
    val direccion: String?,

    @SerializedName("distrito")
    val distrito: String?,

    @SerializedName("provincia")
    val provincia: String?,

    @SerializedName("departamento")
    val departamento: String?
    // Puedes añadir más campos aquí si los necesitas en el futuro
)
