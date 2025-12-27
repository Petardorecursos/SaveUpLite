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
    val categorias: List<CategoriaDTO> = emptyList(),
    val totalIngresos: Double = 0.0,
    val totalGastos: Double = 0.0,
    val selectedDate: java.time.LocalDate = java.time.LocalDate.now() // Nuevo campo: Fecha seleccionada para el filtro
)

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    // Lista completa de movimientos en memoria para filtrar sin recargar la API cada vez si no es necesario (opcional, pero por ahora recargamos todo para asegurar consistencia)
    private var allMovimientos: List<MovimientoResponseDTO> = emptyList()

    /**
     * Carga el saldo y el historial de movimientos desde la API.
     * Ahora trae un límite mayor (o todos) para permitir filtrado local por fecha.
     */
    fun cargarDatosDashboard(rut: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Llamadas a la API
                // Solicitamos un límite alto (ej. 1000) para tener historial suficiente para filtrar por meses anteriores.
                // Idealmente la API soportaría filtros de fecha.
                val saldoResponse = RetrofitClient.apiService.obtenerSaldoActual(rut)
                val movimientosResponse = RetrofitClient.apiService.obtenerMovimientosPorUsuario(rut, limit = 1000)

                if (saldoResponse.isSuccessful && saldoResponse.body() != null &&
                    movimientosResponse.isSuccessful && movimientosResponse.body() != null) {
                    
                    allMovimientos = movimientosResponse.body()!!
                    
                    // Calculamos los totales basados en la fecha seleccionada actual
                    recalcularTotalesPorFecha(_uiState.value.selectedDate)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            saldoActual = saldoResponse.body()!!.saldo,
                            historialMovimientos = allMovimientos.take(10), // Para el historial mostramos solo los últimos 10 reales
                        )
                    }
                } else {
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

    /**
     * Cambia la fecha de filtro y recalcula los totales.
     */
    fun cambiarFechaFiltro(nuevaFecha: java.time.LocalDate) {
        _uiState.update { it.copy(selectedDate = nuevaFecha) }
        recalcularTotalesPorFecha(nuevaFecha)
    }

    private fun recalcularTotalesPorFecha(fecha: java.time.LocalDate) {
        // Filtrar movimientos que correspondan al mes y año de 'fecha'
        val movimientosMes = allMovimientos.filter { mov ->
             // Convertir Date legacy a LocalDate
             val movDate = mov.fecha.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
             movDate.month == fecha.month && movDate.year == fecha.year
        }

        val ingresos = movimientosMes.filter { it.monto > 0 }.sumOf { it.monto }
        val gastos = movimientosMes.filter { it.monto < 0 }.sumOf { kotlin.math.abs(it.monto) }

        _uiState.update {
            it.copy(
                totalIngresos = ingresos,
                totalGastos = gastos
            )
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
