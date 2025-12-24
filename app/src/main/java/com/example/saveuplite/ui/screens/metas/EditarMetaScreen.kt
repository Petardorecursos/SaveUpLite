package com.example.saveuplite.ui.screens.metas

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.viewmodel.MetaAhorroViewModel
import com.example.saveuplite.viewmodel.UsuarioViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarMetaScreen(
    navController: NavController,
    metaId: Long,
    usuarioViewModel: UsuarioViewModel,
    metaAhorroViewModel: MetaAhorroViewModel
) {
    val context = LocalContext.current
    val usuarioState by usuarioViewModel.uiState.collectAsState()
    val metaAhorroState by metaAhorroViewModel.uiState.collectAsState()

    // Encontrar la meta a editar de la lista en el ViewModel
    val metaAEditar = remember(metaId, metaAhorroState.metas) {
        metaAhorroState.metas.find { it.id == metaId }
    }

    // Estados locales para los campos del formulario
    var nombre by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var fechaLimite by remember { mutableStateOf<Date?>(null) }
    val showDatePicker = remember { mutableStateOf(false) }

    // Efecto para pre-rellenar el formulario cuando se encuentra la meta
    LaunchedEffect(metaAEditar) {
        metaAEditar?.let {
            nombre = it.nombre
            monto = it.montoObjetivo?.toInt()?.toString() ?: ""
            fechaLimite = it.fechaLimite
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Meta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la meta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it },
                label = { Text("Monto Objetivo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = fechaLimite?.let { dateFormatter.format(it) } ?: "",
                onValueChange = {},
                label = { Text("Fecha Límite (Opcional)") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker.value = true }
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    val montoDouble = monto.toDoubleOrNull()
                    usuarioState.currentUser?.rut?.let { rut ->
                        metaAhorroViewModel.editarMeta(rut, metaId, nombre, montoDouble, fechaLimite) {
                            // onSuccess: Navega de vuelta a la lista principal
                            navController.navigate(Routes.GOALS) {
                                popUpTo(Routes.GOALS) { inclusive = true }
                            }
                            Toast.makeText(context, "¡Meta actualizada!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = nombre.isNotBlank() && (monto.toDoubleOrNull() ?: 0.0) > 0 && !metaAhorroState.isLoading
            ) {
                if (metaAhorroState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Guardar Cambios")
                }
            }
        }
    }

    if (showDatePicker.value) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        fechaLimite = Date(it + 86400000) // Sumar un día para corregir la zona horaria
                    }
                    showDatePicker.value = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker.value = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
