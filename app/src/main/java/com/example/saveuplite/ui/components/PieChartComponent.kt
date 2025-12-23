package com.example.saveuplite.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saveuplite.model.dto.CategoriaDTO
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape

@Composable
fun PieChartComponent(
    data: Map<String, Double>, // Category Name -> Amount
    colors: List<Color> = listOf(
        Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6), 
        Color(0xFFFFD54F), Color(0xFFBA68C8), Color(0xFF4DB6AC)
    )
) {
    var animationPlayed by remember { mutableStateOf(false) }
    
    val totalAmount = data.values.sum()
    val floatValue = data.values.map { 
        if (totalAmount > 0) 360f * (it.toFloat() / totalAmount.toFloat()) else 0f 
    }
    
    val lastValue = animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "PieChartAnimation"
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                var startAngle = -90f
                data.values.forEachIndexed { index, value ->
                    val sweepAngle = if (totalAmount > 0) 
                        360f * (value.toFloat() / totalAmount.toFloat()) 
                    else 0f
                    
                    val color = colors.getOrElse(index) { Color.Gray }
                    
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle * lastValue.value,
                        useCenter = false,
                        style = Stroke(width = 40f)
                    )
                    startAngle += sweepAngle
                }
            }
            Text(
                text = "Total\n$${totalAmount.toInt()}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Legend
        data.keys.forEachIndexed { index, name ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colors.getOrElse(index) { Color.Gray }, shape = CircleShape)
                )
                Text(
                    text = name,
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$${data[name]?.toInt()}", // Format using user's locale ideally
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
