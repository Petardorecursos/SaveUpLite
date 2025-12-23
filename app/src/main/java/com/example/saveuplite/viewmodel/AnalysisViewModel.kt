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
    
    // In a real app we would have loading/error states here too

    fun obtenerMovimientos(rut: String) {
        viewModelScope.launch {
            try {
                // Fetch all history for analysis. 
                // Ideally backend provides an analysis endpoint (e.g. /api/stats/category)
                // For now we fetch list and aggregate client-side
                val response = RetrofitClient.apiService.obtenerMovimientosPorUsuario(rut)
                if (response.isSuccessful && response.body() != null) {
                    _movimientos.value = response.body()!!
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
