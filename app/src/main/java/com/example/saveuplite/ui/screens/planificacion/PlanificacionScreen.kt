package com.example.saveuplite.ui.screens.planificacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
        topBar = {
            TopAppBar(
                title = { Text("Planificación Financiera") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                "Regla 50/30/20 (Set & Forget)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGrayText
            )
            Text(
                "Define cómo quieres distribuir tus ingresos automáticamente.",
                style = MaterialTheme.typography.bodyMedium,
                color = MediumGrayText
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Sliders para distribución
            DistributionSection(
                needs = uiState.needs,
                wants = uiState.wants,
                savings = uiState.savings,
                onUpdate = { n, w, s -> viewModel.updateDist(n, w, s) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Divider()

            Spacer(modifier = Modifier.height(24.dp))
            
            // Goal Assignment UI
            Text(
                "Distribución de Ahorro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkGrayText
            )
            Text(
                "Asigna el ${(uiState.savings * 100).toInt()}% de ahorro a tus metas actuales.",
                style = MaterialTheme.typography.bodySmall,
                color = MediumGrayText
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (uiState.metas.isEmpty()) {
                 Text("No tienes metas activas. El ahorro se acumulará sin asignar.", style = MaterialTheme.typography.bodyMedium, color = MediumGrayText)
            } else {
                 uiState.metas.forEach { meta ->
                     val percentage = uiState.goalAssignments[meta.id] ?: 0f
                     GoalSlider(meta.nombre, percentage) { newVal ->
                         viewModel.updateGoalAssignment(meta.id, newVal)
                     }
                     Spacer(modifier = Modifier.height(8.dp))
                 }
                 
                 val totalGoals = uiState.goalAssignments.values.sum()
                 val isValid = kotlin.math.abs(totalGoals - 1.0f) <= 0.05f
                 Text(
                    "Total Asignado: ${(totalGoals * 100).toInt()}%",
                    color = if (isValid) MintGreen else DangerRed,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Simulador
            Text(
                "Simulador en Vivo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkGrayText
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = uiState.simulationAmount,
                onValueChange = { viewModel.setSimulationAmount(it) },
                label = { Text("Monto de Ingreso (Ej: Sueldo)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.simulationResults.isNotEmpty()) {
                SimulationResults(uiState.simulationResults)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    usuarioViewModel.uiState.value.currentUser?.rut?.let { viewModel.saveConfiguration(it) }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = (uiState.needs + uiState.wants + uiState.savings) in 0.99f..1.01f
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Configuración")
                }
            }
            
            if (uiState.errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text("Error: ${uiState.errorMessage}", color = DangerRed)
            }
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
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${(value * 100).toInt()}%", fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(thumbColor = PaleTeal, activeTrackColor = PaleTeal)
        )
    }
}

@Composable
fun DistributionSection(needs: Float, wants: Float, savings: Float, onUpdate: (Float, Float, Float) -> Unit) {
    Column {
        DistributionSlider("Necesidades (Fijos)", needs, PaleAqua) { newVal -> 
            val diff = newVal - needs
            // Redistribute diff from others (naive implementation)
            // Just clamp for now or allow free movement and validate total?
            // User requested: "validación en tiempo real (debe sumar 100%)"
            // For simplicity in MVP, let's just update and show total error if not 100
            onUpdate(newVal, wants, savings)
        }
        DistributionSlider("Deseos (Variables)", wants, PalePink) { newVal -> 
            onUpdate(needs, newVal, savings)
        }
        DistributionSlider("Ahorro (Metas)", savings, PaleTeal) { newVal -> 
             onUpdate(needs, wants, newVal)
        }
        
        val total = (needs + wants + savings) * 100
        Text(
            "Total: ${total.roundToInt()}%",
            color = if (total.roundToInt() == 100) MintGreen else DangerRed,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun DistributionSlider(label: String, value: Float, color: Color, onValueChange: (Float) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${(value * 100).roundToInt()}%", fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
        )
    }
}

@Composable
fun SimulationResults(results: Map<String, Double>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        results.forEach { (category, amount) ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(category, style = MaterialTheme.typography.labelSmall)
                    Text(
                        "$${amount.roundToInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
