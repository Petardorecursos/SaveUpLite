package com.example.saveuplite.ui.screens.planificacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saveuplite.ui.theme.*
import com.example.saveuplite.viewmodel.PlanificacionViewModel
import com.example.saveuplite.viewmodel.UsuarioViewModel
import kotlin.math.roundToInt
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanificacionScreen(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel,
    viewModel: PlanificacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        usuarioViewModel.uiState.value.currentUser?.rut?.let { viewModel.cargarDatos(it) }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            android.widget.Toast.makeText(context, "Configuración y Planificación guardada correctamente.", android.widget.Toast.LENGTH_LONG).show()
            viewModel.resetSaveSuccess()
            navController.popBackStack() 
        }
    }

    Scaffold(
        containerColor = SoftWhite,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Planificación Financiera",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = DarkGrayText
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = DarkGrayText)
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
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Description
            Text(
                "Define tu estrategia con la regla 50/30/20 y asigna tus ahorros automáticamente.",
                style = MaterialTheme.typography.bodyMedium,
                color = MediumGrayText
            )

            // Section 1: Global Distribution (50/30/20)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Distribución Base",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkGrayText
                    )
                    
                    DistributionSection(
                        needs = uiState.needs,
                        wants = uiState.wants,
                        savings = uiState.savings,
                        onUpdate = { n, w, s -> viewModel.updateDist(n, w, s) }
                    )
                }
            }

            // Section 2: Goal Assignment
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isSavingsEnabled = uiState.savings > 0f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Mapeo de Metas",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSavingsEnabled) DarkGrayText else MediumGrayText
                            )
                            if (isSavingsEnabled) {
                                Text(
                                    "Reparte tu ${(uiState.savings * 100).toInt()}% de ahorro.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MediumGrayText
                                )
                            }
                        }
                        
                        if (isSavingsEnabled) {
                            val totalGoals = uiState.goalAssignments.values.sum()
                            val isValid = abs(totalGoals - 1.0f) <= 0.05f
                            
                            Text(
                                "${(totalGoals * 100).toInt()}%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isValid) MetricGreen else DangerRed
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Close, // Or Lock/Info, but Close communicates 'Off' well here contextually or just generic info
                                contentDescription = "No disponible",
                                tint = LightGray
                            )
                        }
                    }

                    if (isSavingsEnabled) {
                        if (uiState.metas.isEmpty()) {
                            Text(
                                "No tienes metas activas. Crea una para empezar a asignar ahorros.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MediumGrayText,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            uiState.metas.forEach { meta ->
                                val percentage = uiState.goalAssignments[meta.id] ?: 0f
                                GoalSlider(meta.nombre, percentage) { newVal ->
                                    viewModel.updateGoalAssignment(meta.id, newVal)
                                }
                            }
                        }
                    } else {
                        // Collapsed state message
                         Text(
                            "Asigna un porcentaje a 'Ahorro' para habilitar esta sección.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MediumGrayText,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Section 3: Simulator
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Simulador en Vivo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkGrayText
                    )
                    
                    OutlinedTextField(
                        value = uiState.simulationAmount,
                        onValueChange = { viewModel.setSimulationAmount(it) },
                        label = { Text("Ingresa un monto (Ej: Sueldo)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderBlue,
                            unfocusedBorderColor = LightGray
                        )
                    )

                    if (uiState.simulationResults.isNotEmpty()) {
                        SimulationResults(uiState.simulationResults)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = { 
                    usuarioViewModel.uiState.value.currentUser?.rut?.let { viewModel.saveConfiguration(it) }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LavenderBlue),
                enabled = (uiState.needs + uiState.wants + uiState.savings) in 0.99f..1.01f
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Configuración", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
            
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = DangerRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GoalSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = DarkGrayText)
            Text("${(value * 100).toInt()}%", fontWeight = FontWeight.SemiBold, color = DarkGrayText)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = LavenderBlue, 
                activeTrackColor = LavenderBlue,
                inactiveTrackColor = LightGray
            ),
            modifier = Modifier.height(20.dp) // Compact slider
        )
    }
}

@Composable
fun DistributionSection(needs: Float, wants: Float, savings: Float, onUpdate: (Float, Float, Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DistributionSlider("Necesidades (Fijos)", needs, PaleAqua, DarkTeal) { newVal -> 
            onUpdate(newVal, wants, savings)
        }
        DistributionSlider("Deseos (Variables)", wants, PalePink, DangerRed) { newVal -> 
            onUpdate(needs, newVal, savings)
        }
        DistributionSlider("Ahorro (Metas)", savings, PaleTeal, DarkTeal) { newVal -> 
             onUpdate(needs, wants, newVal)
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val total = (needs + wants + savings) * 100
            val isValid = total.roundToInt() == 100
            Text(
                "Total: ${total.roundToInt()}%",
                color = if (isValid) MetricGreen else DangerRed,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun DistributionSlider(label: String, value: Float, trackColor: Color, thumbColor: Color, onValueChange: (Float) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = DarkGrayText)
            Text("${(value * 100).roundToInt()}%", fontWeight = FontWeight.SemiBold, color = DarkGrayText)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = thumbColor,
                activeTrackColor = thumbColor,
                inactiveTrackColor = trackColor
            )
        )
    }
}

@Composable
fun SimulationResults(results: Map<String, Double>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Map keys to colors for visual feedback
        val colors = mapOf(
            "Necesidades" to PaleAqua,
            "Deseos" to PalePink,
            "Ahorro" to PaleTeal
        )
        
        results.forEach { (category, amount) ->
            val bgColor = colors[category] ?: LightGray
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = bgColor),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        category, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MediumGrayText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "$${amount.roundToInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkGrayText
                    )
                }
            }
        }
    }
}
