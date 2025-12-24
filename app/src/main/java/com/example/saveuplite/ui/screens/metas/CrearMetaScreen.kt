package com.example.saveuplite.ui.screens.metas

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.ui.theme.*
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
    val focusManager = LocalFocusManager.current

    // Estados para el feedback visual de los botones
    var selectedMontoSuggestion by remember { mutableStateOf<String?>(null) }
    var selectedDateSuggestion by remember { mutableStateOf<Int?>(null) }

    val usuarioState by usuarioViewModel.uiState.collectAsState()
    val metaAhorroState by metaAhorroViewModel.uiState.collectAsState()

    // Formateador para mostrar la fecha de forma legible
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        containerColor = SoftWhite,
        topBar = {
            TopAppBar(
                title = { Text("Crear Nueva Meta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftWhite)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PaleAqua),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("Nombre de la meta", style = MaterialTheme.typography.labelLarge, color = DarkGrayText)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        placeholder = { Text("Ej. Vacaciones 2025") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderBlue,
                            unfocusedBorderColor = LightGrayText,
                            focusedContainerColor = SoftWhite,
                            unfocusedContainerColor = SoftWhite
                        )
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text("Monto Objetivo", style = MaterialTheme.typography.labelLarge, color = DarkGrayText)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = monto,
                        onValueChange = { 
                            monto = it 
                            selectedMontoSuggestion = null // Resetea la selección si se escribe manually
                        },
                        placeholder = { Text("Ej. 500000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderBlue,
                            unfocusedBorderColor = LightGrayText,
                            focusedContainerColor = SoftWhite,
                            unfocusedContainerColor = SoftWhite
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionButton(text = "100k", isSelected = selectedMontoSuggestion == "100000") { monto = "100000"; selectedMontoSuggestion = "100000" }
                        SuggestionButton(text = "500k", isSelected = selectedMontoSuggestion == "500000") { monto = "500000"; selectedMontoSuggestion = "500000" }
                        SuggestionButton(text = "1M", isSelected = selectedMontoSuggestion == "1000000") { monto = "1000000"; selectedMontoSuggestion = "1000000" }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text("Fecha Límite (Opcional)", style = MaterialTheme.typography.labelLarge, color = DarkGrayText)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fechaLimite?.let { dateFormatter.format(it) } ?: "",
                        onValueChange = {},
                        placeholder = { Text("Seleccionar fecha") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { 
                            showDatePicker.value = true 
                            selectedDateSuggestion = null // Resetea selección de fecha
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderBlue,
                            unfocusedBorderColor = LightGrayText,
                            focusedContainerColor = SoftWhite,
                            unfocusedContainerColor = SoftWhite
                        ),
                        trailingIcon = {
                           IconButton(onClick = { showDatePicker.value = true }) {
                               Icon(Icons.Filled.DateRange, contentDescription = "Seleccionar fecha", tint = LavenderBlue)
                           }
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionButton(text = "1 Mes", isSelected = selectedDateSuggestion == 1) { fechaLimite = getFutureDate(1); selectedDateSuggestion = 1 }
                        SuggestionButton(text = "3 Meses", isSelected = selectedDateSuggestion == 3) { fechaLimite = getFutureDate(3); selectedDateSuggestion = 3 }
                        SuggestionButton(text = "1 Año", isSelected = selectedDateSuggestion == 12) { fechaLimite = getFutureDate(12); selectedDateSuggestion = 12 }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
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
                enabled = nombre.isNotBlank() && (monto.toDoubleOrNull() ?: 0.0) > 0 && !metaAhorroState.isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LavenderBlue,
                    contentColor = Color.White,
                    disabledContainerColor = LightGray,
                    disabledContentColor = MediumGrayText
                )
            ) {
                if (metaAhorroState.isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Crear Meta", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (showDatePicker.value) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            fechaLimite = Date(it + 86400000) // Sumar un día para corregir la zona horaria
                        }
                        showDatePicker.value = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderBlue)
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker.value = false }) { Text("Cancelar") } },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SuggestionButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = LavenderBlue, contentColor = Color.White)
                 else ButtonDefaults.outlinedButtonColors(contentColor = MediumGrayText, containerColor = Color.Transparent),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, LightGrayText),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun getFutureDate(months: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, months)
    return calendar.time
}
