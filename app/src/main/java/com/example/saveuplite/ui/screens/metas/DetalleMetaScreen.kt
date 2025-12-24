package com.example.saveuplite.ui.screens.metas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.ui.theme.*
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
        containerColor = SoftWhite,
        topBar = {
            TopAppBar(
                title = { Text(meta?.nombre ?: "Detalle de la Meta", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (meta != null) {
                        IconButton(onClick = { navController.navigate(Routes.editGoal(metaId)) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar Meta", tint = LavenderBlue)
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
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar Meta", tint = DangerRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftWhite)
            )
        }
    ) { padding ->
        if (meta == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (metaAhorroState.isLoading) CircularProgressIndicator(color = LavenderBlue) else Text("Meta no encontrada o inválida.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = PaleAqua),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (meta.fechaLimite != null) {
                    val dateFormat = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }
                    Surface(
                        color = PaleAqua,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Meta para el ${dateFormat.format(meta.fechaLimite)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = DarkGrayText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                if (meta.montoObjetivo != null && meta.montoObjetivo > 0) {
                    val progress = (meta.montoAhorrado / meta.montoObjetivo).toFloat().coerceIn(0f, 1f)
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(240.dp),
                            color = LightGray,
                            strokeWidth = 20.dp,
                            strokeCap = StrokeCap.Round,
                        )
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(240.dp),
                            color = LavenderBlue,
                            strokeWidth = 20.dp,
                            strokeCap = StrokeCap.Round,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = LavenderBlue
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatToCLP(meta.montoAhorrado),
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = DarkGrayText
                        )
                        Text(
                            text = "de ${formatToCLP(meta.montoObjetivo)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MediumGrayText
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = formatToCLP(meta.montoAhorrado),
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                        color = LavenderBlue
                    )
                    Text(
                        text = "Ahorrado Total",
                        style = MaterialTheme.typography.titleLarge,
                        color = MediumGrayText
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                     Button(
                        onClick = { navController.navigate(Routes.abonoRetiroMeta(metaId, "retiro")) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PalePink,
                            contentColor = DangerRed
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Retirar", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { navController.navigate(Routes.abonoRetiroMeta(metaId, "abono")) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LavenderBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Abonar", fontWeight = FontWeight.Bold)
                    }
                }
                        Spacer(modifier = Modifier.height(16.dp))
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
