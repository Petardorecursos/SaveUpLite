package com.example.saveuplite.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.ui.theme.MediumBlue
import com.example.saveuplite.viewmodel.UsuarioViewModel

// --- Transformación visual para el RUT ---
private class RutVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 9) text.text.substring(0..8) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == trimmed.length - 2) {
                out += "-"
            }
        }

        val rutOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset >= trimmed.length - 1 && trimmed.length > 1) return offset + 1
                return offset
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset >= out.length) return trimmed.length
                if (out[offset] == '-') return offset - 1
                if (offset > trimmed.length - 1 && trimmed.length > 1) return offset - 1
                return offset
            }
        }

        return TransformedText(AnnotatedString(out), rutOffsetTranslator)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AuthScreen(
    navController: NavHostController,
    viewModel: UsuarioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var isLoginScreen by remember { mutableStateOf(true) }

    // --- Estados para los campos del formulario ---
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") } // Este estado guardará solo los dígitos
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }

    // --- Estados para los errores de validación locales ---
    var emailError by remember { mutableStateOf<String?>(null) }
    var contrasenaError by remember { mutableStateOf<String?>(null) }
    var rutError by remember { mutableStateOf<String?>(null) }
    var nombreError by remember { mutableStateOf<String?>(null) }
    var apellidoError by remember { mutableStateOf<String?>(null) }

    // Limpia los campos cuando cambiamos entre Login y Registro
    LaunchedEffect(isLoginScreen) {
        email = ""
        contrasena = ""
        rut = ""
        nombre = ""
        apellido = ""
        viewModel.clearErrors()
    }

    fun validateFields(rutToValidate: String): Boolean {
        // --- Limpieza automática de espacios ---
        email = email.trim()
        nombre = nombre.trim()
        apellido = apellido.trim()

        emailError = null
        contrasenaError = null
        rutError = null
        nombreError = null
        apellidoError = null
        var isValid = true

        if (!isLoginScreen) {
            if (nombre.isBlank() || nombre.length < 2) {
                nombreError = "El nombre debe tener al menos 2 caracteres"
                isValid = false
            }
            if (apellido.isBlank() || apellido.length < 2) {
                apellidoError = "El apellido debe tener al menos 2 caracteres"
                isValid = false
            }
            val rutRegex = "^\\d{7,8}-[\\dkK]{1}$".toRegex()
            if (!rutToValidate.matches(rutRegex)) {
                rutError = "Formato de RUT inválido (ej: 12.345.678-9)"
                isValid = false
            }
        }

        val emailRegex = "^[\\w-\\.+]*[\\w-]?@([\\w-]+\\.)+[\\w-]{2,4}$".toRegex()
        if (!email.matches(emailRegex)) {
            emailError = "Formato de correo inválido"
            isValid = false
        }
        val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$".toRegex()
        if (!contrasena.matches(passwordRegex)) {
            contrasenaError = "La contraseña debe tener 8-20 caracteres, con letras y números"
            isValid = false
        }
        return isValid
    }

    // Navegación en caso de login exitoso
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            Toast.makeText(context, "¡Bienvenido, ${uiState.currentUser?.nombre}!", Toast.LENGTH_SHORT).show()
            navController.navigate(Routes.HOME) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    // Manejo del registro exitoso
    LaunchedEffect(uiState.registrationSuccess) {
        if (uiState.registrationSuccess) {
            Toast.makeText(context, "¡Registro exitoso! Por favor, inicia sesión.", Toast.LENGTH_LONG).show()
            isLoginScreen = true // Cambia a la pantalla de login
            viewModel.resetRegistrationSuccess() // Resetea el estado
        }
    }

    // Mostrar errores del ViewModel
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearErrors()
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SaveUp Lite", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MediumBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = if (isLoginScreen) "Inicio de Sesión" else "Crea tu Cuenta",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))

                    if (!isLoginScreen) {
                        OutlinedTextField(
                            value = rut,
                            onValueChange = { newText ->
                                val cleaned = newText.filter { it.isDigit() || it.equals('k', ignoreCase = true) }
                                if (cleaned.length <= 9) {
                                    rut = cleaned
                                }
                                rutError = null
                            },
                            label = { Text("RUT") },
                            visualTransformation = RutVisualTransformation(),
                            isError = rutError != null,
                            supportingText = { rutError?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            colors = textFieldColors
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it; nombreError = null },
                            label = { Text("Nombre") },
                            isError = nombreError != null,
                            supportingText = { nombreError?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            colors = textFieldColors
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = apellido,
                            onValueChange = { apellido = it; apellidoError = null },
                            label = { Text("Apellido") },
                            isError = apellidoError != null,
                            supportingText = { apellidoError?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            colors = textFieldColors
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        isError = emailError != null,
                        supportingText = { emailError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = { contrasena = it; contrasenaError = null },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                        isError = contrasenaError != null,
                        supportingText = { contrasenaError?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                    Spacer(Modifier.height(24.dp))

                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                val rutToValidate = if (rut.length > 1) "${rut.dropLast(1)}-${rut.last().uppercaseChar()}" else rut
                                if (validateFields(rutToValidate)) {
                                    if (isLoginScreen) {
                                        viewModel.login(email, contrasena)
                                    } else {
                                        viewModel.register(rutToValidate, nombre, apellido, email, contrasena)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isLoginScreen) "Iniciar Sesión" else "Registrarme")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { isLoginScreen = !isLoginScreen }) {
                        Text(if (isLoginScreen) "¿No tienes cuenta? Regístrate" else "Ya tengo una cuenta")
                    }
                }
            }
        }
    }
}
