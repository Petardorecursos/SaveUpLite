package com.example.saveuplite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saveuplite.api.RetrofitClient
import com.example.saveuplite.model.ConfiguracionPresupuesto
import com.example.saveuplite.model.meta.MetaAhorro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlanificacionUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val config: ConfiguracionPresupuesto? = null,
    val metas: List<MetaAhorro> = emptyList(),
    val needs: Float = 0.50f,
    val wants: Float = 0.30f,
    val savings: Float = 0.20f,
    val simulationAmount: String = "",
    val simulationResults: Map<String, Double> = emptyMap(),
    // State for Goal Assignments (MetaId -> Float Percentage 0.0-1.0)
    val goalAssignments: Map<Long, Float> = emptyMap(),
    val saveSuccess: Boolean = false // To trigger toast
)

class PlanificacionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PlanificacionUiState())
    val uiState = _uiState.asStateFlow()

    fun cargarDatos(rut: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val configResp = RetrofitClient.apiService.obtenerConfiguracionPresupuesto(rut)
                val metasResp = RetrofitClient.apiService.obtenerMetas(rut)

                var currentConfig = if (configResp.isSuccessful && configResp.body() != null) configResp.body() else null
                val userMetas = metasResp.body() ?: emptyList()

                // Initialize goal assignments. 
                // Currently backend response ConfiguracionPresupuesto doesn't return the list of assignments yet (it wasn't in the entity response DTO).
                // So we default to Equal Distribution if we can't load them, OR (improved future) we should fetch them.
                // For this MVP step, we will distribute equally if no config exists, or clear it.
                // NOTE: To properly load existing assignments, we would need the backend endpoint to return them nested or a separate call.
                // Based on previous edits, ConfiguracionPresupuesto Entity has OneToMany but we might need to check if it's serialized.
                // Let's assume for now we start fresh or equal distribution to simplify this session unless user asks otherwise.
                
                val defaultAssignment = if (userMetas.isNotEmpty()) 1f / userMetas.size else 0f
                val initialAssignments = userMetas.associate { it.id to defaultAssignment }

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        config = currentConfig,
                        metas = userMetas,
                        goalAssignments = initialAssignments,
                        needs = currentConfig?.porcentajeNecesidades?.toFloat()?.div(100) ?: 0.50f,
                        wants = currentConfig?.porcentajeDeseos?.toFloat()?.div(100) ?: 0.30f,
                        savings = currentConfig?.porcentajeAhorro?.toFloat()?.div(100) ?: 0.20f
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateDist(n: Float, w: Float, s: Float) {
        _uiState.update { it.copy(needs = n, wants = w, savings = s) }
        calculateSimulation()
    }
    
    fun updateGoalAssignment(metaId: Long, percentage: Float) {
        val current = _uiState.value.goalAssignments.toMutableMap()
        current[metaId] = percentage
        _uiState.update { it.copy(goalAssignments = current) }
    }
    
    // Auto-balance goals to 100% (simple helper) if needed, or leave manual validation to UI.

    fun setSimulationAmount(amount: String) {
        _uiState.update { it.copy(simulationAmount = amount) }
        calculateSimulation()
    }

    private fun calculateSimulation() {
        val amount = _uiState.value.simulationAmount.toDoubleOrNull() ?: 0.0
        val results = mapOf(
            "Necesidades" to amount * _uiState.value.needs,
            "Deseos" to amount * _uiState.value.wants,
            "Ahorro" to amount * _uiState.value.savings
        )
        _uiState.update { it.copy(simulationResults = results) }
    }
    
    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun saveConfiguration(rut: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, saveSuccess = false) }
            try {
                // Validate Goal Assignments sum to 100% (or roughly 1.0)
                val sumGoals = _uiState.value.goalAssignments.values.sum()
                if (kotlin.math.abs(sumGoals - 1.0f) > 0.05f && _uiState.value.metas.isNotEmpty()) {
                     _uiState.update { it.copy(isLoading = false, errorMessage = "La distribución de metas debe sumar 100% (Suma actual: ${(sumGoals*100).toInt()}%)") }
                     return@launch
                }

                // Map assignments DTO
                val assignmentsList = _uiState.value.goalAssignments.map { (id, pct) ->
                    com.example.saveuplite.model.AsignacionPresupuestoDTO(
                        metaId = id,
                        porcentaje = pct * 100.0
                    )
                }

                val dto = com.example.saveuplite.model.ConfiguracionPresupuestoDTO(
                    porcentajeNecesidades = _uiState.value.needs * 100.0,
                    porcentajeDeseos = _uiState.value.wants * 100.0,
                    porcentajeAhorro = _uiState.value.savings * 100.0,
                    activo = true,
                    asignaciones = assignmentsList
                )
                
                val response = RetrofitClient.apiService.guardarConfiguracionPresupuesto(rut, dto)
                if (response.isSuccessful) {
                   _uiState.update { it.copy(saveSuccess = true) }
                } else {
                    _uiState.update { it.copy(errorMessage = "Error al guardar: ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error de red: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
