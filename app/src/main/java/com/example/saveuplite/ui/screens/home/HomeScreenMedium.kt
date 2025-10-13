package com.example.saveuplite.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenMedium(navController: NavHostController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Home - Medium") }) }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Columna izquierda")
                Button(onClick = { /* acción */ }) {
                    Text("Botón Medium")
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Columna derecha")
            }
        }
    }
}
