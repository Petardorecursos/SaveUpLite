package com.example.saveuplite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saveuplite.api.FrankfurterRetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class para representar el estado de la UI de la pantalla de Conversor.
 */
data class ConverterUiState(
    val currencies: Map<String, String> = emptyMap(),
    val amount: String = "1.0",
    val fromCurrency: String = "USD",
    val toCurrency: String = "CLP",
    val conversionResult: Double? = null,
    val isLoading: Boolean = false,
    val isLoadingCurrencies: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel para la pantalla de Conversor de Monedas.
 */
class ConverterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    init {
        loadCurrencies()
    }

    /**
     * Carga la lista de monedas disponibles desde la API.
     */
    private fun loadCurrencies() {
        _uiState.value = _uiState.value.copy(isLoadingCurrencies = true)
        viewModelScope.launch {
            try {
                val response = FrankfurterRetrofitClient.instance.getCurrencies()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(currencies = response.body()!!, isLoadingCurrencies = false)
                    // Realiza una conversión inicial al cargar las monedas
                    performConversion()
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = "Error al cargar las monedas.", isLoadingCurrencies = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error de conexión: ${e.message}", isLoadingCurrencies = false)
            }
        }
    }

    /**
     * Realiza la conversión de moneda utilizando los valores actuales del estado.
     */
    fun performConversion() {
        // ¡SOLUCIÓN! Reemplaza la coma por un punto para asegurar la conversión a Double.
        val sanitizedAmount = _uiState.value.amount.replace(',', '.')
        val amountToConvert = sanitizedAmount.toDoubleOrNull()

        if (amountToConvert == null || amountToConvert == 0.0) {
            _uiState.value = _uiState.value.copy(conversionResult = 0.0)
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = FrankfurterRetrofitClient.instance.getLatestConversion(
                    amount = amountToConvert,
                    from = _uiState.value.fromCurrency,
                    to = _uiState.value.toCurrency
                )
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!.rates[_uiState.value.toCurrency]
                    _uiState.value = _uiState.value.copy(conversionResult = result, isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = "Error en la conversión.", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error de conexión: ${e.message}", isLoading = false)
            }
        }
    }

    // --- Funciones para actualizar el estado desde la UI ---

    fun onAmountChange(newAmount: String) {
        _uiState.value = _uiState.value.copy(amount = newAmount)
    }

    fun onFromCurrencyChange(newCurrency: String) {
        _uiState.value = _uiState.value.copy(fromCurrency = newCurrency)
    }

    fun onToCurrencyChange(newCurrency: String) {
        _uiState.value = _uiState.value.copy(toCurrency = newCurrency)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
