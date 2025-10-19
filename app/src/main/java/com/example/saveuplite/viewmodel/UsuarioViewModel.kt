package com.example.saveuplite.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saveuplite.data.DatabaseHelper
import com.example.saveuplite.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

// Estado de la UI: representa cómo se debe ver la pantalla en un momento dado.
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val currentUser: Usuario? = null
)

/**
 * ViewModel para manejar la lógica de negocio relacionada con el Usuario.
 *
 * Este ViewModel se encarga de la autenticación y el registro,
 * validando los datos y actualizando el estado de la UI para que la pantalla reaccione.
 */
class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)

    // _uiState es privado y mutable, solo el ViewModel puede cambiarlo.
    private val _uiState = MutableStateFlow(AuthUiState())
    // uiState es público e inmutable, la UI solo puede leerlo.
    val uiState = _uiState.asStateFlow()

    /**
     * Inicia sesión de un usuario contra la base de datos local.
     */
    fun login(email: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            if (email.isBlank() || contrasena.isBlank()) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "El correo y la contraseña son obligatorios.") }
                return@launch
            }

            // Operación de base de datos en un hilo de fondo
            val user = withContext(Dispatchers.IO) {
                dbHelper.obtenerAuthUsuarioPorEmail(email)
            }

            if (user != null && user.contrasena == contrasena) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = user
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Credenciales inválidas.") }
            }
        }
    }

    /**
     * Registra un nuevo usuario en la base de datos.
     */
    fun register(rut: String, nombre: String, apellido: String, email: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Validaciones (puedes añadir más)
            if (rut.isBlank() || nombre.isBlank() || apellido.isBlank() || email.isBlank() || contrasena.isBlank()) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Todos los campos son obligatorios.") }
                return@launch
            }

            val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{7,20}$".toRegex()
            if (!contrasena.matches(passwordRegex)) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "La contraseña debe tener entre 7 y 20 caracteres, con al menos una letra y un número.") }
                return@launch
            }

            // Verificar si el usuario ya existe
            val existingUser = withContext(Dispatchers.IO) {
                dbHelper.obtenerAuthUsuarioPorEmail(email)
            }

            if (existingUser != null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "El correo electrónico ya está registrado.") }
                return@launch
            }

            // Crear y guardar el nuevo usuario
            val newUser = Usuario(
                rut = rut,
                nombre = nombre,
                apellido = apellido,
                email = email,
                contrasena = contrasena, // En una app real, la contraseña debería ser hasheada
                fechaRegistro = Date()
            )

            val success = withContext(Dispatchers.IO) {
                dbHelper.insertarAuthUsuario(newUser)
            }

            if (success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true, // Auto-login después de registrar
                        currentUser = newUser
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al registrar el usuario.") }
            }
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun logout() {
        _uiState.value = AuthUiState() // Resetea al estado inicial
    }
}
