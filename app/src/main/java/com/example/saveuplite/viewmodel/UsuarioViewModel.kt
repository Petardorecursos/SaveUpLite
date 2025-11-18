package com.example.saveuplite.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saveuplite.api.ApiService // Importante
import com.example.saveuplite.model.Usuario
import com.example.saveuplite.model.dto.UsuarioLoginDTO
import com.example.saveuplite.model.dto.UsuarioRegistroDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import retrofit2.Response // Necesario para el mock

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val registrationSuccess: Boolean = false,
    val currentUser: Usuario? = null
)

// Ahora recibe ApiService en el constructor.
class UsuarioViewModel(
    private val apiService: ApiService,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun login(email: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val loginDTO = UsuarioLoginDTO(email, contrasena)

            try {
                // ¡Refactorizado! Usa la instancia inyectada de ApiService.
                val response = withContext(Dispatchers.IO) {
                    apiService.loginUsuario(loginDTO)
                }

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = user
                        )
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Credenciales inválidas. Verifica tu email y contraseña."
                        else -> "Error de autenticación (Código: ${response.code()})."
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMessage) }
                }

            } catch (e: IOException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "No se pudo conectar al servidor. Verifica tu conexión.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Ocurrió un error inesperado: ${e.message}") }
            }
        }
    }

    fun register(rut: String, nombre: String, apellido: String, email: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, registrationSuccess = false) }

            val userDTO = UsuarioRegistroDTO(
                rut = rut,
                nombre = nombre,
                apellido = apellido,
                email = email,
                contrasena = contrasena
            )

            try {
                // ¡Refactorizado! Usa la instancia inyectada de ApiService.
                val response = withContext(Dispatchers.IO) {
                    apiService.registerUsuario(userDTO)
                }

                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, registrationSuccess = true) }
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Datos inválidos. Revisa el formato del RUT, la contraseña o el email."
                        409 -> "El RUT o el correo electrónico ya se encuentran registrados."
                        else -> "Error en el registro (Código: ${response.code()}). Inténtalo de nuevo."
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMessage) }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "No se pudo conectar al servidor. Verifica tu conexión a internet.") }
            }
        }
    }

    fun logout() {
        _uiState.value = AuthUiState()
    }

    fun clearErrors() {
        _uiState.update { it.copy(errorMessage = null) }
    }



    fun resetRegistrationSuccess() {
        _uiState.update { it.copy(registrationSuccess = false) }
    }
}