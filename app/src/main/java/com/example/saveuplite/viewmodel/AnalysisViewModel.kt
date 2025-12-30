package com.example.saveuplite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saveuplite.api.RetrofitClient
import com.example.saveuplite.model.dto.MovimientoResponseDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalysisViewModel : ViewModel() {

    private val _movimientos = MutableStateFlow<List<MovimientoResponseDTO>>(emptyList())
    val movimientos = _movimientos.asStateFlow()
    
    // Filtro de fecha
    private val _selectedDate = MutableStateFlow(java.time.LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()
    
    // Estado de ejecución presupuestaria para Insights
    private val _ejecucionPresupuesto = MutableStateFlow<com.example.saveuplite.model.dto.EjecucionPresupuestoDTO?>(null)
    val ejecucionPresupuesto = _ejecucionPresupuesto.asStateFlow()

    fun cambiarFecha(novaFecha: java.time.LocalDate) {
        _selectedDate.value = novaFecha
        // Recargar ejecución al cambiar fecha
        // Necesitamos el RUT, idealmente lo guardaríamos en el VM o se pasa como argumento.
        // Por simplicidad, asumiremos que se recarga desde la UI o guardamos lastRut
    }
    
    private var lastRut: String? = null

    fun obtenerMovimientos(rut: String) {
        lastRut = rut
        viewModelScope.launch {
            try {
                // 1. Fetch Movimientos
                val response = RetrofitClient.apiService.obtenerMovimientosPorUsuario(rut, limit = 1000)
                if (response.isSuccessful && response.body() != null) {
                    _movimientos.value = response.body()!!
                }
                
                // 2. Fetch Ejecución Presupuestaria (para Insights)
                val date = _selectedDate.value
                val execResponse = RetrofitClient.apiService.obtenerEjecucionPresupuesto(rut, date.monthValue, date.year)
                if (execResponse.isSuccessful) {
                    _ejecucionPresupuesto.value = execResponse.body()
                } else {
                    _ejecucionPresupuesto.value = null
                }
                
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun recargarEjecucionFecha(date: java.time.LocalDate) {
        _selectedDate.value = date
        lastRut?.let { 
            viewModelScope.launch {
                try {
                     // Solo recargamos Insights si cambiamos fecha, movimientos ya los tenemos en memoria (suponiendo limit 1000 cubre el mes)
                     // Opcional: Recargar movimientos si se quiere ser estricto.
                     val execResponse = RetrofitClient.apiService.obtenerEjecucionPresupuesto(it, date.monthValue, date.year)
                    if (execResponse.isSuccessful) {
                        _ejecucionPresupuesto.value = execResponse.body()
                    }
                } catch (e: Exception) { }
            }
        }
    }
}
