package com.example.saveuplite.ui.screens.history

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.saveuplite.ui.screens.dashboard.TransactionItem
import com.example.saveuplite.viewmodel.TransactionHistoryViewModel
import com.example.saveuplite.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    navController: NavHostController,
    usuarioViewModel: UsuarioViewModel = viewModel(),
    historyViewModel: TransactionHistoryViewModel = viewModel()
) {
    val historyState by historyViewModel.uiState.collectAsState()
    val usuarioState by usuarioViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Carga inicial de movimientos
    LaunchedEffect(usuarioState.currentUser) {
        usuarioState.currentUser?.rut?.let {
            if (historyState.movements.isEmpty()) { // Carga solo si la lista está vacía
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

    // Efecto para cargar la siguiente página cuando el usuario llega al final de la lista
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= historyState.movements.size - 1 && historyState.canLoadMore) {
                    usuarioState.currentUser?.rut?.let { historyViewModel.loadNextPage(it) }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial Completo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            if (historyState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (historyState.movements.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron movimientos.")
                }
            } else {
                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(historyState.movements) { movimiento ->
                        TransactionItem(item = movimiento)
                    }
                    if (historyState.isLoadingNextPage) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}
