package com.example.saveuplite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saveuplite.api.FrankfurterRetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private var conversionJob: Job? = null

    init {
        loadCurrencies()
    }

    /**
     * Carga la lista de monedas disponibles y establece el estado inicial para la conversión.
     */
    private fun loadCurrencies() {
        _uiState.value = _uiState.value.copy(isLoadingCurrencies = true)
        viewModelScope.launch {
            try {
                val response = FrankfurterRetrofitClient.instance.getCurrencies()
                if (response.isSuccessful && response.body() != null) {
                    val currencies = response.body()!!.toMutableMap()
                    // Asegurar que las monedas por defecto siempre existan en la lista
                    currencies.putIfAbsent("USD", "United States Dollar")
                    currencies.putIfAbsent("CLP", "Chilean Peso")

                    // Establecer explícitamente el estado con las monedas y la selección por defecto
                    _uiState.value = _uiState.value.copy(
                        currencies = currencies,
                        fromCurrency = "USD",
                        toCurrency = "CLP",
                        isLoadingCurrencies = false
                    )
                    // Realizar la conversión inicial con el estado ya garantizado
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
    private fun performConversion() {
        conversionJob?.cancel()

        val sanitizedAmount = _uiState.value.amount.replace(',', '.')
        val amountToConvert = sanitizedAmount.toDoubleOrNull()

        if (amountToConvert == null || amountToConvert <= 0.0) {
            _uiState.value = _uiState.value.copy(conversionResult = null)
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        conversionJob = viewModelScope.launch {
            delay(300) 
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
        val regex = "^[0-9]*[.,]?[0-9]*$".toRegex()
        if (regex.matches(newAmount)) {
            _uiState.value = _uiState.value.copy(amount = newAmount)
            performConversion()
        }
    }

    fun onFromCurrencyChange(newCurrency: String) {
        _uiState.value = _uiState.value.copy(fromCurrency = newCurrency)
        performConversion()
    }

    fun onToCurrencyChange(newCurrency: String) {
        _uiState.value = _uiState.value.copy(toCurrency = newCurrency)
        performConversion()
    }

    fun swapCurrencies() {
        val currentFrom = _uiState.value.fromCurrency
        val currentTo = _uiState.value.toCurrency
        _uiState.value = _uiState.value.copy(fromCurrency = currentTo, toCurrency = currentFrom)
        performConversion()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
