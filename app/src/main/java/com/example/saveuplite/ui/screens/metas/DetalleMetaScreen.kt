package com.example.saveuplite.ui.screens.metas

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.viewmodel.MetaAhorroViewModel
import com.example.saveuplite.viewmodel.UsuarioViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMetaScreen(
    navController: NavController,
    metaId: Long,
    usuarioViewModel: UsuarioViewModel,
    metaAhorroViewModel: MetaAhorroViewModel
) {
    val metaAhorroState by metaAhorroViewModel.uiState.collectAsState()
    val usuarioState by usuarioViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val meta = remember(metaAhorroState.metas, metaId) {
        metaAhorroState.metas.find { it.id == metaId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(meta?.nombre ?: "Detalle de la Meta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.editGoal(metaId)) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar Meta")
                    }
                    IconButton(onClick = { 
                        usuarioState.currentUser?.rut?.let { rut ->
                            val onSuccessAction = {
                                navController.navigate(Routes.GOALS) {
                                    popUpTo(Routes.GOALS) { inclusive = true }
                                }
                                Toast.makeText(context, "Meta eliminada con éxito", Toast.LENGTH_SHORT).show()
                            }
                            metaAhorroViewModel.eliminarMeta(rut, metaId, onSuccessAction)
                        }
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar Meta")
                    }
                }
            )
        }
    ) { padding ->
        if (meta == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (metaAhorroState.isLoading) CircularProgressIndicator() else Text("Meta no encontrada o inválida.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (meta.fechaLimite != null) {
                    val dateFormat = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }
                    Text("Para el ${dateFormat.format(meta.fechaLimite)}", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (meta.montoObjetivo != null && meta.montoObjetivo > 0) {
                    val progress = (meta.montoAhorrado / meta.montoObjetivo).toFloat().coerceIn(0f, 1f)
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(200.dp),
                        strokeWidth = 16.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = formatToCLP(meta.montoAhorrado),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "de ${formatToCLP(meta.montoObjetivo)}",
                        style = MaterialTheme.typography.titleLarge
                    )
                } else {
                    Text(
                        text = formatToCLP(meta.montoAhorrado),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ahorrado",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                     Button(
                        onClick = { navController.navigate(Routes.abonoRetiroMeta(metaId, "retiro")) },
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("Retirar")
                    }
                    Button(
                        onClick = { navController.navigate(Routes.abonoRetiroMeta(metaId, "abono")) },
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("Abonar")
                    }
                }
            }
        }
    }
}

private fun formatToCLP(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    format.maximumFractionDigits = 0
    return format.format(amount).replace(",", ".")
}
