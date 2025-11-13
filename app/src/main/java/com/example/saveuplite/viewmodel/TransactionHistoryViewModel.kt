package com.example.saveuplite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saveuplite.api.RetrofitClient
import com.example.saveuplite.model.dto.MovimientoResponseDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Estado de la UI para la pantalla de historial de transacciones.
 */
data class TransactionHistoryUiState(
    val isLoading: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val errorMessage: String? = null,
    val movements: List<MovimientoResponseDTO> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 1,
) {
    // Propiedad computada para saber si se puede cargar la siguiente página.
    val canLoadMore: Boolean get() = currentPage < totalPages - 1 && !isLoading && !isLoadingNextPage
}

/**
 * ViewModel para la pantalla de historial de transacciones, con lógica de paginación.
 */
class TransactionHistoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Carga la primera página de movimientos.
     */
    fun loadInitialMovements(rut: String) {
        // Evita recargar si ya se está cargando.
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val response = RetrofitClient.apiService.obtenerMovimientosPaginados(rut, page = 0, size = 50)

                if (response.isSuccessful && response.body() != null) {
                    val pageData = response.body()!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            movements = pageData.content,
                            currentPage = pageData.currentPage,
                            totalPages = pageData.totalPages
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error al cargar el historial.") }
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error de conexión.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error inesperado: ${e.message}") }
            }
        }
    }

    /**
     * Carga la siguiente página de movimientos.
     */
    fun loadNextPage(rut: String) {
        // Previene cargas múltiples si ya está cargando o si no hay más páginas.
        if (!_uiState.value.canLoadMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingNextPage = true) }

            try {
                val nextPage = _uiState.value.currentPage + 1
                val response = RetrofitClient.apiService.obtenerMovimientosPaginados(rut, page = nextPage, size = 50)

                if (response.isSuccessful && response.body() != null) {
                    val pageData = response.body()!!
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoadingNextPage = false,
                            // Añade los nuevos movimientos a la lista existente.
                            movements = currentState.movements + pageData.content,
                            currentPage = pageData.currentPage,
                            totalPages = pageData.totalPages
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoadingNextPage = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingNextPage = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
