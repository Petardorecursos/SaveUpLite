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
                if (lastIndex != null && !historyState.isLoadingNextPage && lastIndex >= historyState.movements.size - 5 && historyState.canLoadMore) {
                    usuarioState.currentUser?.rut?.let { historyViewModel.loadNextPage(it) }
                }
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Historial Completo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
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
                    items(historyState.movements) { movimiento ->
                        TransactionItem(item = movimiento)
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
