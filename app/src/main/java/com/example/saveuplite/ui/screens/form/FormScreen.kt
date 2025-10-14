package com.example.saveuplite.ui.screens.form

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.saveuplite.data.DatabaseHelper
import com.example.saveuplite.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun FormScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }

    var nombre by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var ingreso by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    // 🎨 Fondo degradado que utiliza los colores del tema
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Formulario",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Campos del formulario
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            showError = false
                        },
                        label = { Text("Nombre") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = rut,
                        onValueChange = {
                            rut = it
                            showError = false
                        },
                        label = { Text("RUT (ej: 12.345.678-9)") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = ingreso,
                        onValueChange = {
                            ingreso = it.filter { c -> c.isDigit() }
                            showError = false
                        },
                        label = { Text("Ingreso (CLP)") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = {
                            if (it.length <= 50) {
                                descripcion = it
                                showError = false
                            }
                        },
                        label = { Text("Descripción (máx. 50 caracteres)") },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "${descripcion.length}/50",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.End)
                    )

                    // ⚠️ Mensaje de error
                    if (showError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ✅ Botón principal (guardar en SQLite)
                    Button(
                        onClick = {
                            when {
                                nombre.isBlank() -> {
                                    showError = true
                                    errorMsg = "⚠️ El nombre no puede estar vacío"
                                }
                                rut.isBlank() -> {
                                    showError = true
                                    errorMsg = "⚠️ El RUT no puede estar vacío"
                                }
                                ingreso.isBlank() -> {
                                    showError = true
                                    errorMsg = "⚠️ Debes ingresar un valor numérico"
                                }
                                descripcion.isBlank() -> {
                                    showError = true
                                    errorMsg = "⚠️ La descripción no puede estar vacía"
                                }
                                else -> {
                                    val success = dbHelper.insertarUsuario(
                                        nombre,
                                        rut,
                                        ingreso.toInt(),
                                        descripcion
                                    )

                                    if (success) {
                                        Toast.makeText(context, "Datos guardados correctamente ✅", Toast.LENGTH_SHORT).show()

                                        // Limpiar campos
                                        nombre = ""
                                        rut = ""
                                        ingreso = ""
                                        descripcion = ""
                                    } else {
                                        Toast.makeText(context, "Error al guardar ❌", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            "Enviar",
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 🧾 Nuevo botón para listar registros
                    Button(
                        onClick = { navController.navigate(Routes.LIST) },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            "Listar",
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 🔙 Botón volver
                    TextButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver a Home", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
