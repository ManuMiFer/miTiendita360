import com.google.gson.annotations.SerializedName

data class Perdida(
    @SerializedName("id_producto")
    val idProducto: Int,

    @SerializedName("cantidad")
    val cantidad: Int,

    @SerializedName("fecha")
    val fecha: String,

    @SerializedName("razon")
    val razon: String,

    @SerializedName("detalles")
    val detalles: String,

    @SerializedName("id_usuario")
    val idUsuario: String
)

// Clase para la respuesta de la API tras insertar
data class PerdidaResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("idPerdida") val idPerdida: Int? = null
)
data class PerdidaHistorial(
    val id: Int,
    val cantidad: Int,
    val fecha: String,
    val razon: String,
    val detalles: String?,
    val ruta_imagen: String?,
    val nombre_producto: String
)

data class PerdidaHistorialResponse(
    val success: Boolean,
    val message: String,
    val data: List<PerdidaHistorial>
)