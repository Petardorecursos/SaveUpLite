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
import androidx.navigation.NavType
import androidx.navigation.navArgument
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
import com.example.saveuplite.ui.screens.metas.*
import com.example.saveuplite.ui.screens.nativeView.LocationScreen
import com.example.saveuplite.ui.screens.nativeView.NotificationScreen
import com.example.saveuplite.viewmodel.*

object Routes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val TRANSACTION_HISTORY = "transaction_history"
    
    // --- Nuevas Rutas ---
    const val DEBTS = "debts"
    const val ADD_DEBT = "add_debt"
    const val GOALS = "goals"
    const val CREATE_GOAL = "create_goal"
    const val DETAIL_GOAL = "detail_goal/{metaId}"
    const val EDIT_GOAL = "edit_goal/{metaId}" 
    const val ABONO_RETIRO_META = "abono_retiro_meta/{metaId}/{tipo}"
    const val MARKET = "market"
    const val ANALYSIS = "analysis"
    const val CONVERTER = "converter"

    fun detailGoal(metaId: Long) = "detail_goal/$metaId"
    fun abonoRetiroMeta(metaId: Long, tipo: String) = "abono_retiro_meta/$metaId/$tipo"
    fun editGoal(metaId: Long) = "edit_goal/$metaId" 

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
    val application = LocalContext.current.applicationContext as Application
    val factory = AuthViewModelFactory(RetrofitClient.apiService, application)
    val usuarioViewModel: UsuarioViewModel = viewModel(factory = factory)
    val metaAhorroViewModel: MetaAhorroViewModel = viewModel()

    AnimatedNavHost(
        navController = navController,
        startDestination = Routes.AUTH
    ) {
        composable(Routes.AUTH) { AuthScreen(navController, usuarioViewModel) }
        composable(Routes.HOME) { DashboardScreen(navController, usuarioViewModel) }
        composable(Routes.TRANSACTION_HISTORY) { TransactionHistoryScreen(navController, usuarioViewModel) }

        composable(Routes.DEBTS) { DeudasScreen(navController, usuarioViewModel) }
        composable(Routes.ADD_DEBT) { AddDeudaScreen(navController, usuarioViewModel) }
        composable(Routes.GOALS) { MetasScreen(navController, usuarioViewModel, metaAhorroViewModel) } 
        composable(Routes.CREATE_GOAL) { CrearMetaScreen(navController, usuarioViewModel, metaAhorroViewModel) } 
        composable(
            route = Routes.DETAIL_GOAL,
            arguments = listOf(navArgument("metaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val metaId = backStackEntry.arguments?.getLong("metaId") ?: -1
            DetalleMetaScreen(navController, metaId, usuarioViewModel, metaAhorroViewModel)
        }
        composable(
            route = Routes.EDIT_GOAL,
            arguments = listOf(navArgument("metaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val metaId = backStackEntry.arguments?.getLong("metaId") ?: -1
            EditarMetaScreen(navController, metaId, usuarioViewModel, metaAhorroViewModel)
        }
        composable(
            route = Routes.ABONO_RETIRO_META,
            arguments = listOf(
                navArgument("metaId") { type = NavType.LongType },
                navArgument("tipo") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val metaId = backStackEntry.arguments?.getLong("metaId") ?: -1
            val tipo = backStackEntry.arguments?.getString("tipo") ?: "abono"
            AbonoRetiroScreen(navController, metaId, tipo, usuarioViewModel, metaAhorroViewModel)
        }
        composable(Routes.MARKET) { MarketScreen(navController = navController) }
        composable(Routes.ANALYSIS) { 
            val analysisViewModel: AnalysisViewModel = viewModel()
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
