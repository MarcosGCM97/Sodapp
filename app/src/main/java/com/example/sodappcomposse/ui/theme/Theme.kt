package com.example.sodappcomposse.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AzulMedio,        // AzulMedio como primario en oscuro
    onPrimary = Color.White,    // Texto blanco sobre AzulMedio

    secondary = AzulClaro,      // AzulClaro como secundario
    onSecondary = Color.Black,  // Texto negro sobre AzulClaro (podría necesitar ajuste/prueba)
    // Alternativa: Color.White si AzulClaro no es tan claro

    tertiary = Celeste,         // Celeste como terciario
    onTertiary = AzulOscuro,    // Texto AzulOscuro sobre Celeste para mantener la paleta y contraste

    background = Color(20, 25, 30),      // Un fondo muy oscuro, ligeramente azulado/grisáceo
    onBackground = Color(230, 230, 230), // Texto casi blanco sobre el fondo oscuro

    surface = Color(30, 38, 45),         // Superficies ligeramente más claras que el fondo
    onSurface = Color(220, 220, 220),    // Texto sobre superficies

    error = Color(0xFFCF6679), // Rojo de error estándar para tema oscuro
    onError = Color.Black
    // Puedes definir primaryContainer, secondaryContainer, etc., si los usas
    // Ej: primaryContainer = AzulOscuro, onPrimaryContainer = Celeste
)

private val LightColorScheme = lightColorScheme(
    primary = AzulOscuro,       // AzulOscuro como primario en claro
    onPrimary = Color.White,    // Texto blanco sobre AzulOscuro

    secondary = AzulMedio,      // AzulMedio como secundario
    onSecondary = Color.White,  // Texto blanco sobre AzulMedio

    tertiary = AzulClaro,       // AzulClaro como terciario
    onTertiary = Color.Black,   // Texto negro sobre AzulClaro

    background = Celeste,       // Celeste como fondo principal de la app en tema claro
    onBackground = AzulOscuro,  // Texto AzulOscuro sobre fondo Celeste

    surface = Color(245, 250, 248),      // Una superficie muy clara, casi blanca pero con un tinte de Celeste
    onSurface = AzulOscuro,     // Texto AzulOscuro sobre la superficie clara

    error = Color(0xFFB00020), // Rojo de error estándar para tema claro
    onError = Color.White
    // Ej: primaryContainer = Celeste, onPrimaryContainer = AzulOscuro
)

@Composable
fun SodAppComposseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> LightColorScheme
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}