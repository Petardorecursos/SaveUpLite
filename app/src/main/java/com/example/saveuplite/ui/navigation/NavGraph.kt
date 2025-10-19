package com.example.saveuplite.ui.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.example.saveuplite.ui.screens.auth.AuthScreen
import com.example.saveuplite.ui.screens.home.HomeScreen
import com.example.saveuplite.ui.screens.form.FormScreen
import com.example.saveuplite.ui.screens.list.ListScreen
import com.example.saveuplite.ui.screens.nativeView.LocationScreen
import com.example.saveuplite.ui.screens.nativeView.NotificationScreen
import com.example.saveuplite.viewmodel.LocationViewModel
import com.example.saveuplite.viewmodel.UsuarioViewModel

object Routes {
    const val HOME = "home"
    const val FORM = "form"
    const val LOCATION = "location"
    const val LIST = "list"
    const val NOTIFICATION = "notification"
    const val AUTH = "auth" // <-- Ruta añadida
}

@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(navController: NavHostController) {
    val usuarioViewModel: UsuarioViewModel = viewModel()

    AnimatedNavHost(
        navController = navController,
        startDestination = Routes.AUTH, // <-- Cambiado spara que inicie en Auth
        enterTransition = { fadeIn() + slideInHorizontally(initialOffsetX = { 300 }) },
        exitTransition = { fadeOut() + slideOutHorizontally(targetOffsetX = { -300 }) },
        popEnterTransition = { fadeIn() + slideInHorizontally(initialOffsetX = { -300 }) },
        popExitTransition = { fadeOut() + slideOutHorizontally(targetOffsetX = { 300 }) }
    ) {
        // --- Nueva ruta de autenticación ---
        composable(Routes.AUTH) { AuthScreen(navController, usuarioViewModel) }

        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.FORM) { FormScreen(navController) }
        composable(Routes.NOTIFICATION) { NotificationScreen(navController) }
        composable(Routes.LOCATION) {
            val locationViewModel: LocationViewModel = viewModel()
            LocationScreen(viewModel = locationViewModel, navController = navController)
        }
        composable(Routes.LIST) { ListScreen(navController) }
    }
}
