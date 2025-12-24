package com.example.saveuplite.ui.screens.metas

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.saveuplite.model.meta.MetaAhorro
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.viewmodel.MetaAhorroViewModel
import com.example.saveuplite.viewmodel.UsuarioViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetasScreen(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel,
    metaAhorroViewModel: MetaAhorroViewModel
) {
    val usuarioState by usuarioViewModel.uiState.collectAsState()
    val metaAhorroState by metaAhorroViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(usuarioState.currentUser) {
        usuarioState.currentUser?.rut?.let { rut ->
            metaAhorroViewModel.obtenerMetas(rut)
        }
    }

    LaunchedEffect(metaAhorroState.errorMessage) {
        metaAhorroState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            metaAhorroViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Metas de Ahorro") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.CREATE_GOAL) }) {
                Icon(Icons.Filled.Add, contentDescription = "Crear Meta")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            TotalAhorradoCard(totalAhorrado = metaAhorroState.totalAhorrado)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Mis Metas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (metaAhorroState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (metaAhorroState.metas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aún no tienes metas. ¡Crea una!")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(metaAhorroState.metas) { meta ->
                        MetaAhorroItem(meta = meta, onClick = { navController.navigate(Routes.detailGoal(meta.id)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalAhorradoCard(totalAhorrado: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Total Ahorrado", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatToCLP(totalAhorrado),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetaAhorroItem(meta: MetaAhorro, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(meta.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (meta.montoObjetivo != null && meta.montoObjetivo > 0) {
                val progress = (meta.montoAhorrado / meta.montoObjetivo).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = formatToCLP(meta.montoAhorrado),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                     Text(
                        text = formatToCLP(meta.montoObjetivo),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    text = "${formatToCLP(meta.montoAhorrado)} ahorrados",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun formatToCLP(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    format.maximumFractionDigits = 0
    return format.format(amount).replace(",", ".")
}
