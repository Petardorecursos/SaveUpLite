package com.example.saveuplite.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saveuplite.data.DatabaseHelper
import com.example.saveuplite.model.EventoSaldo
import com.example.saveuplite.model.Saldo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

// Estado de la UI para la pantalla de Saldo
data class SaldoUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saldoActual: Float = 0.0f,
    val historialMovimientos: List<Saldo> = emptyList()
)

class SaldoViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)

    private val _uiState = MutableStateFlow(SaldoUiState())
    val uiState = _uiState.asStateFlow()

    fun cargarDatosSaldo(usuarioRut: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val saldo = withContext(Dispatchers.IO) {
                dbHelper.obtenerSaldoActual(usuarioRut)
            }
            val historial = withContext(Dispatchers.IO) {
                dbHelper.obtenerSaldosPorUsuario(usuarioRut)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    saldoActual = saldo,
                    historialMovimientos = historial
                )
            }
        }
    }

    fun agregarMovimiento(usuarioRut: String, montoMovimiento: Float, tipo: EventoSaldo) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            if (montoMovimiento <= 0) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "El monto debe ser positivo.") }
                return@launch
            }

            // --- Lógica de Negocio Corregida ---
            val saldoActual = _uiState.value.saldoActual
            if (tipo == EventoSaldo.GASTO && montoMovimiento > saldoActual) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "No tienes saldo suficiente.") }
                return@launch
            }

            // 1. Determinar el monto a guardar (positivo o negativo)
            val montoParaGuardar = if (tipo == EventoSaldo.INGRESO) montoMovimiento else -montoMovimiento

            // 2. Crear el nuevo registro de Saldo con el monto del MOVIMIENTO
            val nuevoMovimiento = Saldo(
                idSaldo = 0, // La BD lo asigna
                monto = montoParaGuardar, // Guardamos el monto del movimiento, no el total
                fechaRegistro = Date(),
                usuarioRut = usuarioRut,
                tipoEvento = tipo
            )

            // 3. Guardar en la base de datos
            val success = withContext(Dispatchers.IO) {
                dbHelper.insertarSaldo(nuevoMovimiento)
            }

            // 4. Actualizar la UI
            if (success) {
                cargarDatosSaldo(usuarioRut) // Recargar todo para reflejar el cambio
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al guardar el movimiento.") }
            }
        }
    }
}
