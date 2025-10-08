package com.example.saveuplite.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenExpanded(navController: NavHostController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Home - Expanded") }) }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Panel izquierdo")
            }
            Column(modifier = Modifier.weight(2f)) {
                Text("Contenido central")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Panel derecho")
            }
        }
    }
}
