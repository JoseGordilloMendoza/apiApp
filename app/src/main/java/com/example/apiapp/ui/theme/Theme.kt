package com.example.apiapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = PortalGreenDark,
    onPrimary = OnPortalGreenDark,
    primaryContainer = PortalGreenContainerDark,
    onPrimaryContainer = OnPortalGreenContainerDark,
    secondary = CosmicPurpleDark,
    onSecondary = OnCosmicPurpleDark,
    secondaryContainer = CosmicPurpleContainerDark,
    onSecondaryContainer = OnCosmicPurpleContainerDark,
    tertiary = RickCyanDark,
    onTertiary = OnRickCyanDark,
    tertiaryContainer = RickCyanContainerDark,
    onTertiaryContainer = OnRickCyanContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = PortalGreenLight,
    onPrimary = OnPortalGreenLight,
    primaryContainer = PortalGreenContainerLight,
    onPrimaryContainer = OnPortalGreenContainerLight,
    secondary = CosmicPurpleLight,
    onSecondary = OnCosmicPurpleLight,
    secondaryContainer = CosmicPurpleContainerLight,
    onSecondaryContainer = OnCosmicPurpleContainerLight,
    tertiary = RickCyanLight,
    onTertiary = OnRickCyanLight,
    tertiaryContainer = RickCyanContainerLight,
    onTertiaryContainer = OnRickCyanContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun ApiAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // La paleta "Portal" es la identidad de la app: dynamic color queda apagado por defecto
    // para que no la tape el wallpaper del sistema. Se puede habilitar explicitamente.
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
        shapes = AppShapes,
        content = content
    )
}
