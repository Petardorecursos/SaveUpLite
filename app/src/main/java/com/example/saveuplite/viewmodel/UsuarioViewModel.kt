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

    // El dbHelper se mantiene, ya que lo usaremos para el login y otras funciones locales.
    private val dbHelper = DatabaseHelper(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun fetchUsuarioFromApi(rut: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getUsuarioByRut(rut)
                }
                _uiState.update { it.copy(isLoading = false, currentUser = user) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al obtener usuario: ${e.message}") }
            }
        }
    }

    fun login(email: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // NOTA: El login todavía es local. Un siguiente paso sería hacerlo contra la API.
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

    // ======================================================================================
    // === FUNCIÓN "REGISTER" ACTUALIZADA PARA USAR LA API REST =============================
    // ======================================================================================
    fun register(rut: String, nombre: String, apellido: String, email: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Crear el objeto Usuario para enviarlo a la API.
            //    La fecha de registro puede ser generada por el backend, pero enviarla no causa problemas.
            val newUser = Usuario(
                rut = rut,
                nombre = nombre,
                apellido = apellido,
                email = email,
                contrasena = contrasena, // La contraseña se envía en texto plano a la API; el backend se encarga de hashearla.
                fechaRegistro = Date()
            )

            try {
                // 2. Ejecutar la llamada a la API en el hilo de I/O.
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.registerUsuario(newUser)
                }

                // 3. Manejar la respuesta del servidor.
                if (response.isSuccessful) {
                    // CÓDIGO 201 (CREATED): El registro fue exitoso.
                    // Autenticamos al usuario y lo guardamos en el estado.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = newUser.copy(contrasena = null) // Guardamos el usuario en el estado sin la contraseña.
                        )
                    }
                } else {
                    // CÓDIGO 409 (CONFLICT) u otro error del servidor.
                    val errorMessage = when (response.code()) {
                        409 -> "El RUT o el correo electrónico ya están registrados."
                        else -> "Error en el registro (Código: ${response.code()})."
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMessage) }
                }

            } catch (e: Exception) {
                // 4. Manejar errores de conexión (ej. sin internet, servidor caído).
                _uiState.update { it.copy(isLoading = false, errorMessage = "No se pudo conectar al servidor: ${e.message}") }
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
