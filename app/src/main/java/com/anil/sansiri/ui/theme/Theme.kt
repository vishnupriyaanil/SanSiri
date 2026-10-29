package com.anil.sansiri.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = DeepTealPrimary,
    onPrimary = DeepTealOnPrimary,
    primaryContainer = DeepTealContainer,
    onPrimaryContainer = DeepTealOnContainer,
    secondary = EmeraldSecondary,
    onSecondary = EmeraldOnSecondary,
    secondaryContainer = EmeraldContainer,
    onSecondaryContainer = EmeraldOnContainer,
    tertiary = SlateTertiary,
    onTertiary = SlateOnTertiary,
    tertiaryContainer = SlateTertiaryContainer,
    onTertiaryContainer = SlateOnTertiaryContainer,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceVariant = SoftCardSurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CDAD0),
    onPrimary = Color(0xFF003732),
    primaryContainer = Color(0xFF005049),
    onPrimaryContainer = Color(0xFF70F7E5),
    secondary = Color(0xFFB2CCC6),
    onSecondary = Color(0xFF1E3530),
    secondaryContainer = Color(0xFF354B46),
    onSecondaryContainer = Color(0xFFCEE8E2),
    background = Color(0xFF0F1514),
    onBackground = Color(0xFFDEE4E2),
    surface = Color(0xFF0F1514),
    onSurface = Color(0xFFDEE4E2)
)

@Composable
fun SanSiriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our Deep Teal / Emerald Material 3 Design palette
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
