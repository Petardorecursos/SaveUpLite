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
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Análisis de Gastos",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (expenseData.isNotEmpty()) {
            PieChartComponent(data = expenseData)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Detalle por Categoría",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn {
                items(expenseData.keys.toList()) { categoryName ->
                     val amount = expenseData[categoryName] ?: 0.0
                     ListItem(
                         headlineContent = { Text(categoryName) },
                         trailingContent = { Text("$${amount.toInt()}") }
                     )
                     Divider()
                }
            }
        } else {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 Text("No hay datos de gastos para mostrar.")
             }
        }
    }
}
