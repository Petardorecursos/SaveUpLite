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
    val focusManager = LocalFocusManager.current
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
        containerColor = SoftWhite,
        topBar = {
            TopAppBar(
                title = { Text("Editar Meta", fontWeight = FontWeight.Bold) },
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
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text("Monto Objetivo", style = MaterialTheme.typography.labelLarge, color = DarkGrayText)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = monto,
                        onValueChange = { monto = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderBlue,
                            unfocusedBorderColor = LightGrayText,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text("Fecha Límite (Opcional)", style = MaterialTheme.typography.labelLarge, color = DarkGrayText)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fechaLimite?.let { dateFormatter.format(it) } ?: "",
                        onValueChange = {},
                        placeholder = { Text("Seleccionar fecha") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker.value = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderBlue,
                            unfocusedBorderColor = LightGrayText,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        trailingIcon = {
                           IconButton(onClick = { showDatePicker.value = true }) {
                               Icon(Icons.Filled.DateRange, contentDescription = "Seleccionar fecha", tint = LavenderBlue)
                           }
                        }
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
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
                    Text("Guardar Cambios", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
