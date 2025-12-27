package com.example.saveuplite.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.ui.theme.DarkGrayText
import com.example.saveuplite.ui.theme.LavenderBlue

sealed class NavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : NavItem(Routes.HOME, Icons.Filled.Home, "Inicio")
    object Debts : NavItem(Routes.DEBTS, Icons.Filled.AccountBalanceWallet, "Deudas")
    object Goals : NavItem(Routes.GOALS, Icons.Filled.Star, "Metas")
    object Analysis : NavItem(Routes.ANALYSIS, Icons.Filled.PieChart, "Análisis")
}

@Composable
fun SoftUiBottomNav(navController: NavController) {
    val items = listOf(NavItem.Home, NavItem.Debts, NavItem.Goals, NavItem.Analysis)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp), spotColor = Color(0x30DEDEE0))
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp), spotColor = Color.White.copy(alpha = 0.9f))
                .background(Color.White, RoundedCornerShape(32.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            items.forEach { item ->
                SoftUiBottomNavItem(item = item, isSelected = currentRoute == item.route) {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }
}

@Composable
fun SoftUiBottomNavItem(item: NavItem, isSelected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        if (isSelected) {
            Box(
                modifier = Modifier.size(48.dp).shadow(4.dp, CircleShape, spotColor = Color(0x30DEDEE0)).shadow(4.dp, CircleShape, spotColor = Color.White.copy(alpha = 0.9f)).background(LavenderBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(item.icon, contentDescription = item.label, tint = Color.White) }
        } else {
            Icon(item.icon, contentDescription = item.label, tint = DarkGrayText.copy(alpha = 0.8f), modifier = Modifier.padding(12.dp))
        }
    }
}
