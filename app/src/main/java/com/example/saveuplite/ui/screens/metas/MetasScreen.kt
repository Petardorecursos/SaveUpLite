package com.example.saveuplite.ui.screens.metas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.saveuplite.model.meta.MetaAhorro
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.ui.theme.*
import com.example.saveuplite.ui.components.SoftUiBottomNav
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
        containerColor = SoftWhite,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Metas de Ahorro", 
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = DarkGrayText
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftWhite)
            )
        },
        bottomBar = { SoftUiBottomNav(navController = navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.CREATE_GOAL) },
                containerColor = LavenderBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
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
            Spacer(modifier = Modifier.height(8.dp))
            TotalAhorradoCard(totalAhorrado = metaAhorroState.totalAhorrado)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Mis Metas", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold,
                color = DarkGrayText
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (metaAhorroState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LavenderBlue)
                }
            } else if (metaAhorroState.metas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aún no tienes metas. ¡Crea una!", color = MediumGrayText)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(metaAhorroState.metas) { index, meta ->
                        MetaAhorroItem(meta = meta, index = index, onClick = { navController.navigate(Routes.detailGoal(meta.id)) })
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
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Balance Total", 
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = formatToCLP(totalAhorrado),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Disponible en tus metas",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun MetaAhorroItem(meta: MetaAhorro, index: Int, onClick: () -> Unit) {
    // Cycling pastel colors
    val cardColors = listOf(PaleAqua, PalePink, PaleSalmon, PaleTeal, LightBlueBg)
    val backgroundColor = cardColors[index % cardColors.size]
    
    // Choose text color based on background (DarkGrayText usually works good on pastels)
    val contentColor = DarkGrayText

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    meta.nombre, 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
                if (meta.fechaLimite != null) {
                    Text(
                        // Simple formatting, could be improved
                        text = "Vence: ${java.text.SimpleDateFormat("dd/MM", Locale.getDefault()).format(meta.fechaLimite)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (meta.montoObjetivo != null && meta.montoObjetivo > 0) {
                val progress = (meta.montoAhorrado / meta.montoObjetivo).toFloat().coerceIn(0f, 1f)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = formatToCLP(meta.montoAhorrado),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = contentColor
                    )
                    Text(
                        text = "de ${formatToCLP(meta.montoObjetivo)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                 Text(
                    text = "${(progress * 100).toInt()}% completado",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.8f)
                )

            } else {
                Text(
                    text = formatToCLP(meta.montoAhorrado),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = contentColor
                )
                Text(
                    text = "Ahorrado",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f)
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
