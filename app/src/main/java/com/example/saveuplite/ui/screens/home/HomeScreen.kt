package com.example.saveuplite.ui.screens.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.saveuplite.ui.utils.obtenerWindowSizeClass
import com.example.saveuplite.viewmodel.UsuarioViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    usuarioViewModel: UsuarioViewModel = viewModel() // <--- Añadido aquí
) {
    val windowSizeClass = obtenerWindowSizeClass()

    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> HomeScreenCompact(navController, usuarioViewModel)
        WindowWidthSizeClass.Medium -> HomeScreenMedium(navController, usuarioViewModel)
        WindowWidthSizeClass.Expanded -> HomeScreenExpanded(navController, usuarioViewModel)
        else -> HomeScreenCompact(navController, usuarioViewModel) // fallback
    }
}
