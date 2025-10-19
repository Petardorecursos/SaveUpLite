package com.example.saveuplite.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.saveuplite.model.EventoSaldo
import com.example.saveuplite.model.Saldo
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.viewmodel.SaldoViewModel
import com.example.saveuplite.viewmodel.UsuarioViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    navController: NavHostController, // <--- Parámetro añadido
    usuarioViewModel: UsuarioViewModel = viewModel(),
    saldoViewModel: SaldoViewModel = viewModel()
) {
    val saldoState by saldoViewModel.uiState.collectAsState()
    val usuarioState by usuarioViewModel.uiState.collectAsState()

    // Cargar los datos del saldo cuando la pantalla se muestra por primera vez
    LaunchedEffect(usuarioState.currentUser) {
        usuarioState.currentUser?.rut?.let {
            saldoViewModel.cargarDatosSaldo(it)
        }
    }

    // --- Estado para el diálogo de añadir movimiento ---
    var showDialog by remember { mutableStateOf(false) }
    var tipoMovimiento by remember { mutableStateOf(EventoSaldo.INGRESO) }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Card del Saldo Actual ---
            BalanceCard(saldoActual = saldoState.saldoActual)

            Spacer(modifier = Modifier.height(24.dp))

            // --- Botones de Acción ---
            ActionButtons(
                onIngresoClick = {
                    tipoMovimiento = EventoSaldo.INGRESO
                    showDialog = true
                },
                onGastoClick = {
                    tipoMovimiento = EventoSaldo.GASTO
                    showDialog = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Botón para Funciones Adicionales ---
            OutlinedButton(
                onClick = { navController.navigate(Routes.LEGACY_HOME) }, // Navegará a la pantalla antigua
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Funciones Adicionales")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = "Ir a funciones adicionales")
            }


            Spacer(modifier = Modifier.height(24.dp))

            // --- Historial de Movimientos ---
            TransactionHistory(historial = saldoState.historialMovimientos)
        }
    }

    // --- Diálogo para añadir Ingreso/Gasto ---
    if (showDialog) {
        AddTransactionDialog(
            tipo = tipoMovimiento,
            onDismiss = { showDialog = false },
            onConfirm = { monto ->
                usuarioState.currentUser?.rut?.let { rut ->
                    saldoViewModel.agregarMovimiento(rut, monto, tipoMovimiento)
                }
                showDialog = false
            }
        )
    }
}


@Composable
fun BalanceCard(saldoActual: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Saldo Actual", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$ ${"%.2f".format(saldoActual)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ActionButtons(onIngresoClick: () -> Unit, onGastoClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onIngresoClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ingreso")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ingreso")
        }
        Button(
            onClick = onGastoClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Gasto")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gasto")
        }
    }
}

@Composable
fun TransactionHistory(historial: List<Saldo>) {
    Text(
        "Historial de Movimientos",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(16.dp))
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(historial) { saldo ->
            TransactionItem(item = saldo)
        }
    }
}

@Composable
fun TransactionItem(item: Saldo) {
    val color = if (item.tipoEvento == EventoSaldo.INGRESO) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.tipoEvento.name,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = dateFormat.format(item.fechaRegistro),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$ ${"%.2f".format(item.monto)}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    tipo: EventoSaldo,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var monto by remember { mutableStateOf("") }
    val isIngreso = tipo == EventoSaldo.INGRESO

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isIngreso) "Añadir Ingreso" else "Añadir Gasto") },
        text = {
            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it },
                label = { Text("Monto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(monto.toFloatOrNull() ?: 0f) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
