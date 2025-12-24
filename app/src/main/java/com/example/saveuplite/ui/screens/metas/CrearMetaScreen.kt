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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearMetaScreen(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel,
    metaAhorroViewModel: MetaAhorroViewModel
) {
    var nombre by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var fechaLimite by remember { mutableStateOf<Date?>(null) }
    val showDatePicker = remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Estados para el feedback visual de los botones
    var selectedMontoSuggestion by remember { mutableStateOf<String?>(null) }
    var selectedDateSuggestion by remember { mutableStateOf<Int?>(null) }

    val usuarioState by usuarioViewModel.uiState.collectAsState()
    val metaAhorroState by metaAhorroViewModel.uiState.collectAsState()

    // Formateador para mostrar la fecha de forma legible
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Nueva Meta") },
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
                onValueChange = { 
                    monto = it 
                    selectedMontoSuggestion = null // Resetea la selección si se escribe manually
                },
                label = { Text("Monto Objetivo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionButton(text = "100k", isSelected = selectedMontoSuggestion == "100000") { monto = "100000"; selectedMontoSuggestion = "100000" }
                SuggestionButton(text = "500k", isSelected = selectedMontoSuggestion == "500000") { monto = "500000"; selectedMontoSuggestion = "500000" }
                SuggestionButton(text = "1M", isSelected = selectedMontoSuggestion == "1000000") { monto = "1000000"; selectedMontoSuggestion = "1000000" }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = fechaLimite?.let { dateFormatter.format(it) } ?: "",
                onValueChange = {},
                label = { Text("Fecha Límite (Opcional)") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth().clickable { 
                    showDatePicker.value = true 
                    selectedDateSuggestion = null // Resetea selección de fecha
                }
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionButton(text = "1 Mes", isSelected = selectedDateSuggestion == 1) { fechaLimite = getFutureDate(1); selectedDateSuggestion = 1 }
                SuggestionButton(text = "3 Meses", isSelected = selectedDateSuggestion == 3) { fechaLimite = getFutureDate(3); selectedDateSuggestion = 3 }
                SuggestionButton(text = "1 Año", isSelected = selectedDateSuggestion == 12) { fechaLimite = getFutureDate(12); selectedDateSuggestion = 12 }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    val montoDouble = monto.toDoubleOrNull()
                    usuarioState.currentUser?.rut?.let {
                        metaAhorroViewModel.crearMeta(it, nombre, montoDouble, fechaLimite) {
                            // onSuccess: Navega y muestra el Toast DESPUÉS de que todo ha terminado
                            navController.navigate(Routes.GOALS) {
                                popUpTo(Routes.GOALS) { inclusive = true }
                            }
                            Toast.makeText(context, "¡Meta creada con éxito!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = nombre.isNotBlank() && (monto.toDoubleOrNull() ?: 0.0) > 0 && !metaAhorroState.isLoading
            ) {
                if (metaAhorroState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Crear Meta")
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

@Composable
private fun SuggestionButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                 else ButtonDefaults.outlinedButtonColors()
    ) {
        Text(text)
    }
}

private fun getFutureDate(months: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, months)
    return calendar.time
}
