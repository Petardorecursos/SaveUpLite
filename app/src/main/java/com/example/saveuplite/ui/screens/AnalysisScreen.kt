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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Favorite

@Composable
fun AnalysisScreen(
    navController: NavController,
    viewModel: AnalysisViewModel,
    usuarioRut: String
) {
    val movimientos by viewModel.movimientos.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val ejecucion by viewModel.ejecucionPresupuesto.collectAsState()
    
    LaunchedEffect(usuarioRut) {
        viewModel.obtenerMovimientos(usuarioRut)
    }

    // 1. Filtrar por fecha seleccionada
    val filteredMovimientos = remember(movimientos, selectedDate) {
        movimientos.filter {
            val movDate = it.fecha.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            movDate.month == selectedDate.month && movDate.year == selectedDate.year
        }
    }

    // 2. Procesar datos para el gráfico (Total por Categoría)
    val expenseData = remember(filteredMovimientos) {
        filteredMovimientos
            .filter { it.tipoMovimiento == TipoMovimiento.GASTO_GENERAL || it.tipoMovimiento == TipoMovimiento.PAGO_DEUDA }
            .groupBy { it.categoria?.nombre ?: "Sin Categoría" }
            .mapValues { entry -> 
                entry.value.sumOf { Math.abs(it.monto) }
            }
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }

    // 3. Agrupar por Tipo de Presupuesto (Necesidad, Deseo, etc.)
    val groupedByType = remember(filteredMovimientos) {
        filteredMovimientos
            .filter { it.tipoMovimiento == TipoMovimiento.GASTO_GENERAL || it.tipoMovimiento == TipoMovimiento.PAGO_DEUDA }
            .groupBy { 
                it.categoria?.tipoPresupuesto ?: com.example.saveuplite.model.enums.TipoPresupuesto.OTROS 
            }
            .toSortedMap(compareBy { it.name }) // Orden alfabético por ahora
    }

    Scaffold(
        containerColor = com.example.saveuplite.ui.theme.SoftWhite,
        // bottomBar removed
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (expenseData.isNotEmpty()) {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 100.dp), // Increased bottom padding
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // 0. Header y Selector de Fecha (Ahora scrollean)
                        item {
                           Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Análisis de Gastos",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = com.example.saveuplite.ui.theme.DarkGrayText
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                com.example.saveuplite.ui.components.MonthYearSelector(
                                    currentDate = selectedDate,
                                    onDateChange = { viewModel.recargarEjecucionFecha(it) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                           }
                        }

                        // 1. Insights Card (Ahora scrollea)
                        // --- Insight Card ---
                        if (ejecucion != null) { // Check for null safely
                           item {
                                val exec = ejecucion!! // Safe non-null assertion
                                val needsPercent = if (exec.presupuestoNecesidades > 0) (exec.gastoNecesidades / exec.presupuestoNecesidades) * 100 else 0.0
                                val wantsPercent = if (exec.presupuestoDeseos > 0) (exec.gastoDeseos / exec.presupuestoDeseos) * 100 else 0.0
                                
                                // Lógica simple de insight
                                val insightTitle: String
                                val insightBody: String
                                val insightColor: Color
                                val insightIcon: androidx.compose.ui.graphics.vector.ImageVector
                                
                                if (needsPercent > 100) {
                                    insightTitle = "Atención con tus Necesidades"
                                    insightBody = "Tus gastos fijos superan el presupuesto asignado (${exec.porcentajeNecesidadesConfigurado.toInt()}%). Revisa categorías como Arriendo o Supermercado."
                                    insightColor = com.example.saveuplite.ui.theme.PalePink
                                    insightIcon = Icons.Default.Warning
                                } else if (wantsPercent > 100) {
                                    insightTitle = "Ojo con los 'Gustitos'"
                                    insightBody = "Has excedido tu límite para Deseos (${exec.porcentajeDeseosConfigurado.toInt()}%). Intenta reducir compras impulsivas lo que queda del mes."
                                    insightColor = com.example.saveuplite.ui.theme.PalePink // O un Naranja
                                    insightIcon = Icons.Default.Favorite // Corazón
                                } else if (needsPercent < 90 && wantsPercent < 90) {
                                    insightTitle = "¡Excelente Salud Financiera!"
                                    insightBody = "Tus gastos están bajo control. Tienes un margen perfecto para aumentar tu Ahorro este mes. ¡Sigue así!"
                                    insightColor = com.example.saveuplite.ui.theme.PaleAqua
                                    insightIcon = Icons.Default.ThumbUp
                                } else {
                                    insightTitle = "Estás dentro del Presupuesto"
                                    insightBody = "Tus gastos van acordes a lo planificado. Mantén el ritmo para cerrar el mes en verde."
                                    insightColor = com.example.saveuplite.ui.theme.PaleTeal
                                    insightIcon = Icons.Default.CheckCircle
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = insightColor.copy(alpha=0.3f)), // Un fondo suave
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = insightIcon,
                                            contentDescription = null,
                                            tint = com.example.saveuplite.ui.theme.DarkGrayText,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = insightTitle,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                color = com.example.saveuplite.ui.theme.DarkGrayText
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = insightBody,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = com.example.saveuplite.ui.theme.DarkGrayText
                                            )
                                        }
                                    }
                                }
                           }
                        }

                        // 2. Tarjeta del Gráfico
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
                                        text = "Distribución Mensual",
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

                        // 3. Lista Agrupada por Tipo
                        groupedByType.forEach { (tipo, movs) ->
                            val totalTipo = movs.sumOf { Math.abs(it.monto) }
                            
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Header del Grupo (ej: NECESIDAD)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tipo.name, // Podríamos formatear esto mejor
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            color = when(tipo) {
                                                com.example.saveuplite.model.enums.TipoPresupuesto.NECESIDAD -> com.example.saveuplite.ui.theme.DarkTeal
                                                com.example.saveuplite.model.enums.TipoPresupuesto.DESEO -> com.example.saveuplite.ui.theme.SaturatedSalmon
                                                else -> com.example.saveuplite.ui.theme.MediumGrayText
                                            }
                                        )
                                        Text(
                                            text = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "CL")).format(totalTipo),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            color = com.example.saveuplite.ui.theme.DarkGrayText
                                        )
                                    }
                                    
                                    // Lista de Categorías dentro de este Tipo
                                    val categoriasEnGrupo = movs.groupBy { it.categoria?.nombre ?: "Varios" }
                                        .mapValues { it.value.sumOf { m -> Math.abs(m.monto) } }
                                        .toList()
                                        .sortedByDescending { it.second }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            categoriasEnGrupo.forEachIndexed { index, (catName, amount) ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(catName, style = MaterialTheme.typography.bodyMedium, color = com.example.saveuplite.ui.theme.DarkGrayText)
                                                    Text(
                                                        java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "CL")).format(amount),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                        color = com.example.saveuplite.ui.theme.DarkGrayText
                                                    )
                                                }
                                                if (index < categoriasEnGrupo.size - 1) {
                                                    Divider(color = com.example.saveuplite.ui.theme.LightGray.copy(alpha = 0.5f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                     Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(), 
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            com.example.saveuplite.ui.components.MonthYearSelector(
                                currentDate = selectedDate,
                                onDateChange = { viewModel.recargarEjecucionFecha(it) }
                                // No modifier needed here potentially or wrap in Box
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                "No hay datos para este mes.",
                                color = com.example.saveuplite.ui.theme.MediumGrayText
                            )
                        }
                     }
                }
            }
            
            // Barra de navegación flotante
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                SoftUiBottomNav(navController = navController)
            }
        }
    }
}
