package com.example.daylist.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AzulOscuro,
    secondary = Azul,
    tertiary = AzulClaro,
    background = Blanco,
    surface = Blanco,
    onPrimary = Blanco,
    onSecondary = TextoOscuro,
    onTertiary = TextoOscuro,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro
)

private val DarkColorScheme = darkColorScheme(
    primary = Azul,
    secondary = AzulOscuro,
    tertiary = AzulClaro,
    background = TextoOscuro,
    surface = TextoOscuro,
    onPrimary = TextoOscuro,
    onSecondary = Blanco,
    onTertiary = TextoOscuro,
    onBackground = Blanco,
    onSurface = Blanco
)

@Composable
fun DaylistTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

