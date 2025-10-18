package com.trungkien.fbtp_cn.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = OnPrimary,
    primaryContainer = GreenContainer,
    onPrimaryContainer = GreenPrimary,
    secondary = OrangeSecondary,
    onSecondary = OnPrimary,
    secondaryContainer = OrangeAccent,
    onSecondaryContainer = OnPrimary,
    tertiary = BlueTertiary,
    onTertiary = OnTertiary,
    background = Color(0xFF263238),
    onBackground = OnPrimary,
    surface = Color(0xFF263238),
    onSurface = OnPrimary,
    error = Error,
    onError = OnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = OnPrimary,
    primaryContainer = GreenContainer,
    onPrimaryContainer = GreenPrimary,
    secondary = OrangeSecondary,
    onSecondary = OnPrimary,
    secondaryContainer = OrangeAccent,
    onSecondaryContainer = OnPrimary,
    tertiary = BlueTertiary,
    onTertiary = OnTertiary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    error = Error,
    onError = OnPrimary
)

@Composable
fun FBTP_CNTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Tắt dynamic color để dùng màu custom
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
