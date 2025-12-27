package com.example.saveuplite.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.example.saveuplite.model.dto.MovimientoResponseDTO
import com.example.saveuplite.model.enums.TipoMovimiento
import com.example.saveuplite.ui.components.PieChartComponent
import com.example.saveuplite.ui.components.SoftUiBottomNav
import com.example.saveuplite.viewmodel.AnalysisViewModel
import java.time.LocalDate

@Composable
fun AnalysisScreen(
    navController: NavController,
    viewModel: AnalysisViewModel,
    usuarioRut: String
) {
    val movimientos by viewModel.movimientos.collectAsState()
    
    LaunchedEffect(usuarioRut) {
        viewModel.obtenerMovimientos(usuarioRut)
    }

    // Procesar datos para el gráfico
    val expenseData = remember(movimientos) {
        movimientos
            .filter { it.tipoMovimiento == TipoMovimiento.GASTO_GENERAL || it.tipoMovimiento == TipoMovimiento.PAGO_DEUDA }
            .groupBy { it.categoria?.nombre ?: "Sin Categoría" }
            .mapValues { entry -> 
                entry.value.sumOf { Math.abs(it.monto) }
            }
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }

    Scaffold(
        containerColor = com.example.saveuplite.ui.theme.SoftWhite,
        bottomBar = { SoftUiBottomNav(navController = navController) }
    ) { paddingValues ->
        if (expenseData.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text(
                        text = "Análisis de Gastos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = com.example.saveuplite.ui.theme.DarkGrayText
                    )
                }

                // Tarjeta del Gráfico
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Distribución de Gastos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                color = com.example.saveuplite.ui.theme.DarkGrayText,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            PieChartComponent(data = expenseData)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Tarjeta de Detalle por Categoría
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Detalle por Categoría",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = com.example.saveuplite.ui.theme.DarkGrayText
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            expenseData.forEach { (categoryName, amount) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Bullet point colored? For now just text
                                        Text(
                                            text = categoryName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = com.example.saveuplite.ui.theme.DarkGrayText,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "CL")).format(amount),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = com.example.saveuplite.ui.theme.MetricRed
                                    )
                                }
                                Divider(color = com.example.saveuplite.ui.theme.LightGray)
                            }
                        }
                    }
                }
            }
        } else {
             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), 
                contentAlignment = Alignment.Center
            ) {
                 Text(
                    "No hay datos de gastos para mostrar.",
                    color = com.example.saveuplite.ui.theme.MediumGrayText
                 )
             }
        }
    }
}
