package com.example.saveuplite.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.saveuplite.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenCompact(navController: NavHostController) {
    // Fondo degradado verde -> negro
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF00C853), // verde intenso
            Color(0xFF004D40), // verde oscuro
            Color.Black         // negro base
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "💸 SaveUp Lite",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tu asistente inteligente para ahorrar mejor.",
                color = Color(0xFFB2DFDB),
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón principal “Comenzar”
            Button(
                onClick = { navController.navigate(Routes.FORM) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E676),
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .width(220.dp)
                    .height(56.dp)
            ) {
                Text("Comenzar", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 👇 Nuevo botón para probar la función nativa
            OutlinedButton(
                onClick = { navController.navigate(Routes.LOCATION) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .width(220.dp)
                    .height(48.dp)
            ) {
                Text("Probar ubicación", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Versión Lite • Simple y poderosa",
                color = Color(0xFF80CBC4),
                fontSize = 14.sp
            )
        }
    }
}
