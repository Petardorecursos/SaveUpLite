package com.example.saveuplite.ui.screens.deudas

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.saveuplite.api.RetrofitClient
import com.example.saveuplite.model.deuda.Deuda
import com.example.saveuplite.model.deuda.EstadoDeuda
import com.example.saveuplite.model.deuda.PagoDeuda
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.ui.screens.dashboard.SoftUiBottomNav
import com.example.saveuplite.ui.utils.NumberVisualTransformation
import com.example.saveuplite.viewmodel.DeudaViewModel
import com.example.saveuplite.viewmodel.DeudaViewModelFactory
import com.example.saveuplite.viewmodel.UsuarioViewModel
import java.text.NumberFormat
import java.util.*
import kotlin.math.ceil

// Enum para las opciones de pago
private enum class OpcionPago {
    CUOTA,
    PERSONALIZADO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeudasScreen(
    navController: NavHostController,
    usuarioViewModel: UsuarioViewModel,
    deudaViewModel: DeudaViewModel = viewModel(factory = DeudaViewModelFactory(RetrofitClient.apiService))
) {
    val usuarioState by usuarioViewModel.uiState.collectAsState()
    val deudaState by deudaViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showPagoDialog by remember { mutableStateOf(false) }
    var selectedDeuda by remember { mutableStateOf<Deuda?>(null) }
    
    LaunchedEffect(usuarioState.currentUser, navController.currentBackStackEntry) {
        usuarioState.currentUser?.rut?.let {
            deudaViewModel.obtenerDeudas(it)
        }
    }
    
    LaunchedEffect(deudaState.operacionExitosa) {
        if (deudaState.operacionExitosa) {
            Toast.makeText(context, "Operación realizada con éxito", Toast.LENGTH_SHORT).show()
            deudaViewModel.resetOperacionExitosa()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Deudas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { SoftUiBottomNav(navController = navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.ADD_DEBT) }) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir Deuda")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (deudaState.isLoading && deudaState.deudas.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (deudaState.errorMessage != null) {
                Text(
                    text = deudaState.errorMessage ?: "Ocurrió un error",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (deudaState.deudas.isEmpty()) {
                Text(
                    text = "¡Felicidades! No tienes deudas pendientes.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(deudaState.deudas) { deuda ->
                        DeudaItem(
                            deuda = deuda,
                            onPagarClick = {
                                selectedDeuda = it
                                showPagoDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showPagoDialog && selectedDeuda != null) {
        PagoDeudaDialog(
            deuda = selectedDeuda!!,
            onDismiss = { showPagoDialog = false },
            onConfirm = { monto, descripcion ->
                usuarioState.currentUser?.rut?.let { rut ->
                    val pago = PagoDeuda(monto = monto, descripcion = descripcion)
                    deudaViewModel.registrarPago(selectedDeuda!!.id, rut, pago)
                }
                showPagoDialog = false
            }
        )
    }
}

@Composable
fun DeudaItem(deuda: Deuda, onPagarClick: (Deuda) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = deuda.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            deuda.descripcion?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            val progress = if (deuda.montoTotal > 0) (deuda.montoPagado / deuda.montoTotal).toFloat() else 0f
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pagado: ${formatToCLP(deuda.montoPagado)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Restante: ${formatToCLP(deuda.montoRestante)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "${deuda.cuotasPagadas} de ${deuda.cantidadCuotas} cuotas pagadas",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (deuda.estado == EstadoDeuda.PENDIENTE) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onPagarClick(deuda) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Registrar Pago")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagoDeudaDialog(
    deuda: Deuda,
    onDismiss: () -> Unit,
    onConfirm: (monto: Double, descripcion: String) -> Unit
) {
    var monto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("Pago cuota ${deuda.cuotasPagadas + 1}") }
    var opcionPago by remember { mutableStateOf(OpcionPago.CUOTA) }

    val cuotasRestantes = deuda.cantidadCuotas - deuda.cuotasPagadas
    val montoCuotaCalculado = remember(deuda) {
        if (cuotasRestantes > 0) {
            ceil(deuda.montoRestante / cuotasRestantes)
        } else {
            deuda.montoRestante
        }
    }

    LaunchedEffect(opcionPago) {
        monto = if (opcionPago == OpcionPago.CUOTA) {
            montoCuotaCalculado.toLong().toString()
        } else {
            ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pagar: ${deuda.nombre}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Contenedor vertical para los Radio Buttons
                Column {
                    OpcionPago.values().forEach { opcion ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (opcion == opcionPago),
                                    onClick = { opcionPago = opcion },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (opcion == opcionPago),
                                onClick = { opcionPago = opcion }
                            )
                            Text(
                                text = when(opcion) {
                                    OpcionPago.CUOTA -> "Monto cuota (${formatToCLP(montoCuotaCalculado)})"
                                    OpcionPago.PERSONALIZADO -> "Monto personalizado"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it.filter { char -> char.isDigit() } },
                    label = { Text("Monto a pagar") },
                    enabled = opcionPago == OpcionPago.PERSONALIZADO,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = NumberVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción del pago") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                monto.toDoubleOrNull()?.let {
                    if (it > 0) {
                        onConfirm(it, descripcion)
                    }
                }
            }) {
                Text("Confirmar Pago")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatToCLP(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    format.maximumFractionDigits = 0
    return format.format(amount).replace(",", ".")
}
