package com.example.saveuplite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saveuplite.api.MindicadorRetrofitClient
import com.example.saveuplite.model.dto.IndicadorEconomicoDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class para representar el estado de la UI de la pantalla de Mercado.
 */
data class MarketUiState(
    val indicadores: List<IndicadorEconomicoDTO> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel para la pantalla de Mercado.
 * Se encarga de obtener y gestionar los datos de los indicadores económicos.
 */
class MarketViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    init {
        // Carga los indicadores en cuanto el ViewModel se inicializa.
        loadIndicadores()
    }

    fun loadIndicadores() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = MindicadorRetrofitClient.instance.getIndicadoresEconomicos()
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    // Convertimos el objeto de respuesta en una lista plana de indicadores
                    val listaIndicadores = listOf(
                        responseBody.uf,
                        responseBody.dolar,
                        responseBody.euro,
                        responseBody.utm,
                        responseBody.ipc
                    )
                    _uiState.value = _uiState.value.copy(isLoading = false, indicadores = listaIndicadores)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error al obtener los datos del servidor.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error de conexión: ${e.message}")
            }
        }
    }

    /**
     * Limpia el mensaje de error una vez que ha sido mostrado.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
