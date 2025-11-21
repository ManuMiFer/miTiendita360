// En: app/src/main/java/com/miranda/mitiendita360/ClienteViewModel.kt

package com.miranda.mitiendita360 // O com.miranda.mitiendita360.viewmodels si lo creaste

import androidx.compose.runtime.State // <-- CORRECCIÓN 1: Añadir este import
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.miranda.mitiendita360.models.Cliente // <-- CORRECCIÓN 2: Importar el nuevo modelo 'Cliente'
import com.miranda.mitiendita360.network.RetrofitClient
import kotlinx.coroutines.launch

class ClienteViewModel : ViewModel() {

    // <-- CORRECCIÓN 3: Usar el tipo de dato 'Cliente'
    private val _clientes = mutableStateOf<List<Cliente>>(emptyList())
    val clientes: State<List<Cliente>> = _clientes

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        getClientes()
    }

    fun getClientes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _error.value = "No se pudo obtener el ID del usuario. Inicie sesión de nuevo."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.clienteService.getClientes(userId)
                if (response.isSuccessful) {
                    // Ahora esto funciona porque el tipo de dato es el correcto
                    _clientes.value = (response.body() ?: emptyList()) as List<Cliente>
                } else {
                    _error.value = "Error al cargar los clientes: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de red: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
