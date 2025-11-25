package com.example.saveuplite.ui.screens.dashboard

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.saveuplite.model.dto.MovimientoResponseDTO
import com.example.saveuplite.model.enums.TipoMovimiento
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.ui.theme.*
import com.example.saveuplite.viewmodel.DashboardViewModel
import com.example.saveuplite.viewmodel.UsuarioViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// --- sealed class para los items de navegación ---
sealed class NavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : NavItem(Routes.HOME, Icons.Filled.Home, "Inicio")
    object Debts : NavItem(Routes.DEBTS, Icons.Filled.AccountBalanceWallet, "Deudas")
    object Goals : NavItem(Routes.GOALS, Icons.Filled.Star, "Metas")
    object Market : NavItem(Routes.MARKET, Icons.Filled.ShowChart, "Mercado")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    usuarioViewModel: UsuarioViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val usuarioState by usuarioViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // --- Estados para diálogos y selección de imagen ---
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var tipoMovimientoDialog by remember { mutableStateOf(TipoMovimiento.INGRESO_GENERAL) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // --- Launchers para permisos y selección de imagen ---
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) { profileImageUri = tempCameraUri }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        profileImageUri = uri
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val imageDir = File(context.filesDir, "images").apply { mkdirs() }
            val photoFile = File(imageDir, "profile_image_${System.currentTimeMillis()}.jpg")
            tempCameraUri = FileProvider.getUriForFile(context, "com.example.saveuplite.fileprovider", photoFile)
            cameraLauncher.launch(tempCameraUri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }
    val galleryPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) { galleryLauncher.launch("image/*") }
        else { Toast.makeText(context, "Permiso de galería denegado", Toast.LENGTH_SHORT).show() }
    }

    // --- Efectos para carga de datos y errores ---
    LaunchedEffect(usuarioState.currentUser) {
        usuarioState.currentUser?.rut?.let { dashboardViewModel.cargarDatosDashboard(it) }
    }
    LaunchedEffect(dashboardState.errorMessage) {
        dashboardState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            dashboardViewModel.clearError()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                navController = navController,
                usuarioViewModel = usuarioViewModel,
                profileImageUri = profileImageUri,
                onImageClick = { showImageSourceDialog = true },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { TopBar(scope, drawerState, usuarioState.currentUser?.nombre) },
            bottomBar = { SoftUiBottomNav(navController = navController) }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (dashboardState.isLoading && dashboardState.historialMovimientos.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                } else {
                    Spacer(Modifier.height(16.dp))
                    BalanceCard(dashboardState.saldoActual)
                    Spacer(Modifier.height(24.dp))
                    ActionButtons(
                        onIngresoClick = { tipoMovimientoDialog = TipoMovimiento.INGRESO_GENERAL; showAddTransactionDialog = true },
                        onGastoClick = { tipoMovimientoDialog = TipoMovimiento.GASTO_GENERAL; showAddTransactionDialog = true }
                    )
                    Spacer(Modifier.height(24.dp))
                    TransactionHistory(
                        historial = dashboardState.historialMovimientos,
                        onNavigateToHistory = { navController.navigate(Routes.TRANSACTION_HISTORY) }
                    )
                }
            }
        }
    }

    // --- Diálogos ---
    if (showAddTransactionDialog) {
        AddTransactionDialog(
            tipo = tipoMovimientoDialog,
            onDismiss = { showAddTransactionDialog = false },
            onConfirm = {
                usuarioState.currentUser?.rut?.let { rut -> dashboardViewModel.registrarMovimiento(rut, it.first, it.second, tipoMovimientoDialog) }
                showAddTransactionDialog = false
            }
        )
    }
    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showImageSourceDialog = false },
            onCameraClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
            onGalleryClick = {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
                galleryPermissionLauncher.launch(permission)
            }
        )
    }
}

// --- Componentes de la UI ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(scope: CoroutineScope, drawerState: DrawerState, userName: String?) {
    TopAppBar(
        title = { Text("Hola, ${userName ?: ""}!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                Icon(Icons.Filled.Menu, contentDescription = "Abrir menú", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun DrawerContent(navController: NavHostController, usuarioViewModel: UsuarioViewModel, profileImageUri: Uri?, onImageClick: () -> Unit, onCloseDrawer: () -> Unit) {
    val usuarioState by usuarioViewModel.uiState.collectAsState()
    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)).clickable(onClick = onImageClick),
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUri != null) {
                    AsyncImage(model = profileImageUri, contentDescription = "Imagen de perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = "Ícono de usuario", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(text = usuarioState.currentUser?.nombre ?: "Invitado", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = usuarioState.currentUser?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))
            NavigationDrawerItem(icon = { Icon(Icons.Default.Calculate, null) }, label = { Text("Conversor de Moneda") }, selected = false, onClick = { navController.navigate(Routes.CONVERTER); onCloseDrawer() }, shape = RoundedCornerShape(12.dp))
            NavigationDrawerItem(icon = { Icon(Icons.Default.Info, null) }, label = { Text("Funciones Legacy") }, selected = false, onClick = { navController.navigate(Routes.LEGACY_HOME); onCloseDrawer() }, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { usuarioViewModel.logout(); navController.navigate(Routes.AUTH) { popUpTo(navController.graph.startDestinationId) { inclusive = true } } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = DarkGrayText)
            ) { Text("Cerrar Sesión") }
        }
    }
}

@Composable
fun SoftUiBottomNav(navController: NavController) {
    val items = listOf(NavItem.Home, NavItem.Debts, NavItem.Goals, NavItem.Market)
    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x30DEDEE0)).shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp), spotColor = Color.White.copy(alpha = 0.9f)).background(Color.White, RoundedCornerShape(24.dp)).padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
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

@Composable
fun BalanceCard(saldoActual: Double) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MediumBlue), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.Start) {
            Text("Saldo Actual", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = formatToCLP(saldoActual, false), style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ActionButtons(onIngresoClick: () -> Unit, onGastoClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = onIngresoClick, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = PaleAqua, contentColor = DarkGrayText)) {
            Icon(Icons.Filled.Add, contentDescription = "Ingreso")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ingreso", fontWeight = FontWeight.SemiBold)
        }
        Button(onClick = onGastoClick, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = PalePink, contentColor = DarkGrayText)) {
            Icon(Icons.Filled.Remove, contentDescription = "Gasto")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gasto", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun TransactionHistory(historial: List<MovimientoResponseDTO>, onNavigateToHistory: () -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToHistory).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Últimos Movimientos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Icon(Icons.Default.ChevronRight, contentDescription = "Ver historial completo", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (historial.isEmpty()) {
            Text("Aún no tienes movimientos.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                historial.take(3).forEach { movimiento -> TransactionItem(item = movimiento) }
            }
        }
    }
}

@Composable
fun TransactionItem(item: MovimientoResponseDTO) {
    val isIncome = item.monto > 0
    val amountColor = if (isIncome) DesaturatedPurple else DarkGrayText
    val formattedAmount = formatToCLP(item.monto, true)

    val dateFormat = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.descripcion, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = dateFormat.format(item.fecha), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = formattedAmount, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = amountColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(tipo: TipoMovimiento, onDismiss: () -> Unit, onConfirm: (Pair<Double, String>) -> Unit) {
    var monto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = { Text(if (tipo == TipoMovimiento.INGRESO_GENERAL) "Añadir Ingreso" else "Añadir Gasto") },
        text = {
            Column {
                OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Monto") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = { Button(onClick = { onConfirm(Pair(monto.toDoubleOrNull() ?: 0.0, descripcion)) }, shape = RoundedCornerShape(12.dp)) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ImageSourceDialog(onDismiss: () -> Unit, onCameraClick: () -> Unit, onGalleryClick: () -> Unit) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Foto de Perfil") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { onDismiss(); onCameraClick() }, modifier = Modifier.fillMaxWidth()) { Text("Tomar Foto", modifier = Modifier.padding(8.dp)) }
                TextButton(onClick = { onDismiss(); onGalleryClick() }, modifier = Modifier.fillMaxWidth()) { Text("Seleccionar de Galería", modifier = Modifier.padding(8.dp)) }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun formatToCLP(amount: Double, withSign: Boolean): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    format.maximumFractionDigits = 0
    val formattedText = format.format(amount).replace(",", ".")

    if (withSign && amount > 0) {
        return "+ $formattedText"
    }
    return formattedText
}
