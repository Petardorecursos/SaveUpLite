package com.example.saveuplite.ui.navigation

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.example.saveuplite.api.RetrofitClient
import com.example.saveuplite.ui.screens.auth.AuthScreen
import com.example.saveuplite.ui.screens.converter.ConverterScreen
import com.example.saveuplite.ui.screens.dashboard.DashboardScreen
import com.example.saveuplite.ui.screens.AnalysisScreen
import com.example.saveuplite.ui.screens.deudas.AddDeudaScreen
import com.example.saveuplite.ui.screens.deudas.DeudasScreen
import com.example.saveuplite.ui.screens.history.TransactionHistoryScreen
import com.example.saveuplite.ui.screens.home.HomeScreen
import com.example.saveuplite.ui.screens.form.FormScreen
import com.example.saveuplite.ui.screens.list.ListScreen
import com.example.saveuplite.ui.screens.market.MarketScreen
import com.example.saveuplite.ui.screens.nativeView.LocationScreen
import com.example.saveuplite.ui.screens.nativeView.NotificationScreen
import com.example.saveuplite.viewmodel.AuthViewModelFactory
import com.example.saveuplite.viewmodel.LocationViewModel
import com.example.saveuplite.viewmodel.UsuarioViewModel
import com.example.saveuplite.viewmodel.AnalysisViewModel

object Routes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val TRANSACTION_HISTORY = "transaction_history"
    
    // --- Nuevas Rutas ---
    const val DEBTS = "debts"
    const val ADD_DEBT = "add_debt" // <-- NUEVA RUTA
    const val GOALS = "goals"
    const val MARKET = "market"
    const val ANALYSIS = "analysis" // <-- NUEVA RUTA
    const val CONVERTER = "converter"

    // --- Rutas Legacy ---
    const val LEGACY_HOME = "legacyhome"
    const val FORM = "form"
    const val LOCATION = "location"
    const val LIST = "list"
    const val NOTIFICATION = "notification"
}

@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(navController: NavHostController) {
    // Para instanciar un ViewModel con un constructor personalizado, necesitamos su Factory.
    val application = LocalContext.current.applicationContext as Application
    val factory = AuthViewModelFactory(RetrofitClient.apiService, application)
    val usuarioViewModel: UsuarioViewModel = viewModel(factory = factory)

    AnimatedNavHost(
        navController = navController,
        startDestination = Routes.AUTH
    ) {
        // AuthScreen ahora recibe el ViewModel como parámetro.
        composable(Routes.AUTH) { AuthScreen(navController, usuarioViewModel) }
        
        // Las otras pantallas sí usan la instancia compartida creada aquí.
        composable(Routes.HOME) { DashboardScreen(navController, usuarioViewModel) }
        composable(Routes.TRANSACTION_HISTORY) { TransactionHistoryScreen(navController, usuarioViewModel) }

        // --- Pantallas Nuevas ---
        composable(Routes.DEBTS) { DeudasScreen(navController, usuarioViewModel) }
        composable(Routes.ADD_DEBT) { AddDeudaScreen(navController, usuarioViewModel) }
        composable(Routes.GOALS) { PlaceholderScreen(screenName = "Metas de Ahorro") }
        composable(Routes.MARKET) { MarketScreen(navController = navController) }
        composable(Routes.ANALYSIS) { 
            val analysisViewModel: AnalysisViewModel = viewModel()
            // Se asume que el usuario esta logueado si llega aca
            val rut = usuarioViewModel.uiState.value.currentUser?.rut ?: ""
            AnalysisScreen(navController, analysisViewModel, rut) 
        }
        composable(Routes.CONVERTER) { ConverterScreen(navController = navController) }

        // --- Rutas de funciones adicionales (Legacy) ---
        composable(Routes.LEGACY_HOME) { HomeScreen(navController, usuarioViewModel) }
        composable(Routes.FORM) { FormScreen(navController) }
        composable(Routes.NOTIFICATION) { NotificationScreen(navController) }
        composable(Routes.LOCATION) {
            val locationViewModel: LocationViewModel = viewModel()
            LocationScreen(viewModel = locationViewModel, navController = navController)
        }
        composable(Routes.LIST) { ListScreen(navController) }
    }
}

@Composable
fun PlaceholderScreen(screenName: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Pantalla '$screenName' - En construcción.")
    }
}
