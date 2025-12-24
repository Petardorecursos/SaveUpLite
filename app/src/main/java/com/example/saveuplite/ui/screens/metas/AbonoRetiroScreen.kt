package com.example.saveuplite.ui.screens.metas

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.saveuplite.ui.navigation.Routes
import com.example.saveuplite.viewmodel.MetaAhorroViewModel
import com.example.saveuplite.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbonoRetiroScreen(
    navController: NavController,
    metaId: Long,
    tipo: String, // "abono" o "retiro"
    usuarioViewModel: UsuarioViewModel,
    metaAhorroViewModel: MetaAhorroViewModel
) {
    var monto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    val context = LocalContext.current

    val usuarioState by usuarioViewModel.uiState.collectAsState()
    val metaAhorroState by metaAhorroViewModel.uiState.collectAsState()
    val isAbono = tipo == "abono"
    val title = if (isAbono) "Abonar a Meta" else "Retirar de Meta"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it },
                label = { Text("Monto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción (Opcional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    val montoDouble = monto.toDoubleOrNull()
                    if (montoDouble != null && montoDouble > 0) {
                        usuarioState.currentUser?.rut?.let { rut ->
                            val desc = descripcion.ifBlank { if (isAbono) "Abono a meta" else "Retiro de meta" }
                            
                            val onSuccessAction = {
                                navController.navigate(Routes.GOALS) {
                                    popUpTo(Routes.GOALS) { inclusive = true }
                                }
                                val message = if(isAbono) "Abono realizado con éxito" else "Retiro realizado con éxito"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }

                            if (isAbono) {
                                metaAhorroViewModel.realizarAbono(rut, metaId, montoDouble, desc, onSuccessAction)
                            } else {
                                metaAhorroViewModel.realizarRetiro(rut, metaId, montoDouble, desc, onSuccessAction)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = monto.toDoubleOrNull()?.let { it > 0 } ?: false && !metaAhorroState.isLoading
            ) {
                if (metaAhorroState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (isAbono) "Abonar" else "Retirar")
                }
            }
        }
    }
}
