// G:/Downloads MOZILLA/saveupLITE_V4R/saveupLITE/app/src/main/java/com/example/saveuplite/ui/screens/nativeView/LocationScreen.kt

package com.example.saveuplite.ui.screens.nativeView

import android.Manifest
import android.content.Intent // 1. Importar Intent
import android.content.pm.PackageManager
import android.net.Uri // 2. Importar Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.viewmodel.LocationViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
fun LocationScreen(
    viewModel: LocationViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    var hasPermission by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    // Permiso de ubicación
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            Log.d("LocationScreen", "Permission granted: $granted")
            hasPermission = granted
        }
    )

    androidx.compose.runtime.LaunchedEffect(Unit) {
        val permissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        Log.d("LocationScreen", "Initial permission check: $permissionGranted")
        hasPermission = permissionGranted
        if (!hasPermission) {
            Log.d("LocationScreen", "Requesting location permission")
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val locationState by viewModel.location.collectAsState()

    // Fondo degradado que utiliza los colores del tema
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.background
        )
    )

    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text("Ubicación Actual") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
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
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                text = "Obtén tus coordenadas y visualízalas en el mapa.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))

            // Muestra las coordenadas si están disponibles
            if (locationState != null) {
                androidx.compose.material3.Text(
                    text = "Latitud: ${locationState?.latitude}\nLongitud: ${locationState?.longitude}",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                androidx.compose.material3.Text(
                    text = "Presiona 'Obtener ubicación' para empezar.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))

            // Botón para obtener la ubicación
            androidx.compose.material3.Button(onClick = {
                Log.d("LocationScreen", "Get Location button clicked, hasPermission: $hasPermission")
                if (hasPermission) {
                    viewModel.getLocation(context)
                } else {
                    Log.d("LocationScreen", "Permission not granted, requesting again.")
                    launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }) {
                androidx.compose.material3.Text("Obtener Ubicación")
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

            // 3. Botón para abrir el mapa (solo se puede hacer clic si tenemos una ubicación)
            androidx.compose.material3.Button(
                onClick = {
                    locationState?.let { loc ->
                        // Crea un URI geo con las coordenadas y un marcador
                        val gmmIntentUri = Uri.parse("geo:${loc.latitude},${loc.longitude}?q=${loc.latitude},${loc.longitude}(Mi Ubicación)")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        // Indica a Android que busque una app de mapas
                        mapIntent.setPackage("com.google.android.apps.maps")
                        // Inicia la actividad (abre la app de mapas)
                        context.startActivity(mapIntent)
                    }
                },
                enabled = locationState != null // El botón solo se activa si hay una ubicación
            ) {
                androidx.compose.material3.Text("Abrir en el mapa")
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))

            // Botón para volver al Home
            androidx.compose.material3.OutlinedButton(onClick = { navController.navigate(Routes.HOME) }) {
                androidx.compose.material3.Text("Volver al Home")
            }
        }
    }
}
