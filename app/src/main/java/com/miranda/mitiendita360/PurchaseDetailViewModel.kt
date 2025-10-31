package com.miranda.mitiendita360

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// ¡AÑADIR ESTA IMPORTACIÓN!
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.models.Purchase
import com.miranda.mitiendita360.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// La clase sellada no cambia
sealed class PurchaseUiState {
    data object Loading : PurchaseUiState()
    data class Success(val purchases: List<Purchase>) : PurchaseUiState()
    data class Error(val message: String) : PurchaseUiState()
}

class PurchaseDetailViewModel : ViewModel() {

    private val compraService = RetrofitClient.compraService
    // ¡AÑADIR ESTA LÍNEA!
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // ... (El resto de los estados no cambia)
    private val _uiState = mutableStateOf<PurchaseUiState>(PurchaseUiState.Loading)
    val uiState: State<PurchaseUiState> = _uiState

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _startDate = mutableStateOf<String?>(null)
    private val _endDate = mutableStateOf<String?>(null)

    private var searchJob: Job? = null


    init {
        // Carga inicial sin filtros
        fetchPurchases()
    }

    // --- EVENTOS DE LA UI (No cambian) ---

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        fetchPurchasesWithDebounce()
    }

    fun onStartDateChange(apiFormattedDate: String?) {
        _startDate.value = apiFormattedDate
        fetchPurchasesWithDebounce()
    }

    fun onEndDateChange(apiFormattedDate: String?) {
        _endDate.value = apiFormattedDate
        fetchPurchasesWithDebounce()
    }

    // --- LÓGICA DE CARGA DE DATOS (AQUÍ ESTÁ LA MODIFICACIÓN) ---

    private fun fetchPurchasesWithDebounce() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            fetchPurchases()
        }
    }

    private fun fetchPurchases() {
        // --- INICIO DE LA MODIFICACIÓN ---
        // 1. Obtener el UID del usuario actual
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.value = PurchaseUiState.Error("No se pudo verificar la sesión del usuario.")
            return // Detiene la ejecución si no hay usuario logueado
        }
        // --- FIN DE LA MODIFICACIÓN ---

        viewModelScope.launch {
            _uiState.value = PurchaseUiState.Loading
            try {
                val queryToSend = _searchQuery.value.ifBlank { null }
                val startDateToSend = _startDate.value
                val endDateToSend = _endDate.value

                Log.d(
                    "PurchaseViewModel",
                    // ¡Añadimos el uid al log!
                    "Enviando a la API: uid='${uid}', q='${queryToSend}', fechaInicio='${startDateToSend}', fechaFin='${endDateToSend}'"
                )

                // --- INICIO DE LA MODIFICACIÓN ---
                // 2. Llamar a la API pasando el UID
                val filteredPurchases = compraService.getPurchaseDetails(
                    uid = uid, // <--- EL NUEVO PARÁMETRO
                    searchQuery = queryToSend,
                    startDate = startDateToSend,
                    endDate = endDateToSend
                )
                // --- FIN DE LA MODIFICACIÓN ---

                _uiState.value = PurchaseUiState.Success(filteredPurchases)
            } catch (e: Exception) {
                Log.e("PurchaseViewModel", "Error al obtener compras", e) // Loguea el error completo
                _uiState.value = PurchaseUiState.Error("Error de red: ${e.message}")
            }
        }
    }

    // --- FUNCIÓN AUXILIAR ---

    // Convierte "dd/MM/yyyy" (del DatePicker) a "yyyy-MM-dd" (para la API)
    private fun String.toApiFormat(): String? {
        if (this.isBlank()) return null
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(this)
            date?.let { outputFormat.format(it) }
        } catch (e: Exception) {
            null
        }
    }
}