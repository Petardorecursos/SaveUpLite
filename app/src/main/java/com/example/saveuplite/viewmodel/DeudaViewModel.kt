package com.example.saveuplite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saveuplite.api.ApiService
import com.example.saveuplite.model.deuda.Deuda
import com.example.saveuplite.model.deuda.DeudaCreacion
import com.example.saveuplite.model.deuda.PagoDeuda
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class DeudaUiState(
    val isLoading: Boolean = false,
    val deudas: List<Deuda> = emptyList(),
    val errorMessage: String? = null,
    val operacionExitosa: Boolean = false
)

class DeudaViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(DeudaUiState())
    val uiState: StateFlow<DeudaUiState> = _uiState.asStateFlow()

    fun obtenerDeudas(rut: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.obtenerDeudasPorUsuario(rut)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            deudas = response.body() ?: emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al obtener las deudas: ${response.message()}"
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error de conexión. Revisa tu acceso a internet."
                    )
                }
            }
        }
    }

    fun crearDeuda(deudaCreacion: DeudaCreacion) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, operacionExitosa = false) }
            try {
                val response = apiService.crearDeuda(deudaCreacion)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(isLoading = false, operacionExitosa = true)
                    }
                    // Opcional: Recargar la lista de deudas
                    obtenerDeudas(deudaCreacion.usuarioRut)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al crear la deuda: ${response.message()}"
                        )
                    }
                }
            } catch (e: IOException) {
                 _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error de conexión al crear la deuda."
                    )
                }
            }
        }
    }

    fun registrarPago(deudaId: Long, rutUsuario: String, pago: PagoDeuda) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, operacionExitosa = false) }
            try {
                val response = apiService.registrarPagoDeuda(deudaId, pago)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, operacionExitosa = true) }
                    // Recargar la lista para reflejar el pago
                    obtenerDeudas(rutUsuario)
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Error al registrar el pago.")
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error de conexión al registrar el pago.")
                }
            }
        }
    }
    
    fun resetOperacionExitosa() {
        _uiState.update { it.copy(operacionExitosa = false) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
