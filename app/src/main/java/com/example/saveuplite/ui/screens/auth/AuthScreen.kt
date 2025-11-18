package com.example.saveuplite.ui.screens.auth

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.saveuplite.R
import com.example.saveuplite.api.RetrofitClient
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.ui.theme.*
import com.example.saveuplite.viewmodel.AuthViewModelFactory
import com.example.saveuplite.viewmodel.UsuarioViewModel

// --- Transformación visual para el RUT (se mantiene sin cambios) ---
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isError: Boolean,
    errorText: String?,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = MediumGrayText) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = LightGrayText) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LavenderBlue,
            unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else Color.Transparent,
            unfocusedContainerColor = SoftWhite,
            focusedContainerColor = SoftWhite,
            errorContainerColor = PalePink.copy(alpha = 0.4f)
        ),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        isError = isError,
        supportingText = { 
            if (isError && errorText != null) {
                Text(errorText, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

// --- Pantalla de Autenticación Rediseñada ---

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthScreen(
    navController: NavHostController
) {
    val application = LocalContext.current.applicationContext as Application
    val factory = AuthViewModelFactory(RetrofitClient.apiService, application)
    val viewModel: UsuarioViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var isLoginScreen by remember { mutableStateOf(true) }

    // --- Estados para los campos del formulario ---
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
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
        email = ""; contrasena = ""; rut = ""; nombre = ""; apellido = "";
        viewModel.clearErrors()
        emailError = null; contrasenaError = null; rutError = null; nombreError = null; apellidoError = null;
    }

    fun validateFields(rutToValidate: String): Boolean {
        email = email.trim(); nombre = nombre.trim(); apellido = apellido.trim();

        emailError = null; contrasenaError = null; rutError = null; nombreError = null; apellidoError = null;
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
            navController.navigate(Routes.HOME) { popUpTo(navController.graph.startDestinationId) { inclusive = true } }
        }
    }

    // Manejo del registro exitoso
    LaunchedEffect(uiState.registrationSuccess) {
        if (uiState.registrationSuccess) {
            Toast.makeText(context, "¡Registro exitoso! Por favor, inicia sesión.", Toast.LENGTH_LONG).show()
            isLoginScreen = true
            viewModel.resetRegistrationSuccess()
        }
    }

    // Mostrar errores del ViewModel
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearErrors()
        }
    }

    // --- UI Rediseñada ---
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Parte superior con color e ilustración
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .background(PaleAqua),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.auth_background),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)),
                contentScale = ContentScale.Crop,
                alpha = 0.2f // Se aplica transparencia a la imagen
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Hola!", style = MaterialTheme.typography.titleLarge, color = DarkGrayText, fontWeight = FontWeight.Bold)
                Text("Bienvenido a", style = MaterialTheme.typography.bodyLarge, color = DarkGrayText.copy(alpha = 0.8f))
                Text("SaveUp", style = MaterialTheme.typography.displayMedium, color = DarkGrayText, fontWeight = FontWeight.ExtraBold)
            }
        }

        // Tarjeta del formulario
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Color.White)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isLoginScreen) "Login" else "Sign Up",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = DarkGrayText
            )
            Spacer(Modifier.height(24.dp))

            // --- FORMULARIO ---
            if (!isLoginScreen) {
                AuthTextField(rut, { rut = it; rutError = null }, "RUT", Icons.Outlined.Person, isError = rutError != null, errorText = rutError, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down)}), visualTransformation = RutVisualTransformation())
                Spacer(Modifier.height(16.dp))
                AuthTextField(nombre, { nombre = it; nombreError = null }, "Nombre", Icons.Outlined.Person, isError = nombreError != null, errorText = nombreError, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down)}))
                Spacer(Modifier.height(16.dp))
                AuthTextField(apellido, { apellido = it; apellidoError = null }, "Apellido", Icons.Outlined.Person, isError = apellidoError != null, errorText = apellidoError, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down)}))
                Spacer(Modifier.height(16.dp))
            }

            AuthTextField(email, { email = it; emailError = null }, "Email", Icons.Outlined.Email, isError = emailError != null, errorText = emailError, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down)}))
            Spacer(Modifier.height(16.dp))
            AuthTextField(contrasena, { contrasena = it; contrasenaError = null }, "Contraseña", Icons.Outlined.Lock, isError = contrasenaError != null, errorText = contrasenaError, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }), visualTransformation = PasswordVisualTransformation())
            Spacer(Modifier.height(12.dp))

            if (isLoginScreen) {
                TextButton(onClick = { /* TODO */ }, modifier = Modifier.align(Alignment.End)) {
                    Text("Olvidé mi contraseña", color = LavenderBlue, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Botón principal
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LavenderBlue)
            ) {
                Text(if (isLoginScreen) "Login" else "Crear Cuenta", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            /* --- Logins sociales (Visual) ---
            Spacer(Modifier.height(24.dp))
            Text("O inicia sesión con", color = MediumGrayText, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Estos son solo visuales, no tienen acción
                OutlinedButton(onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(52.dp), contentPadding = PaddingValues(0.dp)) { Icon(painterResource(id = R.drawable.ic_google), null, tint = Color.Unspecified) }
                Spacer(Modifier.width(20.dp))
                OutlinedButton(onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(52.dp), contentPadding = PaddingValues(0.dp)) { Icon(painterResource(id = R.drawable.ic_facebook), null,  tint = Color.Unspecified) }
                Spacer(Modifier.width(20.dp))
                OutlinedButton(onClick = { }, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(52.dp), contentPadding = PaddingValues(0.dp)) { Icon(painterResource(id = R.drawable.ic_apple), null, tint = Color.Unspecified) }
            }*/

            Spacer(Modifier.weight(1f))

            // Link para cambiar entre Login / Registro
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isLoginScreen) "¿No tienes cuenta?" else "¿Ya tienes una cuenta?", color = MediumGrayText)
                TextButton(onClick = { isLoginScreen = !isLoginScreen }) {
                    Text(if (isLoginScreen) "Regístrate" else "Inicia Sesión", color = LavenderBlue, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp)) // Añadido para subir ligeramente el link
        }
    }
}