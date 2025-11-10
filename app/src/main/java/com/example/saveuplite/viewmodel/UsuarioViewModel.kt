package com.example.saveuplite.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saveuplite.api.RetrofitClient
import com.example.saveuplite.data.DatabaseHelper
import com.example.saveuplite.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val currentUser: Usuario? = null
)

class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Obtiene los datos de un usuario desde la API del backend.
     * Actualiza el `currentUser` en el `AuthUiState`.
     *
     * @param rut El RUT del usuario a buscar.
     */
    fun fetchUsuarioFromApi(rut: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Ejecuta la llamada a la API en el hilo de IO
                val user = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getUsuarioByRut(rut)
                }
                // Actualiza el estado con el usuario obtenido de la API
                _uiState.update { it.copy(isLoading = false, currentUser = user) }

            } catch (e: Exception) {
                // En caso de error (red, 404, etc.), actualiza el estado con un mensaje
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al obtener usuario: ${e.message}") }
            }
        }
    }

    fun login(email: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val user = withContext(Dispatchers.IO) {
                dbHelper.obtenerAuthUsuarioPorEmail(email)
            }

            if (user != null && user.contrasena == contrasena) {
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true, currentUser = user) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Credenciales inválidas.") }
            }
        }
    }

    fun register(rut: String, nombre: String, apellido: String, email: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // --- Validaciones Previas en ViewModel ---
            val rutExists = withContext(Dispatchers.IO) { dbHelper.usuarioExistePorRut(rut) }
            if (rutExists) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "El RUT ya está registrado.") }
                return@launch
            }

            val emailExists = withContext(Dispatchers.IO) { dbHelper.usuarioExistePorEmail(email) }
            if (emailExists) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "El correo electrónico ya está registrado.") }
                return@launch
            }

            val newUser = Usuario(
                rut = rut, nombre = nombre, apellido = apellido, email = email, 
                contrasena = contrasena, // En una app real, la contraseña debería ser hasheada
                fechaRegistro = Date()
            )

            val success = withContext(Dispatchers.IO) {
                dbHelper.insertarAuthUsuario(newUser)
            }

            if (success) {
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true, currentUser = newUser) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al registrar el usuario.") }
            }
        }
    }

    fun logout() {
        _uiState.value = AuthUiState()
    }
    
    fun clearErrors() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
