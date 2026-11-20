package com.safelink.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Teal = Color(0xFF0E6B63)
private val TealDark = Color(0xFF8ED4CB)

private val Light = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7EBE4),
    onPrimaryContainer = Color(0xFF00201D),
    secondary = Color(0xFF3E5C57),
    background = Color(0xFFF3F7F6),
    surface = Color.White,
    onBackground = Color(0xFF14201F),
    onSurface = Color(0xFF14201F),
    error = Color(0xFF9B1028),
)

private val Dark = darkColorScheme(
    primary = TealDark,
    onPrimary = Color(0xFF003732),
    primaryContainer = Teal,
    onPrimaryContainer = Color(0xFFB7EBE4),
    background = Color(0xFF0E1716),
    surface = Color(0xFF172221),
    onBackground = Color(0xFFE4EFED),
    onSurface = Color(0xFFE4EFED),
    error = Color(0xFFFFB3B8),
)

@Composable
fun SafeLinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
