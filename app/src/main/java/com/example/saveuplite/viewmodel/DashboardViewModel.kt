package com.example.saveuplite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saveuplite.api.RetrofitClient
import com.example.saveuplite.model.dto.MovimientoRegistroDTO
import com.example.saveuplite.model.dto.MovimientoResponseDTO
import com.example.saveuplite.model.dto.CategoriaDTO
import com.example.saveuplite.model.enums.TipoMovimiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

// 1. Estado de la UI para el Dashboard, desacoplado del modelo antiguo.
data class DashboardUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saldoActual: Double = 0.0,
    val historialMovimientos: List<MovimientoResponseDTO> = emptyList(),
    val categorias: List<CategoriaDTO> = emptyList() // Lista de categorías
)

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Carga el saldo y el historial de movimientos desde la API.
     * AHORA PIDE SOLO LOS ÚLTIMOS 10 MOVIMIENTOS PARA EL DASHBOARD.
     */
    fun cargarDatosDashboard(rut: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Llamadas a la API
                val saldoResponse = RetrofitClient.apiService.obtenerSaldoActual(rut)
                val movimientosResponse = RetrofitClient.apiService.obtenerMovimientosPorUsuario(rut, limit = 10) // <-- ¡NUEVO CAMBIO!

                if (saldoResponse.isSuccessful && saldoResponse.body() != null &&
                    movimientosResponse.isSuccessful && movimientosResponse.body() != null) {
                    // Éxito en ambas llamadas
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            saldoActual = saldoResponse.body()!!.saldo,
                            historialMovimientos = movimientosResponse.body()!!
                        )
                    }
                } else {
                    // Error en alguna de las llamadas
                    val errorMsg = "Error: ${saldoResponse.code()} / ${movimientosResponse.code()}"
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                }

            } catch (e: IOException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error de conexión. Revisa tu red.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error inesperado: ${e.message}") }
            }
        }
    }

    fun cargarCategorias() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getCategorias()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.update { it.copy(categorias = response.body()!!) }
                } else {
                    _uiState.update { it.copy(errorMessage = "Error al cargar categorías: ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error de red al cargar categorías: ${e.message}") }
            }
        }
    }

    /**
     * Registra un nuevo movimiento (ingreso o gasto) a través de la API.
     */
    fun registrarMovimiento(rut: String, monto: Double, descripcion: String, tipo: TipoMovimiento, categoriaId: Long? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // El backend espera que los gastos sean negativos.
            val montoCorregido = if (tipo == TipoMovimiento.GASTO_GENERAL) -kotlin.math.abs(monto) else kotlin.math.abs(monto)

            val dto = MovimientoRegistroDTO(
                monto = montoCorregido,
                descripcion = descripcion,
                tipoMovimiento = tipo,
                usuarioRut = rut,
                categoriaId = categoriaId
            )

            try {
                val response = RetrofitClient.apiService.registrarMovimiento(dto)
                if (response.isSuccessful) {
                    // Al registrar con éxito, recargamos los datos para reflejar el cambio.
                    cargarDatosDashboard(rut)
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error al registrar (Código: ${response.code()})") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error de conexión: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
