package com.example.saveuplite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.saveuplite.ui.navigation.AppNavHost
import com.example.saveuplite.ui.theme.SaveupLITETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SaveupLITETheme {
                // Creamos el controlador de navegación
                val navController = rememberNavController()
                // Cargamos el NavHost con las pantallas
                // God
                AppNavHost(navController = navController)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SaveupLITETheme {
        // Vista previa limitada: solo muestra el HomeScreen
        // Nota: NavController no funciona en Preview
        // Puedes previsualizar HomeScreen directamente si quieres
    }
}
