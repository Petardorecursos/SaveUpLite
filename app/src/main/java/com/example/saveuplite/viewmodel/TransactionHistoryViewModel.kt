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

    private val _selectedDate = MutableStateFlow<java.time.LocalDate?>(null)
    val selectedDate = _selectedDate.asStateFlow()

    /**
     * Carga los movimientos. 
     * Si hay fecha seleccionada, carga historial reciente (limit 1000) y filtra.
     * Si no, usa paginación.
     */
    fun loadInitialMovements(rut: String) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, movements = emptyList()) } // Limpiar lista al recargar

            try {
                if (_selectedDate.value != null) {
                    // MODO FILTRO: Carga masiva y filtra en cliente
                    // Nota: Idealmente el backend tendría endpoint por fechas. Usamos limit 1000 como 'batch' seguro.
                    val response = RetrofitClient.apiService.obtenerMovimientosPorUsuario(rut, 1000)
                    if (response.isSuccessful && response.body() != null) {
                        val allMovs = response.body()!!
                        val filtered = allMovs.filter { 
                            val movDate = it.fecha.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                            val selDate = _selectedDate.value!!
                            movDate.month == selDate.month && movDate.year == selDate.year
                        }
                        _uiState.update { it.copy(isLoading = false, movements = filtered, totalPages = 1, currentPage = 0) }
                    } else {
                         _uiState.update { it.copy(isLoading = false, errorMessage = "Error al cargar historial.") }
                    }
                } else {
                    // MODO PAGINACIÓN (Default)
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
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Error al cargar historial.") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error: ${e.message}") }
            }
        }
    }

    /**
     * Carga la siguiente página de movimientos.
     * Deshabilitado si hay fecha seleccionada.
     */
    fun loadNextPage(rut: String) {
        if (_selectedDate.value != null) return // No paginar en modo filtro
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

    fun setSelectedDate(date: java.time.LocalDate?, rut: String) {
        _selectedDate.value = date
        loadInitialMovements(rut)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // --- Lógica de Descarga ---
    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    fun downloadReport(
        androidContext: android.content.Context, 
        rut: String, 
        isMonthly: Boolean
    ) {
        if (_isDownloading.value) return

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _isDownloading.value = true
            try {
                val alcance = if (isMonthly) "MENSUAL" else "COMPLETO"
                val date = _selectedDate.value ?: java.time.LocalDate.now()
                val mes = if (isMonthly) date.monthValue else null
                val anio = if (isMonthly) date.year else null

                val response = RetrofitClient.apiService.descargarReporte(rut, alcance, "CSV", mes, anio)

                if (response.isSuccessful && response.body() != null) {
                    val filename = "reporte_saveup_${if(isMonthly) "${date.monthValue}_${date.year}" else "completo"}.csv"
                    saveFileToDownloads(androidContext, response.body()!!, filename)
                    
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        android.widget.Toast.makeText(androidContext, "Reporte descargado correctamente", android.widget.Toast.LENGTH_LONG).show()
                    }
                } else {
                     kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        _uiState.update { it.copy(errorMessage = "Error al descargar reporte") }
                     }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _uiState.update { it.copy(errorMessage = "Error: ${e.message}") }
                }
            } finally {
                _isDownloading.value = false
            }
        }
    }

    private fun saveFileToDownloads(context: android.content.Context, body: okhttp3.ResponseBody, filename: String) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        body.byteStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            } else {
                // Legacy (api < 29) - Not strictly needed if app targets 29+, but simple fallback
                val target = java.io.File(
                    android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
                    filename
                )
                java.io.FileOutputStream(target).use { outputStream ->
                    body.byteStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
             throw e
        }
    }
}
