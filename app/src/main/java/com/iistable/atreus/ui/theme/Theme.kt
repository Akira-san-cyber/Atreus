package com.iistable.atreus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// The Ash Gray + Neon Green Palette
private val AtreusColorScheme = darkColorScheme(
    primary = Color(0xFF00E676),           // Neon Green Accent
    onPrimary = Color.Black,               // Text on Green
    primaryContainer = Color(0xFF003D1E),  // Darker Green for Chat Bubbles
    onPrimaryContainer = Color(0xFF00E676),
    secondary = Color(0xFF2C2C2C),         // Ash Gray for secondary elements
    background = Color(0xFF121212),        // Deep Ash Black Background
    onBackground = Color(0xFFE8E8E8),      // Off-white text
    surface = Color(0xFF1E1E1E),           // Lighter Ash Gray for cards/menus
    onSurface = Color(0xFFE8E8E8),
    surfaceVariant = Color(0xFF2C2C2C),    // Ash Gray for input fields
    onSurfaceVariant = Color(0xFFA0A0A0)
)

@Composable
fun AtreusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AtreusColorScheme,
        content = content
    )
}
