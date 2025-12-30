package com.example.saveuplite.ui.screens.history

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.saveuplite.ui.screens.dashboard.TransactionItem // Reutilizamos el item rediseñado
import com.example.saveuplite.viewmodel.TransactionHistoryViewModel
import com.example.saveuplite.viewmodel.UsuarioViewModel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.filled.CalendarToday

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    navController: NavHostController,
    usuarioViewModel: UsuarioViewModel = viewModel(),
    historyViewModel: TransactionHistoryViewModel = viewModel()
) {
    val historyState by historyViewModel.uiState.collectAsState()
    val selectedDate by historyViewModel.selectedDate.collectAsState()
    val usuarioState by usuarioViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Carga inicial de movimientos
    LaunchedEffect(usuarioState.currentUser, selectedDate) { // React to date change
        usuarioState.currentUser?.rut?.let {
            // Load if empty OR if we switched modes (date changed)
            if (historyState.movements.isEmpty() || selectedDate != null) { 
                historyViewModel.loadInitialMovements(it)
            }
        }
    }

    // Muestra errores
    LaunchedEffect(historyState.errorMessage) {
        historyState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            historyViewModel.clearError()
        }
    }

    // Paginación (Solo si no hay filtro de fecha)
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (selectedDate == null && lastIndex != null && !historyState.isLoadingNextPage && lastIndex >= historyState.movements.size - 5 && historyState.canLoadMore) {
                    usuarioState.currentUser?.rut?.let { historyViewModel.loadNextPage(it) }
                }
            }
    }

    // Agrupar movimientos por fecha
    val groupedMovements = remember(historyState.movements) {
        historyState.movements.groupBy { 
            it.fecha.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate() 
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Historial Completo", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                
                // Selector de Fecha / Filtro
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    if (selectedDate == null) {
                        Button(
                            onClick = { 
                                usuarioState.currentUser?.rut?.let { 
                                    historyViewModel.setSelectedDate(java.time.LocalDate.now(), it) 
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Filtrar por Mes", color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    } else {
                        Column {
                            com.example.saveuplite.ui.components.MonthYearSelector(
                                currentDate = selectedDate!!,
                                onDateChange = { newDate ->
                                    usuarioState.currentUser?.rut?.let { historyViewModel.setSelectedDate(newDate, it) }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { usuarioState.currentUser?.rut?.let { historyViewModel.setSelectedDate(null, it) } },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Ver todo el historial")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (historyState.isLoading && historyState.movements.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (historyState.movements.isEmpty()) {
                Text(
                    text = "No se encontraron movimientos.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedMovements.forEach { (date, movements) ->
                        stickyHeader { 
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.background // O un color suave
                            ) {
                                Text(
                                    text = formatFriendlyDate(date),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        items(movements) { movimiento ->
                            TransactionItem(item = movimiento)
                        }
                    }
                    
                    if (historyState.isLoadingNextPage) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

// Función auxiliar para fechas amigables
private fun formatFriendlyDate(date: java.time.LocalDate): String {
    val hoy = java.time.LocalDate.now()
    val ayer = hoy.minusDays(1)
    
    return when {
        date.isEqual(hoy) -> "Hoy"
        date.isEqual(ayer) -> "Ayer"
        else -> date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", java.util.Locale("es", "ES"))).capitalize(java.util.Locale("es", "ES"))
    }
}
private fun String.capitalize(locale: java.util.Locale): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
