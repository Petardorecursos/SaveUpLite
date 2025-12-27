package com.example.saveuplite.ui.screens.deudas

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.saveuplite.api.RetrofitClient
import com.example.saveuplite.model.deuda.DeudaCreacion
import com.example.saveuplite.ui.theme.*
import com.example.saveuplite.ui.utils.NumberVisualTransformation
import com.example.saveuplite.viewmodel.DeudaViewModel
import com.example.saveuplite.viewmodel.DeudaViewModelFactory
import com.example.saveuplite.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AddDeudaScreen(
    navController: NavHostController,
    usuarioViewModel: UsuarioViewModel,
    deudaViewModel: DeudaViewModel = viewModel(factory = DeudaViewModelFactory(RetrofitClient.apiService))
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var montoTotal by remember { mutableStateOf("") }
    var cantidadCuotas by remember { mutableStateOf("") }

    val context = LocalContext.current
    val deudaState by deudaViewModel.uiState.collectAsState()
    val usuarioState by usuarioViewModel.uiState.collectAsState()
    
    val focusManager = LocalFocusManager.current
    val (nombreFocus, descripcionFocus, montoFocus, cuotasFocus) = remember { FocusRequester.createRefs() }

    // Función para guardar la deuda
    val guardarDeuda = {
        val monto = montoTotal.toDoubleOrNull()
        val cuotas = cantidadCuotas.toIntOrNull()
        val rut = usuarioState.currentUser?.rut

        if (nombre.isNotBlank() && monto != null && cuotas != null && rut != null) {
            val nuevaDeuda = DeudaCreacion(
                nombre = nombre,
                descripcion = descripcion.takeIf { it.isNotBlank() },
                montoTotal = monto,
                cantidadCuotas = cuotas,
                usuarioRut = rut
            )
            deudaViewModel.crearDeuda(nuevaDeuda)
        } else {
            Toast.makeText(context, "Por favor, completa todos los campos correctamente.", Toast.LENGTH_SHORT).show()
        }
    }

    // Observar el estado de la operación para navegar hacia atrás
    LaunchedEffect(deudaState.operacionExitosa) {
        if (deudaState.operacionExitosa) {
            Toast.makeText(context, "Deuda creada exitosamente", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            deudaViewModel.resetOperacionExitosa()
        }
    }
    
    LaunchedEffect(deudaState.errorMessage) {
        deudaState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            deudaViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        containerColor = SoftWhite,
        topBar = {
            TopAppBar(
                title = { Text("Añadir Nueva Deuda", fontWeight = FontWeight.Bold, color = DarkGrayText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = DarkGrayText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftWhite)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre de la deuda") },
                        modifier = Modifier.fillMaxWidth().focusRequester(nombreFocus),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { descripcionFocus.requestFocus() }),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SoftWhite,
                            unfocusedContainerColor = SoftWhite,
                            focusedBorderColor = LavenderBlue,
                            unfocusedBorderColor = LightGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción (Opcional)") },
                        modifier = Modifier.fillMaxWidth().focusRequester(descripcionFocus),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { montoFocus.requestFocus() }),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SoftWhite,
                            unfocusedContainerColor = SoftWhite,
                            focusedBorderColor = LavenderBlue,
                            unfocusedBorderColor = LightGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = montoTotal,
                        onValueChange = {
                            montoTotal = it.filter { char -> char.isDigit() }
                        },
                        label = { Text("Monto Total") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { cuotasFocus.requestFocus() }),
                        visualTransformation = NumberVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().focusRequester(montoFocus),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SoftWhite,
                            unfocusedContainerColor = SoftWhite,
                            focusedBorderColor = LavenderBlue,
                            unfocusedBorderColor = LightGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = cantidadCuotas,
                        onValueChange = { cantidadCuotas = it },
                        label = { Text("Cantidad de Cuotas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            guardarDeuda()
                        }),
                        modifier = Modifier.fillMaxWidth().focusRequester(cuotasFocus),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SoftWhite,
                            unfocusedContainerColor = SoftWhite,
                            focusedBorderColor = LavenderBlue,
                            unfocusedBorderColor = LightGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = guardarDeuda,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !deudaState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = LavenderBlue, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (deudaState.isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Guardar Deuda", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
