package com.example.saveuplite.ui.screens.deudas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.saveuplite.api.RetrofitClient
import com.example.saveuplite.model.deuda.Deuda
import com.example.saveuplite.model.deuda.EstadoDeuda
import com.example.saveuplite.model.deuda.PagoDeuda
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.ui.components.SoftUiBottomNav
import com.example.saveuplite.ui.theme.*
import com.example.saveuplite.ui.utils.NumberVisualTransformation
import com.example.saveuplite.viewmodel.DeudaViewModel
import com.example.saveuplite.viewmodel.DeudaViewModelFactory
import com.example.saveuplite.viewmodel.UsuarioViewModel
import java.text.NumberFormat
import java.util.*
import kotlin.math.ceil

// Enum para las opciones de pago
// Enum para las opciones de pago
private enum class OpcionPago {
    CUOTA,
    VARIAS_CUOTAS,
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
        containerColor = SoftWhite,
        topBar = {
            TopAppBar(
                title = { Text("Mis Deudas", fontWeight = FontWeight.Bold, color = DarkGrayText) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftWhite)
            )
        },
        bottomBar = { SoftUiBottomNav(navController = navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.ADD_DEBT) },
                containerColor = LavenderBlue,
                contentColor = Color.White
            ) {
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
            val totalDeuda = deudaState.deudas.sumOf { it.montoRestante }

            if (deudaState.isLoading && deudaState.deudas.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = LavenderBlue)
            } else if (deudaState.errorMessage != null) {
                Text(
                    text = deudaState.errorMessage ?: "Ocurrió un error",
                    modifier = Modifier.align(Alignment.Center),
                    color = MediumGrayText
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        TotalDeudaCard(totalDeuda)
                    }

                    if (deudaState.deudas.isEmpty()) {
                        item {
                             Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "¡Felicidades! Estás libre de deudas.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MediumGrayText
                                )
                             }
                        }
                    } else {
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
fun TotalDeudaCard(totalDeuda: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Total Deuda Pendiente",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = formatToCLP(totalDeuda),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DeudaItem(deuda: Deuda, onPagarClick: (Deuda) -> Unit) {
    // --- CÁLCULO CORRECTO DE CUOTAS PAGADAS ---
    val valorCuota = if (deuda.cantidadCuotas > 0) deuda.montoTotal / deuda.cantidadCuotas else 0.0
    val cuotasPagadasReales = if (valorCuota > 0) {
        (deuda.montoPagado / valorCuota).toInt() // División entera (floor)
    } else {
        if (deuda.montoPagado >= deuda.montoTotal) deuda.cantidadCuotas else 0
    }.coerceAtMost(deuda.cantidadCuotas) // Asegura no mostrar más cuotas que el total

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), // Soft UI Shape
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) { // Increased padding
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 Text(
                    text = deuda.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkGrayText
                )
                // Estado Badge (Opcional)
                if (deuda.estado == EstadoDeuda.PAGADA) {
                     Text("PAGADA", style = MaterialTheme.typography.labelSmall, color = MediumBlue, fontWeight = FontWeight.Bold)
                }
            }
           
            Spacer(modifier = Modifier.height(4.dp))
            deuda.descripcion?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MediumGrayText)
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            val progress = if (deuda.montoTotal > 0) (deuda.montoPagado / deuda.montoTotal).toFloat() else 0f
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = SaturatedSalmon, // Saturated Salmon for progress
                trackColor = LightGray
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Restante",
                        style = MaterialTheme.typography.labelSmall,
                        color = MediumGrayText
                    )
                    Text(
                        text = formatToCLP(deuda.montoRestante),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkGrayText
                    )
                }
                // --- USO DEL VALOR CALCULADO ---
                Text(
                    text = "${cuotasPagadasReales}/${deuda.cantidadCuotas} cuotas",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MediumGrayText
                )
            }
            
            if (deuda.estado == EstadoDeuda.PENDIENTE) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onPagarClick(deuda) },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderBlue, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pagar")
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
    // --- CÁLCULO CORRECTO DE CUOTAS PARA LA DESCRIPCIÓN Y EL PAGO SUGERIDO ---
    val valorCuota = if (deuda.cantidadCuotas > 0) deuda.montoTotal / deuda.cantidadCuotas else 0.0
    val cuotasPagadasReales = if (valorCuota > 0) {
        (deuda.montoPagado / valorCuota).toInt()
    } else {
        if (deuda.montoPagado >= deuda.montoTotal) deuda.cantidadCuotas else 0
    }.coerceAtMost(deuda.cantidadCuotas)

    var monto by remember { mutableStateOf("") }
    // Estado inicial de descripción vacío, se llenará dinámicamente
    var descripcion by remember { mutableStateOf("") }
    var opcionPago by remember { mutableStateOf(OpcionPago.CUOTA) }
    
    // Estado para "Varias cuotas"
    val cuotasRestantes = (deuda.cantidadCuotas - cuotasPagadasReales).coerceAtLeast(1)
    var cantidadCuotasSeleccionadas by remember { mutableIntStateOf(1) }

    val montoCuotaCalculado = remember(deuda) {
        if (cuotasRestantes > 0) {
            ceil(deuda.montoRestante / cuotasRestantes)
        } else {
            deuda.montoRestante
        }
    }

    // Efecto para actualizar monto y descripción según la opción seleccionada
    LaunchedEffect(opcionPago, cantidadCuotasSeleccionadas) {
        when (opcionPago) {
            OpcionPago.CUOTA -> {
                monto = montoCuotaCalculado.toLong().toString()
                descripcion = "Pago cuota ${cuotasPagadasReales + 1}"
            }
            OpcionPago.VARIAS_CUOTAS -> {
                val totalLote = montoCuotaCalculado * cantidadCuotasSeleccionadas
                monto = totalLote.toLong().toString()
                val inicio = cuotasPagadasReales + 1
                val fin = cuotasPagadasReales + cantidadCuotasSeleccionadas
                descripcion = "Pago cuotas $inicio a $fin"
            }
            OpcionPago.PERSONALIZADO -> {
                if (monto.isEmpty()) monto = "" // No borrar si ya usuario escribió algo? Mejor resetear para evitar inconsistencias
                // Mantener descripción actual o dejar vacía? Dejar vacía para que usuario escriba
                if (descripcion.startsWith("Pago cuota")) descripcion = ""
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pagar: ${deuda.nombre}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                    OpcionPago.VARIAS_CUOTAS -> "Varias cuotas" // El detalle se muestra abajo si está seleccionado
                                    OpcionPago.PERSONALIZADO -> "Monto personalizado"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        // Selector de cantidad (solo visible si seleccionó Varias Cuotas y es esa opción)
                        if (opcion == OpcionPago.VARIAS_CUOTAS && opcionPago == OpcionPago.VARIAS_CUOTAS) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 48.dp, bottom = 8.dp), // Indentation
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FilledIconButton(
                                    onClick = { if (cantidadCuotasSeleccionadas > 1) cantidadCuotasSeleccionadas-- },
                                    enabled = cantidadCuotasSeleccionadas > 1,
                                    modifier = Modifier.size(32.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = LavenderBlue)
                                ) {
                                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Menos", tint = Color.White)
                                }
                                
                                Text(
                                    text = "$cantidadCuotasSeleccionadas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                FilledIconButton(
                                    onClick = { if (cantidadCuotasSeleccionadas < cuotasRestantes) cantidadCuotasSeleccionadas++ },
                                    enabled = cantidadCuotasSeleccionadas < cuotasRestantes,
                                    modifier = Modifier.size(32.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = LavenderBlue)
                                ) {
                                     Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Más", tint = Color.White)
                                }
                                
                                Text(
                                    text = "(${cantidadCuotasSeleccionadas} de $cuotasRestantes)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MediumGrayText
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = monto,
                    onValueChange = { 
                        if (opcionPago == OpcionPago.PERSONALIZADO) {
                            monto = it.filter { char -> char.isDigit() } 
                        }
                    },
                    label = { Text("Monto a pagar") },
                    enabled = opcionPago == OpcionPago.PERSONALIZADO,
                    readOnly = opcionPago != OpcionPago.PERSONALIZADO,
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
