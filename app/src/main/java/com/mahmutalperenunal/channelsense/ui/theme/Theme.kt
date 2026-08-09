package com.mahmutalperenunal.channelsense.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Teal90,
    onPrimary = DeepTeal10,
    primaryContainer = MistTeal20,
    onPrimaryContainer = MistTeal90,
    secondary = Slate80,
    onSecondary = Slate20,
    secondaryContainer = Slate20,
    onSecondaryContainer = Slate90,
    tertiary = Amber80,
    onTertiary = Amber20,
    tertiaryContainer = Amber20,
    onTertiaryContainer = Amber90,
    background = Night6,
    onBackground = NightText90,
    surface = Night10,
    onSurface = NightText90,
    surfaceVariant = Night18,
    onSurfaceVariant = NightText70,
    surfaceContainerLowest = Night6,
    surfaceContainerLow = Night10,
    surfaceContainer = Night14,
    surfaceContainerHigh = Night18,
    surfaceContainerHighest = Color(0xFF273132),
    outline = Color(0xFF899394),
    outlineVariant = Color(0xFF3E494A),
    error = Error80,
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    onPrimary = Color.White,
    primaryContainer = MistTeal90,
    onPrimaryContainer = DeepTeal10,
    secondary = Slate40,
    onSecondary = Color.White,
    secondaryContainer = Slate90,
    onSecondaryContainer = Slate20,
    tertiary = Amber40,
    onTertiary = Color.White,
    tertiaryContainer = Amber90,
    onTertiaryContainer = Amber20,
    background = Cloud98,
    onBackground = Ink10,
    surface = Color.White,
    onSurface = Ink10,
    surfaceVariant = Cloud94,
    onSurfaceVariant = Ink30,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Cloud98,
    surfaceContainer = Cloud96,
    surfaceContainerHigh = Cloud94,
    surfaceContainerHighest = Color(0xFFE2E9E8),
    outline = Outline50,
    outlineVariant = Outline80,
    error = Error40,
    onError = Color.White
)

@Composable
fun ChannelSenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ChannelSenseShapes,
        content = content
    )
}
