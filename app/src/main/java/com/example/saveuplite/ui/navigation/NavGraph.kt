package com.example.saveuplite.ui.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel // 1. Importar el viewModel de compose
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.example.saveuplite.ui.screens.home.HomeScreen
import com.example.saveuplite.ui.screens.form.FormScreen
import com.example.saveuplite.ui.screens.list.ListScreen // 👈 nuevo import
import com.example.saveuplite.ui.screens.nativeView.LocationScreen
import com.example.saveuplite.viewmodel.LocationViewModel

object Routes {
    const val HOME = "home"
    const val FORM = "form"
    const val LOCATION = "location"
    const val LIST = "list"
}

@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(navController: NavHostController) {
    AnimatedNavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInHorizontally(initialOffsetX = { 300 }) },
        exitTransition = { androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -300 }) },
        popEnterTransition = { androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInHorizontally(initialOffsetX = { -300 }) },
        popExitTransition = { androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutHorizontally(targetOffsetX = { 300 }) }
    ) {
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.FORM) { FormScreen(navController) }
        composable(Routes.LOCATION) {
            // 2. Usar viewModel() para obtener la instancia correctamente
            val viewModel: LocationViewModel = viewModel()
            // 3. Pasar el navController a LocationScreen
            LocationScreen(viewModel = viewModel, navController = navController)
        }

        // 🧾 Nueva ruta para listar los registros
        composable(Routes.LIST) { ListScreen(navController) }
    }
}
