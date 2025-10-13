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
import androidx.compose.ui.graphics.Color
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

    // 🎨 Fondo degradado verde → negro
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF00C853), // verde brillante
            Color(0xFF004D40), // verde oscuro
            Color.Black         // negro base
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Formulario",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF004D40)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Campos del formulario
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        showError = false
                    },
                    label = { Text("Nombre", color = Color(0xFFB2DFDB)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rut,
                    onValueChange = {
                        rut = it
                        showError = false
                    },
                    label = { Text("RUT (ej: 12.345.678-9)", color = Color(0xFFB2DFDB)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = ingreso,
                    onValueChange = {
                        ingreso = it.filter { c -> c.isDigit() }
                        showError = false
                    },
                    label = { Text("Ingreso (CLP)", color = Color(0xFFB2DFDB)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
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
                    label = { Text("Descripción (máx. 50 caracteres)", color = Color(0xFFB2DFDB)) },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "${descripcion.length}/50",
                    color = Color(0xFF80CBC4),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.End)
                )

                // ⚠️ Mensaje de error
                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg,
                        color = Color(0xFFFF8A80),
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
                        containerColor = Color(0xFF00E676),
                        contentColor = Color.Black
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

                // 🔙 Botón volver
                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver a Home", color = Color(0xFF80CBC4))
                }
            }
        }
    }
}
