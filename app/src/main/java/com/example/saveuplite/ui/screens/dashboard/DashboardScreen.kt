package com.example.saveuplite.ui.screens.dashboard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.saveuplite.model.EventoSaldo
import com.example.saveuplite.model.Saldo
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.viewmodel.SaldoViewModel
import com.example.saveuplite.viewmodel.UsuarioViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    usuarioViewModel: UsuarioViewModel = viewModel(),
    saldoViewModel: SaldoViewModel = viewModel()
) {
    val saldoState by saldoViewModel.uiState.collectAsState()
    val usuarioState by usuarioViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current

    // --- Estados para la imagen de perfil ---
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    // --- ESTADOS FALTANTES: Declara showDialog y tipoMovimiento aquí ---
    var showDialog by remember { mutableStateOf(false) }
    var tipoMovimiento by remember { mutableStateOf(EventoSaldo.INGRESO) } // Initial value can be INGRESO or GASTO
    // -------------------------------------------------------

    // --- Lanzador para la cámara ---
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            // La foto fue tomada exitosamente, la URI ya está en `profileImageUri`
            // No hacemos nada aquí, ya que el URI ya está actualizado
        }
    }

    // --- Lanzador para la galería ---
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        profileImageUri = it
    }

    // --- Gestión de permisos para la cámara ---
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File(context.externalCacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
            profileImageUri = FileProvider.getUriForFile(context, "com.example.saveuplite.fileprovider", photoFile)
            cameraLauncher.launch(profileImageUri) // Lanzar la cámara con el URI
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Gestión de permisos para la galería ---
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*") // Lanzar la galería
        } else {
            Toast.makeText(context, "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Cargar los datos del saldo cuando la pantalla se muestra por primera vez
    LaunchedEffect(usuarioState.currentUser) {
        usuarioState.currentUser?.rut?.let {
            saldoViewModel.cargarDatosSaldo(it)
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.background
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // --- Ícono / Imagen de Perfil ---
                    Box(modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { showImageSourceDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageUri != null) {
                            AsyncImage(
                                model = profileImageUri,
                                contentDescription = "Imagen de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Ícono de usuario por defecto",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Nombre del Usuario ---
                    Text(
                        text = usuarioState.currentUser?.nombre ?: "Invitado",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = usuarioState.currentUser?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Otros elementos del menú aquí (opcional)
                    // Por ejemplo, NavigationDrawerItem(label = { Text("Configuración") }, selected = false, onClick = { /*TODO*/ })

                    Spacer(modifier = Modifier.weight(1f)) // Empuja el botón de cerrar sesión hacia abajo

                    // --- Botón de Cerrar Sesión ---
                    Button(
                        onClick = {
                            scope.launch { drawerState.close() } // Cierra el drawer primero
                            usuarioViewModel.logout()
                            navController.navigate(Routes.AUTH) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cerrar Sesión", color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Dashboard") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient)
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Card del Saldo Actual ---
                BalanceCard(saldoActual = saldoState.saldoActual)

                Spacer(modifier = Modifier.height(24.dp))

                // --- Botones de Acción ---
                ActionButtons(
                    onIngresoClick = {
                        tipoMovimiento = EventoSaldo.INGRESO
                        showDialog = true
                    },
                    onGastoClick = {
                        tipoMovimiento = EventoSaldo.GASTO
                        showDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Botón para Funciones Adicionales ---
                OutlinedButton(
                    onClick = { navController.navigate(Routes.LEGACY_HOME) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Funciones Adicionales")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = "Ir a funciones adicionales")
                }


                Spacer(modifier = Modifier.height(24.dp))

                // --- Historial de Movimientos ---
                TransactionHistory(historial = saldoState.historialMovimientos)
            }
        }
    }

    // --- Diálogo para añadir Ingreso/Gasto ---
    if (showDialog) {
        AddTransactionDialog(
            tipo = tipoMovimiento,
            onDismiss = { showDialog = false },
            onConfirm = { monto ->
                usuarioState.currentUser?.rut?.let { rut ->
                    saldoViewModel.agregarMovimiento(rut, monto, tipoMovimiento)
                }
                showDialog = false
            }
        )
    }

    // --- Diálogo para elegir origen de imagen ---
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Cambiar Foto de Perfil") },
            text = {
                Column {
                    TextButton(onClick = {
                        showImageSourceDialog = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }) {
                        Text("Tomar Foto")
                    }
                    TextButton(onClick = {
                        showImageSourceDialog = false
                        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                        galleryPermissionLauncher.launch(permission)
                    }) {
                        Text("Seleccionar de Galería")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


@Composable
fun BalanceCard(saldoActual: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Usar color del tema
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Saldo Actual", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$ ${ "%.2f".format(saldoActual)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ActionButtons(onIngresoClick: () -> Unit, onGastoClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onIngresoClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary) // Usar color del tema
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ingreso")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ingreso")
        }
        Button(
            onClick = onGastoClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Usar color del tema
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Gasto")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gasto")
        }
    }
}

@Composable
fun TransactionHistory(historial: List<Saldo>) {
    Text(
        "Historial de Movimientos",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(16.dp))
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(historial) { saldo ->
            TransactionItem(item = saldo)
        }
    }
}

@Composable
fun TransactionItem(item: Saldo) {
    val color = if (item.tipoEvento == EventoSaldo.INGRESO) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.tipoEvento.name,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = dateFormat.format(item.fechaRegistro),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$ ${ "%.2f".format(item.monto)}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    tipo: EventoSaldo,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var monto by remember { mutableStateOf("") }
    val isIngreso = tipo == EventoSaldo.INGRESO

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isIngreso) "Añadir Ingreso" else "Añadir Gasto") },
        text = {
            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it }, // <-- ¡CORREGIDO AQUÍ!
                label = { Text("Monto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(monto.toFloatOrNull() ?: 0f) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
