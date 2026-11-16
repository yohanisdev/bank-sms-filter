package com.example.hermodit.ui.theme

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

private val StitchDarkColorScheme = darkColorScheme(
    primary = Color(0xFF93CCFF),
    onPrimary = Color(0xFF003353),
    primaryContainer = Color(0xFF004B73),
    onPrimaryContainer = Color(0xFFCCE5FF),
    secondary = Color(0xFF68DBA9),
    onSecondary = Color(0xFF003824),
    secondaryContainer = Color(0xFF005137),
    onSecondaryContainer = Color(0xFF85F8C4),
    tertiary = Color(0xFFFFB3B6),
    onTertiary = Color(0xFF680018),
    background = Color(0xFF131B2E),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF131B2E),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF283044),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF707881),
    outlineVariant = Color(0xFF3F4850)
)

private val StitchLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF007BB9),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6F8F0),
    onSecondaryContainer = Color(0xFF00714E),
    tertiary = Color(0xFFDC2626),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEE2E2),
    onTertiaryContainer = Color(0xFF991B1B),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF8FAFC),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun HermoditTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> StitchDarkColorScheme
        else -> StitchLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
