package com.example.saveuplite.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Paleta oscura (Dark Theme) - Opcional, pero bueno tenerla.
// Puedes ajustarla si quieres un tema oscuro "Soft UI" también.
private val DarkColorScheme = darkColorScheme(
    primary = LavenderBlue, // Púrpura/Azul principal
    secondary = PaleAqua, // Verde azulado pálido
    tertiary = PalePink, // Rosa pálido
    background = NearBlackText, // Un fondo muy oscuro, no negro puro
    surface = DarkGrayText, // Superficie ligeramente más clara que el fondo
    onPrimary = SoftWhite, // Texto/iconos sobre el color primario
    onSecondary = NearBlackText, // Texto/iconos sobre el color secundario
    onTertiary = NearBlackText, // Texto/iconos sobre el color terciario
    onBackground = SoftWhite, // Texto/iconos sobre el fondo general
    onSurface = LightGray, // Texto/iconos sobre las superficies (tarjetas, etc.)
)

// Paleta clara (Light Theme) - Estilo Soft UI
private val LightColorScheme = lightColorScheme(
    primary = LavenderBlue, // Púrpura/Azul principal para acentos
    secondary = PaleAqua, // Verde azulado pálido para elementos secundarios
    tertiary = PalePink, // Rosa pálido para otros elementos
    background = SoftWhite, // Fondo principal de la app (blanco suave)
    surface = LightGray, // Fondo de tarjetas y otros elementos elevados
    onPrimary = SoftWhite, // Texto/iconos sobre el color primario
    onSecondary = DarkGrayText, // Texto/iconos sobre el color secundario
    onTertiary = DarkGrayText, // Texto/iconos sobre el color terciario
    onBackground = DarkGrayText, // Color principal del texto sobre el fondo
    onSurface = NearBlackText, // Color del texto sobre las tarjetas/superficies
)

@Composable
fun SaveupLITETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color no es ideal para un estilo Neumorfista/Soft UI, así que lo desactivamos.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
