package com.example.saveuplite.ui.screens.dashboard

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.graphics.Brush
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
import com.example.saveuplite.model.dto.CategoriaDTO
import com.example.saveuplite.ui.components.CategorySelector
import com.example.saveuplite.viewmodel.UsuarioViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

import com.example.saveuplite.ui.components.SoftUiBottomNav

import com.example.saveuplite.viewmodel.MetaAhorroViewModel
import com.example.saveuplite.viewmodel.DeudaViewModel
import com.example.saveuplite.viewmodel.DeudaViewModelFactory
import com.example.saveuplite.api.RetrofitClient
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    usuarioViewModel: UsuarioViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel(),
    metaAhorroViewModel: MetaAhorroViewModel = viewModel(),
    deudaViewModel: DeudaViewModel = viewModel(factory = DeudaViewModelFactory(RetrofitClient.apiService))
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val usuarioState by usuarioViewModel.uiState.collectAsState()
    val metaAhorroState by metaAhorroViewModel.uiState.collectAsState()
    val deudaState by deudaViewModel.uiState.collectAsState()
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
        usuarioState.currentUser?.rut?.let { 
            dashboardViewModel.cargarDatosDashboard(it) 
            dashboardViewModel.cargarCategorias() // Cargar categorías
            metaAhorroViewModel.obtenerMetas(it) // Cargar metas
            deudaViewModel.obtenerDeudas(it) // Cargar deudas
        }
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
            topBar = { TopBar(scope, drawerState, usuarioState.currentUser?.nombre) }
            // bottomBar removed to allow floating effect
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
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
                        Spacer(Modifier.height(16.dp))
                        
                        FinancialHealthWidget(
                            ingresos = dashboardState.totalIngresos,
                            gastos = dashboardState.totalGastos,
                            selectedDate = dashboardState.selectedDate,
                            ejecucion = dashboardState.ejecucionPresupuesto,
                            onDateChange = { newDate -> dashboardViewModel.cambiarFechaFiltro(newDate) }
                        )
                        Spacer(Modifier.height(24.dp))
                        TransactionHistory(
                            historial = dashboardState.historialMovimientos,
                            onNavigateToHistory = { navController.navigate(Routes.TRANSACTION_HISTORY) }
                        )
                        Spacer(Modifier.height(24.dp))
                        SavingsSummaryWidget(
                             totalAhorrado = metaAhorroState.totalAhorrado,
                             goalCount = metaAhorroState.metas.size,
                             onClick = { navController.navigate(Routes.GOALS) }
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        val totalDeuda = deudaState.deudas.sumOf { it.montoRestante }
                        DebtSummaryWidget(
                            totalDeuda = totalDeuda,
                            debtCount = deudaState.deudas.count { it.montoRestante > 0 },
                            onClick = { navController.navigate(Routes.DEBTS) }
                        )
                        // Espacio extra para que el contenido no quede oculto por la barra flotante
                        Spacer(Modifier.height(100.dp))
                    }
                }
                
                // Barra de navegación flotante
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    SoftUiBottomNav(navController = navController)
                }
            }
        }
    }

    // --- Diálogos ---
    if (showAddTransactionDialog) {
        AddTransactionDialog(
            tipo = tipoMovimientoDialog,
            categorias = dashboardState.categorias, // Pasar categorías
            isBudgetConfigured = dashboardState.isBudgetConfigured, 
            onDismiss = { showAddTransactionDialog = false },
            onConfirm = { amount, desc, catId, applyBudget ->
                usuarioState.currentUser?.rut?.let { rut -> 
                    dashboardViewModel.registrarMovimiento(rut, amount, desc, tipoMovimientoDialog, catId, applyBudget) 
                }
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
            NavigationDrawerItem(icon = { Icon(Icons.Default.PieChart, null) }, label = { Text("Planificación Financiera") }, selected = false, onClick = { navController.navigate(Routes.PLANNING); onCloseDrawer() }, shape = RoundedCornerShape(12.dp))
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
fun BalanceCard(saldoActual: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(32.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(
                    "Saldo Actual", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatToCLP(saldoActual, false), 
                    style = MaterialTheme.typography.displayLarge, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ActionButtons(onIngresoClick: () -> Unit, onGastoClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(
            onClick = onIngresoClick, 
            modifier = Modifier.weight(1f).height(56.dp), 
            shape = RoundedCornerShape(16.dp), 
            colors = ButtonDefaults.buttonColors(containerColor = MintGreen, contentColor = Color.White), // Updated to MintGreen
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Ingreso", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ingreso", fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = onGastoClick, 
            modifier = Modifier.weight(1f).height(56.dp), 
            shape = RoundedCornerShape(16.dp), 
            colors = ButtonDefaults.buttonColors(containerColor = SoftCoral, contentColor = Color.White), // Updated to SoftCoral
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Gasto", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gasto", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun TransactionHistory(historial: List<MovimientoResponseDTO>, onNavigateToHistory: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // Updated to White
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp) // Increased elevation
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToHistory)
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Últimos Movimientos", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold, 
                    color = DarkGrayText
                )
                // Arrow with circle background and depth
                Surface(
                    shape = CircleShape,
                    color = SoftWhite,
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        Icons.Default.ChevronRight, 
                        contentDescription = "Ver historial completo", 
                        tint = DarkGrayText.copy(alpha = 0.7f),
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
            
            if (historial.isEmpty()) {
                Text(
                    "Aún no tienes movimientos.", 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MediumGrayText, 
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    historial.take(3).forEach { movimiento -> TransactionItem(item = movimiento) }
                }
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

    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(18.dp), 
        colors = CardDefaults.cardColors(containerColor = Color.White), // White background
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Soft depth
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.descripcion, fontWeight = FontWeight.SemiBold, color = DarkGrayText)
                Spacer(Modifier.height(4.dp))
                // Muestra la categoría si está disponible
                item.categoria?.let {
                    Text(
                        text = it.nombre,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(text = dateFormat.format(item.fecha), style = MaterialTheme.typography.bodySmall, color = MediumGrayText)
            }
            Text(text = formattedAmount, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = amountColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    tipo: TipoMovimiento, 
    categorias: List<CategoriaDTO>,
    isBudgetConfigured: Boolean,
    onDismiss: () -> Unit, 
    onConfirm: (Double, String, Long?, Boolean) -> Unit
) {
    var monto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CategoriaDTO?>(null) }
    var aplicarPresupuesto by remember { mutableStateOf(false) }
    
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = { Text(if (tipo == TipoMovimiento.INGRESO_GENERAL) "Añadir Ingreso" else "Añadir Gasto") },
        text = {
            Column {
                OutlinedTextField(
                    value = monto, 
                    onValueChange = { monto = it }, 
                    label = { Text("Monto") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = descripcion, 
                    onValueChange = { descripcion = it }, 
                    label = { Text("Descripción") }, 
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Categoría", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Lógica de Filtrado y Ordenamiento
                val filteredCategories = remember(categorias, tipo) {
                    if (tipo == com.example.saveuplite.model.enums.TipoMovimiento.INGRESO_GENERAL) {
                        // Para Ingresos: Solo mostrar Sueldo, Depósito, Otros. Excluir Necesidades/Deseos explícitos y AHORRO.
                        categorias.filter { cat -> 
                             val name = cat.nombre.trim().lowercase()
                             // NO mostrar AHORRO en ingresos manuales
                             val isAhorro = cat.tipoPresupuesto == com.example.saveuplite.model.enums.TipoPresupuesto.AHORRO
                             
                             !isAhorro && (name == "sueldo" || name == "depósito" || name == "deposito" || name == "otros" || name == "otro" ||
                             (cat.tipoPresupuesto != com.example.saveuplite.model.enums.TipoPresupuesto.NECESIDAD && 
                              cat.tipoPresupuesto != com.example.saveuplite.model.enums.TipoPresupuesto.DESEO))
                        }.sortedBy { it.nombre }
                    } else {
                        // Para Gastos: Excluir Sueldo, Deudas y AHORRO. Ordenar por Necesidad -> Deseo -> Otros
                        categorias.filter { cat ->
                             val name = cat.nombre.trim().lowercase()
                             val isAhorro = cat.tipoPresupuesto == com.example.saveuplite.model.enums.TipoPresupuesto.AHORRO
                             !isAhorro && name != "sueldo" && name != "depósito" && name != "deposito" && name != "deudas"
                        }.sortedWith(compareBy<CategoriaDTO> { 
                            when(it.tipoPresupuesto) {
                                com.example.saveuplite.model.enums.TipoPresupuesto.NECESIDAD -> 1
                                com.example.saveuplite.model.enums.TipoPresupuesto.DESEO -> 2
                                else -> 3
                            }
                        }.thenBy { it.nombre })
                    }
                }

                CategorySelector(
                    categories = filteredCategories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
                
                // Simbología (Solo para Gastos)
                if (tipo != com.example.saveuplite.model.enums.TipoMovimiento.INGRESO_GENERAL) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Necesidad
                        Box(modifier = Modifier.size(6.dp).background(com.example.saveuplite.ui.theme.DarkTeal, androidx.compose.foundation.shape.CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Necesidad (50%)", style = MaterialTheme.typography.labelSmall, color = com.example.saveuplite.ui.theme.MediumGrayText)
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Deseo
                        Box(modifier = Modifier.size(6.dp).background(com.example.saveuplite.ui.theme.SaturatedSalmon, androidx.compose.foundation.shape.CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Deseo (30%)", style = MaterialTheme.typography.labelSmall, color = com.example.saveuplite.ui.theme.MediumGrayText)
                    }
                }
                
                if (tipo == TipoMovimiento.INGRESO_GENERAL && isBudgetConfigured) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Aplicar Presupuesto (Set & Forget)", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = aplicarPresupuesto,
                            onCheckedChange = { aplicarPresupuesto = it }
                        )
                    }
                }
            }
        },
        confirmButton = { 
            Button(
                onClick = { 
                    onConfirm(
                        monto.toDoubleOrNull() ?: 0.0, 
                        descripcion,
                        selectedCategory?.id,
                        aplicarPresupuesto
                    ) 
                }, 
                shape = RoundedCornerShape(12.dp)
            ) { Text("Guardar") } 
        },
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

@Composable
fun SavingsSummaryWidget(totalAhorrado: Double, goalCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PaleTeal),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Star, // Using Star icon for goals
                    contentDescription = null,
                    tint = DarkGrayText,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Mis Metas de Ahorro",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkGrayText
                )
                Text(
                    text = "${formatToCLP(totalAhorrado, false)} en $goalCount metas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkGrayText.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.5f)
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = DarkGrayText,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun DebtSummaryWidget(totalDeuda: Double, debtCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PaleSalmon),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MoneyOff, 
                    contentDescription = null,
                    tint = DarkGrayText,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Resumen de Deudas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkGrayText
                )
                Text(
                    text = "${formatToCLP(totalDeuda, false)} por pagar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkGrayText.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.5f)
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = DarkGrayText,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun FinancialHealthWidget(
    ingresos: Double,
    gastos: Double,
    selectedDate: java.time.LocalDate,
    ejecucion: com.example.saveuplite.model.dto.EjecucionPresupuestoDTO? = null,
    onDateChange: (java.time.LocalDate) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Evitar división por cero
    val progress = if (ingresos > 0) (gastos / ingresos).toFloat().coerceIn(0f, 1f) else 0f
    
    // Determinar color basado en el porcentaje de gasto
    val healthColor = when {
        progress < 0.5f -> PaleAqua // Saludable (Verde suave)
        progress < 0.8f -> Color(0xFFFFE0B2) // Precaución (Naranja suave)
        else -> PalePink // Crítico (Rojo suave)
    }

    Card(
        modifier = Modifier.fillMaxWidth()
            .animateContentSize(), // Smooth animation for expansion
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Salud Financiera",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkGrayText
                    )
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.padding(start = 8.dp).size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.CalendarToday,
                            contentDescription = "Expandir detalle",
                            tint = LavenderBlue
                        )
                    }
                }
                
                Text(
                    text = "${(progress * 100).toInt()}% Gastado",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if(progress > 0.8f) DangerRed else MediumGrayText
                )
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                com.example.saveuplite.ui.components.MonthYearSelector(
                    currentDate = selectedDate,
                    onDateChange = onDateChange,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // --- Sección de Ejecución Presupuestaria ---
                if (ejecucion != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Ejecución Presupuestaria",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkGrayText
                        )
                        IconButton(
                            onClick = { showInfoDialog = true },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Información",
                                tint = LavenderBlue
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    BudgetProgressBar(
                        label = "Necesidades (${ejecucion.porcentajeNecesidadesConfigurado.toInt()}%)",
                        spent = ejecucion.gastoNecesidades,
                        budget = ejecucion.presupuestoNecesidades,
                        color = PaleAqua
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BudgetProgressBar(
                        label = "Deseos (${ejecucion.porcentajeDeseosConfigurado.toInt()}%)",
                        spent = ejecucion.gastoDeseos,
                        budget = ejecucion.presupuestoDeseos,
                        color = PaleSalmon
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Barra de progreso estilo DeudasScreen
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)), // Rounded corners for consistency
                color = SaturatedSalmon, // Updated to match DeudasScreen progress color
                trackColor = LightGray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Ingresos", style = MaterialTheme.typography.labelSmall, color = MediumGrayText)
                    Text(formatToCLP(ingresos, false), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DarkTeal) // Changed to DarkTeal
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Gastos", style = MaterialTheme.typography.labelSmall, color = MediumGrayText)
                    Text(formatToCLP(gastos, false), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DangerRed)
                }
            }
        }
    }
    
    if (showInfoDialog) {
        BudgetExplanationDialog(onDismiss = { showInfoDialog = false })
    }
}

@Composable
fun BudgetExplanationDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = LavenderBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("¿Cómo funciona?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Tu presupuesto se adapta automáticamente a tus ingresos mensuales para ayudarte a no gastar de más. Por ejemplo, usando la regla 50/30/20:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Divider(color = LightGray)
                Row(verticalAlignment = Alignment.Top) {
                    Box(modifier = Modifier.padding(top = 4.dp).size(12.dp).background(PaleAqua, CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Necesidades (50%)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Gastos indispensables: Casa, Supermercado, Transporte, Cuentas.", style = MaterialTheme.typography.bodySmall, color = MediumGrayText)
                    }
                }

                Row(verticalAlignment = Alignment.Top) {
                    Box(modifier = Modifier.padding(top = 4.dp).size(12.dp).background(PaleSalmon, CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Deseos (30%)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Gustos personales: Salidas, Cine, Ropa, Compras extra.", style = MaterialTheme.typography.bodySmall, color = MediumGrayText)
                    }
                }
                
                Text(
                    "💡 Mantén tus barras 'verdes' para asegurar tu capacidad de Ahorro (20%).",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkTeal,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = LavenderBlue)
            ) { 
                Text("¡Entendido!", fontWeight = FontWeight.Bold) 
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun BudgetProgressBar(label: String, spent: Double, budget: Double, color: Color) {
    val progress = if (budget > 0) (spent / budget).toFloat() else 0f // Allow > 1f
    val displayProgress = progress.coerceIn(0f, 1f)
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = DarkGrayText)
            Text(
                "${(progress * 100).toInt()}% (${formatToCLP(spent, false)} / ${formatToCLP(budget, false)})",
                style = MaterialTheme.typography.bodySmall, 
                fontWeight = FontWeight.Bold,
                color = if (progress > 1f) DangerRed else MediumGrayText
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { displayProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (progress > 1f) DangerRed else color,
            trackColor = LightGray
        )
    }
}
