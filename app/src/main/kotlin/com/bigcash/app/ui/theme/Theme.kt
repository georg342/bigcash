package com.bigcash.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bigcash.core.model.ThemeMode

/** Liquid Glass palette: mint, periwinkle and pink light refracted through dark glass. */
object GlassColors {
    val Mint = Color(0xFF7CF7D4)
    val Periwinkle = Color(0xFF9CB8FF)
    val Pink = Color(0xFFFFA7D1)
    val Amber = Color(0xFFFFD48A)
    val Ink = Color(0xFF07070C)
    val DeepInk = Color(0xFF0B1021)
    val Mist = Color(0xFF12172B)
    val GlassLight = Color(0xFFFFFFFF)
    val GlassBorder = Color(0x33FFFFFF)
    val GlassBorderStrong = Color(0x66FFFFFF)
    val TextPrimaryDark = Color(0xFFF4F6FF)
    val TextSecondaryDark = Color(0xFFA8B0C9)
    val TextPrimaryLight = Color(0xFF101426)
    val TextSecondaryLight = Color(0xFF4A5170)
}

/** Extra tokens that Material 3 does not model, shared through a composition local. */
data class GlassTokens(
    val surfaceTint: Color,
    val borderColor: Color,
    val backgroundBrush: Brush,
    val accentGradient: Brush,
    val isDark: Boolean,
)

val LocalGlassTokens = staticCompositionLocalOf {
    GlassTokens(
        surfaceTint = GlassColors.GlassLight,
        borderColor = GlassColors.GlassBorder,
        backgroundBrush = Brush.verticalGradient(listOf(GlassColors.Ink, GlassColors.DeepInk)),
        accentGradient = Brush.linearGradient(listOf(GlassColors.Mint, GlassColors.Periwinkle)),
        isDark = true,
    )
}

private val DarkScheme = darkColorScheme(
    primary = GlassColors.Mint,
    onPrimary = GlassColors.Ink,
    secondary = GlassColors.Periwinkle,
    onSecondary = GlassColors.Ink,
    tertiary = GlassColors.Pink,
    background = GlassColors.Ink,
    onBackground = GlassColors.TextPrimaryDark,
    surface = GlassColors.Mist,
    onSurface = GlassColors.TextPrimaryDark,
    surfaceVariant = Color(0xFF1B2138),
    onSurfaceVariant = GlassColors.TextSecondaryDark,
    outline = GlassColors.GlassBorderStrong,
    error = Color(0xFFFF8A9B),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF1F8F79),
    onPrimary = Color.White,
    secondary = Color(0xFF4C63B6),
    onSecondary = Color.White,
    tertiary = Color(0xFFB84F86),
    background = Color(0xFFF3F5FB),
    onBackground = GlassColors.TextPrimaryLight,
    surface = Color(0xFFFFFFFF),
    onSurface = GlassColors.TextPrimaryLight,
    surfaceVariant = Color(0xFFE6EAF6),
    onSurfaceVariant = GlassColors.TextSecondaryLight,
    outline = Color(0x33000000),
    error = Color(0xFFB3261E),
)

private val BigCashTypography = Typography(
    displaySmall = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.2.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp),
)

@Composable
fun BigCashTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val tokens = if (dark) {
        GlassTokens(
            surfaceTint = GlassColors.GlassLight,
            borderColor = GlassColors.GlassBorder,
            backgroundBrush = Brush.verticalGradient(
                listOf(Color(0xFF05060B), Color(0xFF0A0F1E), Color(0xFF070A14)),
            ),
            accentGradient = Brush.linearGradient(listOf(GlassColors.Mint, GlassColors.Periwinkle)),
            isDark = true,
        )
    } else {
        GlassTokens(
            surfaceTint = Color.White,
            borderColor = Color(0x66FFFFFF),
            backgroundBrush = Brush.verticalGradient(
                listOf(Color(0xFFEDF1FA), Color(0xFFF7F3FA), Color(0xFFEAF6F3)),
            ),
            accentGradient = Brush.linearGradient(listOf(Color(0xFF1F8F79), Color(0xFF4C63B6))),
            isDark = false,
        )
    }

    CompositionLocalProvider(LocalGlassTokens provides tokens) {
        MaterialTheme(
            colorScheme = if (dark) DarkScheme else LightScheme,
            typography = BigCashTypography,
            content = content,
        )
    }
}
