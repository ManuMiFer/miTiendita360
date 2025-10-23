package com.miranda.mitiendita360


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.models.Proveedor
import com.miranda.mitiendita360.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class SupplierUiState {
    object Loading : SupplierUiState()
    data class Success(val suppliers: List<Proveedor>) : SupplierUiState()
    data class Error(val message: String) : SupplierUiState()
}

class SupplierViewModel : ViewModel(){
    // --- ESTADOS OBSERVABLES ---

    // Estado principal de la UI (cargando, éxito, error)
    private val _uiState = mutableStateOf<SupplierUiState>(SupplierUiState.Loading)
    val uiState: State<SupplierUiState> = _uiState

    // Estado para el texto en el campo de búsqueda
    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    // --- LÓGICA PRIVADA ---

    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private var searchJob: Job? = null

    // Se llama automáticamente al crear el ViewModel
    init {
        // Carga inicial de todos los proveedores del usuario
        fetchSuppliers("")
    }

    // --- ACCIONES (llamadas desde la UI) ---
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        // Cancela el trabajo de búsqueda anterior si está en curso
        searchJob?.cancel()
        // Inicia un nuevo trabajo de búsqueda con un retardo de 300ms
        searchJob = viewModelScope.launch {
            delay(300) // Espera a que el usuario deje de escribir
            fetchSuppliers(newQuery)
        }
    }

    /**
     * Llama a la API para obtener la lista de proveedores filtrada.
     */
    fun fetchSuppliers(query: String) {
        if (userId == null) {
            _uiState.value = SupplierUiState.Error("Usuario no autenticado.")
            return
        }

        _uiState.value = SupplierUiState.Loading

        viewModelScope.launch {
            try {
                // Llama al servicio de Retrofit con el userId y el término de búsqueda
                val response = RetrofitClient.proveedorService.getProveedores(userId, query)
                if (response.success && response.data != null) {
                    _uiState.value = SupplierUiState.Success(response.data)
                } else {
                    _uiState.value = SupplierUiState.Error(response.message ?: "No se encontraron proveedores.")
                }
            } catch (e: Exception) {
                // Error de red, parsing, etc.
                _uiState.value = SupplierUiState.Error("Error de red: ${e.message}")
            }
        }
    }
}