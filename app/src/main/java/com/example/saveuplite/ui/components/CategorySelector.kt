package com.example.saveuplite.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.saveuplite.model.dto.CategoriaDTO

@Composable
fun CategorySelector(
    categories: List<CategoriaDTO>,
    selectedCategory: CategoriaDTO?,
    onCategorySelected: (CategoriaDTO) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategory?.id == category.id
            
            // Determinar color según el tipo de presupuesto (Colores saturados para mejor visibilidad)
            val categoryColor = when (category.tipoPresupuesto) {
                com.example.saveuplite.model.enums.TipoPresupuesto.NECESIDAD -> com.example.saveuplite.ui.theme.DarkTeal
                com.example.saveuplite.model.enums.TipoPresupuesto.DESEO -> com.example.saveuplite.ui.theme.SaturatedSalmon
                else -> Color.Gray
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onCategorySelected(category) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Indicador de tipo
                        if (category.tipoPresupuesto == com.example.saveuplite.model.enums.TipoPresupuesto.NECESIDAD || 
                            category.tipoPresupuesto == com.example.saveuplite.model.enums.TipoPresupuesto.DESEO) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(categoryColor, androidx.compose.foundation.shape.CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        Text(
                            text = category.nombre,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Etiqueta pequeña debajo (opcional, para mayor claridad)
                if (isSelected && (category.tipoPresupuesto == com.example.saveuplite.model.enums.TipoPresupuesto.NECESIDAD || category.tipoPresupuesto == com.example.saveuplite.model.enums.TipoPresupuesto.DESEO)) {
                     Text(
                        text = if (category.tipoPresupuesto == com.example.saveuplite.model.enums.TipoPresupuesto.NECESIDAD) "Necesidad" else "Deseo",
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor,
                        modifier = Modifier.padding(top = 2.dp)
                     )
                }
            }
        }
    }
}
