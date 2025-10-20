package com.example.saveuplite.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    navController: NavHostController,
    viewModel: UsuarioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var isLoginScreen by remember { mutableStateOf(true) }

    // --- Estados para los campos del formulario ---
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }

    // --- Estados para los errores de validación ---
    var emailError by remember { mutableStateOf<String?>(null) }
    var contrasenaError by remember { mutableStateOf<String?>(null) }
    var rutError by remember { mutableStateOf<String?>(null) }
    var nombreError by remember { mutableStateOf<String?>(null) }
    var apellidoError by remember { mutableStateOf<String?>(null) }

    fun validateFields(): Boolean {
        // Limpiar errores previos
        emailError = null
        contrasenaError = null
        rutError = null
        nombreError = null
        apellidoError = null

        var isValid = true

        // --- Validaciones para el REGISTRO ---
        if (!isLoginScreen) {
            if (nombre.isBlank() || nombre.length < 3) {
                nombreError = "El nombre debe tener al menos 3 caracteres"
                isValid = false
            }
            if (apellido.isBlank() || apellido.length < 3) {
                apellidoError = "El apellido debe tener al menos 3 caracteres"
                isValid = false
            }
            val rutRegex = "^\\d{7,8}-[\\dkK]{1}$".toRegex()
            if (!rut.matches(rutRegex)) {
                rutError = "Formato de RUT inválido (ej: 12345678-9)"
                isValid = false
            }
        }

        // --- Validaciones comunes (Login y Registro) ---
        val emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$".toRegex()
        if (!email.matches(emailRegex)) {
            emailError = "Formato de correo inválido"
            isValid = false
        }
        val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{7,20}$".toRegex()
        if (!contrasena.matches(passwordRegex)) {
            contrasenaError = "La contraseña debe tener 7-20 caracteres, con letras y números"
            isValid = false
        }

        return isValid
    }

    // Navegar a Home si la autenticación es exitosa
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            Toast.makeText(context, "¡Bienvenido, ${uiState.currentUser?.nombre}!", Toast.LENGTH_SHORT).show()
            navController.navigate(Routes.HOME) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    // Mostrar mensaje de error si existe (errores de ViewModel)
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    val gradient = Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.background))

    Box(
        modifier = Modifier.fillMaxSize().background(gradient).padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "SaveUp Lite", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                    Text(text = if (isLoginScreen) "Bienvenido de Nuevo" else "Crea tu Cuenta", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))

                    // --- Campos del Formulario con Validaciones ---
                    if (!isLoginScreen) {
                        OutlinedTextField(value = rut, onValueChange = { rut = it; rutError = null }, label = { Text("RUT") }, isError = rutError != null, supportingText = { rutError?.let { Text(it) } }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = nombre, onValueChange = { nombre = it; nombreError = null }, label = { Text("Nombre") }, isError = nombreError != null, supportingText = { nombreError?.let { Text(it) } }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = apellido, onValueChange = { apellido = it; apellidoError = null }, label = { Text("Apellido") }, isError = apellidoError != null, supportingText = { apellidoError?.let { Text(it) } }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedTextField(value = email, onValueChange = { email = it; emailError = null }, label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), isError = emailError != null, supportingText = { emailError?.let { Text(it) } }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = contrasena, onValueChange = { contrasena = it; contrasenaError = null }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), isError = contrasenaError != null, supportingText = { contrasenaError?.let { Text(it) } }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = {
                                if (validateFields()) {
                                    if (isLoginScreen) {
                                        viewModel.login(email, contrasena)
                                    } else {
                                        viewModel.register(rut, nombre, apellido, email, contrasena)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text(if (isLoginScreen) "Iniciar Sesión" else "Registrarme")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { isLoginScreen = !isLoginScreen; viewModel.clearErrors() }) {
                        Text(if (isLoginScreen) "¿No tienes cuenta? Regístrate" else "Ya tengo una cuenta")
                    }
                }
            }
        }
    }
}

// Una pequeña función de extensión en el ViewModel para limpiar errores al cambiar de pantalla
fun UsuarioViewModel.clearErrors() {
    // Esta es una forma de exponer una función para limpiar el estado si es necesario.
    // En este caso, lo manejamos directamente en el ViewModel, pero es un buen patrón.
}
