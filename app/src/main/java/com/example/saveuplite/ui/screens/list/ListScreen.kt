package com.example.saveuplite.ui.screens.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.saveuplite.data.DatabaseHelper
import com.example.saveuplite.data.UsuarioDB
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val usuarios = remember { mutableStateListOf<UsuarioDB>() }

    // Cargar los usuarios al iniciar
    LaunchedEffect(Unit) {
        usuarios.clear()
        usuarios.addAll(dbHelper.obtenerUsuarios())
    }

    // 🎨 Fondo degradado verde → negro
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF00C853), // verde brillante
            Color(0xFF004D40), // verde oscuro
            Color.Black         // negro base
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lista de Usuarios",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF004D40)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {
            if (usuarios.isEmpty()) {
                // Mensaje cuando no hay datos
                Text(
                    text = "No hay usuarios registrados 😢",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Lista de usuarios
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(usuarios) { usuario ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Text("👤 ${usuario.nombre}", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("🆔 RUT: ${usuario.rut}", color = Color(0xFFB2DFDB))
                                Text("💰 Ingreso: ${usuario.ingreso} CLP", color = Color(0xFFB2DFDB))
                                Text("📝 ${usuario.descripcion}", color = Color(0xFFB2DFDB))
                            }
                        }
                    }
                }
            }
        }
    }
}
