package com.example.saveuplite.ui.screens.converter

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saveuplite.ui.theme.LavenderBlue
import com.example.saveuplite.ui.theme.SoftWhite
import com.example.saveuplite.viewmodel.ConverterViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(navController: NavController) {
    val viewModel: ConverterViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Conversor de Moneda", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (uiState.isLoadingCurrencies) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Campo de Monto
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = { viewModel.onAmountChange(it) },
                    label = { Text("Monto a convertir") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(20.dp))

                // Dropdowns de Monedas
                CurrencyDropdown(label = "De", selectedCurrency = uiState.fromCurrency, currencies = uiState.currencies) { viewModel.onFromCurrencyChange(it) }
                Spacer(Modifier.height(12.dp))
                IconButton(onClick = { viewModel.swapCurrencies() }) { Icon(Icons.Default.SwapVert, contentDescription = "Intercambiar monedas") }
                Spacer(Modifier.height(12.dp))
                CurrencyDropdown(label = "A", selectedCurrency = uiState.toCurrency, currencies = uiState.currencies) { viewModel.onToCurrencyChange(it) }
                Spacer(Modifier.height(24.dp))

                // Área de Resultado con animación y estado de carga
                AnimatedContent(
                    targetState = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { isLoading ->
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(vertical = 40.dp))
                    } else if (uiState.conversionResult != null) {
                        ResultCard(uiState.conversionResult, uiState.toCurrency)
                    }
                }

                 if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(result: Double?, toCurrency: String) {
    if (result == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LavenderBlue) // ¡SOLUCIÓN! Color de fondo actualizado
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Resultado", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatCurrency(result, toCurrency),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    label: String,
    selectedCurrency: String,
    currencies: Map<String, String>,
    onCurrencySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currencyDisplayName = currencies[selectedCurrency] ?: selectedCurrency

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = "$currencyDisplayName ($selectedCurrency)",
            onValueChange = {}, // No se puede cambiar directamente
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = SoftWhite, focusedContainerColor = SoftWhite)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 250.dp)
        ) {
            currencies.keys.sorted().forEach { currencyCode ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = "${currencies[currencyCode]} ($currencyCode)",
                            color = if (currencyCode == selectedCurrency) LavenderBlue else Color.Unspecified,
                            fontWeight = if (currencyCode == selectedCurrency) FontWeight.Bold else FontWeight.Normal
                        )
                     },
                    onClick = {
                        onCurrencySelected(currencyCode)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double, currencyCode: String): String {
    return try {
        val format = NumberFormat.getCurrencyInstance().apply {
            maximumFractionDigits = 2
            currency = java.util.Currency.getInstance(currencyCode)
        }
        format.format(amount)
    } catch (e: Exception) {
        // Fallback para códigos de moneda no estándar (como criptomonedas)
        "$amount $currencyCode"
    }
}
