package com.miranda.mitiendita360.models

import com.google.gson.annotations.SerializedName

data class Cliente(
    val dni: String,
    val nombre: String,
    val apellidop: String,
    val apellidom: String,
    val contacto: String?,
    val direccion: String?
)
data class ReniecResponse(
    @SerializedName("first_name") val nombres: String,
    @SerializedName("first_last_name") val apellidoPaterno: String,
    @SerializedName("second_last_name") val apellidoMaterno: String,
    @SerializedName("full_name") val nombreCompleto: String,
    @SerializedName("document_number") val dni: String
)

data class ClienteRequest(
    val dni: String,
    val nombre: String,
    val apellidop: String,
    val apellidom: String,
    val idUsuario: String,
    val contacto: String? = null, // Opcional
    val direccion: String? = null // Opcional
)