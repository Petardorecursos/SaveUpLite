package com.example.saveuplite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saveuplite.api.RetrofitClient
import com.example.saveuplite.model.meta.AbonoRetiro
import com.example.saveuplite.model.meta.MetaAhorro
import com.example.saveuplite.model.meta.MetaAhorroCreacion
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

data class MetaAhorroUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val metas: List<MetaAhorro> = emptyList(),
    val totalAhorrado: Double = 0.0
)

class MetaAhorroViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MetaAhorroUiState())
    val uiState = _uiState.asStateFlow()

    fun obtenerMetas(rut: String): Job { // Devuelve el Job para poder hacer .join()
        return viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = RetrofitClient.apiService.obtenerMetas(rut)
                if (response.isSuccessful && response.body() != null) {
                    val metas = response.body()!!
                    val total = metas.sumOf { it.montoAhorrado }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            metas = metas,
                            totalAhorrado = total
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error al obtener metas: ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error de red: ${e.message}") }
            }
        }
    }

    fun crearMeta(rut: String, nombre: String, monto: Double?, fecha: Date?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val dto = MetaAhorroCreacion(nombre, monto, fecha, rut)
            try {
                val response = RetrofitClient.apiService.crearMeta(dto)
                if (response.isSuccessful) {
                    obtenerMetas(rut).join() // Espera a que la lista se refresque
                    onSuccess() // Llama al callback para navegar y mostrar mensaje
                } else {
                     _uiState.update { it.copy(isLoading = false, errorMessage = "Error al crear meta: ${response.code()}") }
                }
            } catch (e: Exception) {
                 _uiState.update { it.copy(isLoading = false, errorMessage = "Error de red: ${e.message}") }
            }
        }
    }

    fun realizarAbono(rut: String, metaId: Long, monto: Double, descripcion: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val dto = AbonoRetiro(monto, descripcion)
            try {
                val response = RetrofitClient.apiService.abonarAMeta(metaId, dto)
                if (response.isSuccessful) {
                    obtenerMetas(rut).join()
                    onSuccess()
                } else {
                     _uiState.update { it.copy(isLoading = false, errorMessage = "Error al abonar: ${response.code()}") }
                }
            } catch (e: Exception) {
                 _uiState.update { it.copy(isLoading = false, errorMessage = "Error de red: ${e.message}") }
            }
        }
    }

    fun realizarRetiro(rut: String, metaId: Long, monto: Double, descripcion: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val dto = AbonoRetiro(monto, descripcion)
            try {
                val response = RetrofitClient.apiService.retirarDeMeta(metaId, dto)
                if (response.isSuccessful) {
                    obtenerMetas(rut).join()
                    onSuccess()
                } else {
                     _uiState.update { it.copy(isLoading = false, errorMessage = "Error al retirar: ${response.code()}") }
                }
            } catch (e: Exception) {
                 _uiState.update { it.copy(isLoading = false, errorMessage = "Error de red: ${e.message}") }
            }
        }
    }

    fun editarMeta(rut: String, metaId: Long, nombre: String, monto: Double?, fecha: Date?, onSuccess: () -> Unit) {
         viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val dto = MetaAhorroCreacion(nombre, monto, fecha, rut)
            try {
                val response = RetrofitClient.apiService.editarMeta(metaId, dto)
                if (response.isSuccessful) {
                    obtenerMetas(rut).join()
                    onSuccess()
                } else {
                     _uiState.update { it.copy(errorMessage = "Error al editar meta: ${response.code()}") }
                }
            } catch (e: Exception) {
                 _uiState.update { it.copy(isLoading = false, errorMessage = "Error de red: ${e.message}") }
            }
        }
    }

    fun eliminarMeta(rut: String, metaId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = RetrofitClient.apiService.eliminarMeta(metaId)
                if (response.isSuccessful) {
                    obtenerMetas(rut).join()
                    onSuccess()
                } else {
                     _uiState.update { it.copy(isLoading = false, errorMessage = "Error al eliminar meta: ${response.code()}") }
                }
            } catch (e: Exception) {
                 _uiState.update { it.copy(isLoading = false, errorMessage = "Error de red: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
