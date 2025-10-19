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

    /**
     * Carga los datos iniciales del saldo (el monto actual y el historial) para un usuario.
     */
    fun cargarDatosSaldo(usuarioRut: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Obtener saldo y historial en un hilo de fondo
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

    /**
     * Registra un nuevo movimiento (ingreso o gasto) y actualiza el estado.
     */
    fun agregarMovimiento(usuarioRut: String, montoMovimiento: Float, tipo: EventoSaldo) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            if (montoMovimiento <= 0) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "El monto debe ser positivo.") }
                return@launch
            }

            // --- Lógica de Negocio Principal ---
            // 1. Obtener el saldo actual desde el estado
            val saldoAnterior = _uiState.value.saldoActual

            // 2. Calcular el nuevo saldo total
            val nuevoSaldoTotal = when (tipo) {
                EventoSaldo.INGRESO -> saldoAnterior + montoMovimiento
                EventoSaldo.GASTO -> saldoAnterior - montoMovimiento
                else -> saldoAnterior // No modificar para otros tipos por ahora
            }
            
            if (nuevoSaldoTotal < 0) {
                 _uiState.update { it.copy(isLoading = false, errorMessage = "No tienes saldo suficiente.") }
                return@launch
            }

            // 3. Crear el nuevo registro de Saldo
            val nuevoMovimiento = Saldo(
                idSaldo = 0, // El ID es autoincremental, la BD lo asignará
                monto = nuevoSaldoTotal, // Guardamos el nuevo total
                fechaRegistro = Date(),
                usuarioRut = usuarioRut,
                tipoEvento = tipo
            )

            // 4. Guardar en la base de datos
            val success = withContext(Dispatchers.IO) {
                dbHelper.insertarSaldo(nuevoMovimiento)
            }

            // 5. Actualizar la UI
            if (success) {
                // Volver a cargar los datos para reflejar el cambio
                cargarDatosSaldo(usuarioRut)
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al guardar el movimiento.") }
            }
        }
    }
}
