package com.example.saveuplite.ui.screens.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.saveuplite.ui.utils.obtenerWindowSizeClass

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val windowSizeClass = obtenerWindowSizeClass()

    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> HomeScreenCompact(navController)
        WindowWidthSizeClass.Medium -> HomeScreenMedium(navController)
        WindowWidthSizeClass.Expanded -> HomeScreenExpanded(navController)
        else -> HomeScreenCompact(navController) // fallback
    }
}
