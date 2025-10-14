package com.example.saveuplite.ui.screens.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons // 1. Importar Icons
import androidx.compose.material.icons.filled.ArrowBack // 2. Importar el ícono de flecha
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.saveuplite.data.DatabaseHelper
import com.example.saveuplite.data.UsuarioDB
import androidx.navigation.NavHostController

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@androidx.compose.runtime.Composable
fun ListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dbHelper = androidx.compose.runtime.remember { DatabaseHelper(context) }
    val usuarios = androidx.compose.runtime.remember { mutableStateListOf<UsuarioDB>() }

    // Cargar los usuarios al iniciar
    androidx.compose.runtime.LaunchedEffect(Unit) {
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

    androidx.compose.material3.Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    androidx.compose.material3.Text(
                        "Lista de Usuarios",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF004D40)
                ),
                // 3. Añadir el botón de navegación para volver
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver atrás",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {
            if (usuarios.isEmpty()) {
                // Mensaje cuando no hay datos
                androidx.compose.material3.Text(
                    text = "No hay usuarios registrados 😢",
                    color = Color.White,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Lista de usuarios
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    items(usuarios) { usuario ->
                        androidx.compose.material3.Card(
                            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.foundation.layout.Column(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                androidx.compose.material3.Text("👤 ${usuario.nombre}", color = Color.White, fontWeight = FontWeight.Bold)
                                androidx.compose.material3.Text("🆔 RUT: ${usuario.rut}", color = Color(0xFFB2DFDB))
                                androidx.compose.material3.Text("💰 Ingreso: ${usuario.ingreso} CLP", color = Color(0xFFB2DFDB))
                                androidx.compose.material3.Text("📝 ${usuario.descripcion}", color = Color(0xFFB2DFDB))
                            }
                        }
                    }
                }
            }
        }
    }
}
