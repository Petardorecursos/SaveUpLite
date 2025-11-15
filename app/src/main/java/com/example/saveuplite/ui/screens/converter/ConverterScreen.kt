package com.example.saveuplite.ui.screens.converter

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.saveuplite.ui.theme.LavenderBlue
import com.example.saveuplite.ui.theme.MediumBlue
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
                    .verticalScroll(rememberScrollState()), // <-- Scroll para toda la pantalla
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
                IconButton(onClick = { /* TODO: Implementar swap */ }) { Icon(Icons.Default.SwapVert, contentDescription = "Intercambiar monedas") }
                Spacer(Modifier.height(12.dp))
                CurrencyDropdown(label = "A", selectedCurrency = uiState.toCurrency, currencies = uiState.currencies) { viewModel.onToCurrencyChange(it) }
                Spacer(Modifier.height(24.dp))

                // Botón de Conversión
                Button(
                    onClick = { viewModel.performConversion() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderBlue)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Convertir", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Área de Resultado
                if (uiState.conversionResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MediumBlue)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Resultado", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = formatCurrency(uiState.conversionResult ?: 0.0, uiState.toCurrency),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
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

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = "${currencies[selectedCurrency] ?: selectedCurrency} ($selectedCurrency)",
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
            modifier = Modifier.heightIn(max = 250.dp) // <-- ¡SOLUCIÓN: MENÚ DESLIZABLE!
        ) {
            currencies.keys.sorted().forEach { currencyCode ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = "${currencies[currencyCode]} ($currencyCode)",
                            color = if (currencyCode == selectedCurrency) LavenderBlue else Color.Unspecified
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
    val format = NumberFormat.getCurrencyInstance().apply {
        maximumFractionDigits = 2
        try {
            currency = java.util.Currency.getInstance(currencyCode)
        } catch (e: Exception) {
            currency = java.util.Currency.getInstance(Locale.US) // Fallback
        }
    }
    return format.format(amount)
}
