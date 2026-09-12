package com.lingubible.app.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.lingubible.app.core.settings.AppColorScheme
import com.lingubible.app.core.settings.PaletteStyle

val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant,
)

val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    inversePrimary = md_theme_dark_inversePrimary,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    surfaceTint = md_theme_dark_surfaceTint,
    inverseSurface = md_theme_dark_inverseSurface,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    outline = md_theme_dark_outline,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
    surfaceDim = md_theme_dark_surfaceDim,
    surfaceBright = md_theme_dark_surfaceBright,
    surfaceContainerLowest = md_theme_dark_surfaceContainerLowest,
    surfaceContainerLow = md_theme_dark_surfaceContainerLow,
    surfaceContainer = md_theme_dark_surfaceContainer,
    surfaceContainerHigh = md_theme_dark_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_dark_surfaceContainerHighest,
)

val LocalDarkTheme = compositionLocalOf { true }
val LocalOledBlack = compositionLocalOf { true }
val LocalAppColorScheme = compositionLocalOf { AppColorScheme.LINGNAN_RED }
val LocalPaletteStyle = compositionLocalOf { PaletteStyle.TONAL_SPOT }
val LocalInvertedColors = compositionLocalOf { false }
val LocalContrastLevel = compositionLocalOf { 0.0f }
val LocalCustomColorHex = compositionLocalOf<Long?> { null }
val LocalHighContrast = compositionLocalOf { false }

@Composable
fun isAppDarkTheme(): Boolean = LocalDarkTheme.current

@Composable
fun isOledBlackActive(): Boolean = LocalOledBlack.current && LocalDarkTheme.current

@Composable
fun currentAppColorScheme(): AppColorScheme = LocalAppColorScheme.current

@Composable
fun currentPaletteStyle(): PaletteStyle = LocalPaletteStyle.current

@Composable
fun isInvertedColorsActive(): Boolean = LocalInvertedColors.current

@Composable
fun currentContrastLevel(): Float = LocalContrastLevel.current

@Composable
fun currentCustomColorHex(): Long? = LocalCustomColorHex.current

@Composable
fun isHighContrastActive(): Boolean = LocalHighContrast.current || LocalContrastLevel.current > 0.4f

fun invertColorHue(color: Color): Color {
    val r = color.red
    val g = color.green
    val b = color.blue

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    var h = when {
        delta == 0f -> 0f
        max == r -> (((g - b) / delta) % 6f) * 60f
        max == g -> (((b - r) / delta) + 2f) * 60f
        else -> (((r - g) / delta) + 4f) * 60f
    }
    if (h < 0f) h += 360f

    val s = if (max == 0f) 0f else delta / max
    val v = max

    val newH = (h + 180f) % 360f

    val c = v * s
    val x = c * (1f - kotlin.math.abs((newH / 60f) % 2f - 1f))
    val m = v - c

    val (rPrime, gPrime, bPrime) = when ((newH / 60f).toInt()) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (rPrime + m).coerceIn(0f, 1f),
        green = (gPrime + m).coerceIn(0f, 1f),
        blue = (bPrime + m).coerceIn(0f, 1f),
        alpha = color.alpha
    )
}

fun extractColorHue(color: Color): Float {
    val r = color.red
    val g = color.green
    val b = color.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    if (delta == 0f) return 0f
    var h = when {
        max == r -> (((g - b) / delta) % 6f) * 60f
        max == g -> (((b - r) / delta) + 2f) * 60f
        else -> (((r - g) / delta) + 4f) * 60f
    }
    if (h < 0f) h += 360f
    return h
}

fun extractColorSaturation(color: Color): Float {
    val r = color.red
    val g = color.green
    val b = color.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    return if (max == 0f) 0f else delta / max
}

fun colorFromHsv(h: Float, s: Float, v: Float): Color {
    val clampedH = ((h % 360f) + 360f) % 360f
    val clampedS = s.coerceIn(0f, 1f)
    val clampedV = v.coerceIn(0f, 1f)

    val c = clampedV * clampedS
    val x = c * (1f - kotlin.math.abs((clampedH / 60f) % 2f - 1f))
    val m = clampedV - c

    val (rPrime, gPrime, bPrime) = when ((clampedH / 60f).toInt()) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (rPrime + m).coerceIn(0f, 1f),
        green = (gPrime + m).coerceIn(0f, 1f),
        blue = (bPrime + m).coerceIn(0f, 1f),
        alpha = 1f
    )
}

fun colorSchemeFor(
    palette: AppColorScheme,
    isDark: Boolean,
    isOledBlack: Boolean = false,
    isHighContrast: Boolean = false,
    paletteStyle: PaletteStyle = PaletteStyle.TONAL_SPOT,
    isInvertedColors: Boolean = false,
    contrastLevel: Float = 0.0f,
    customColorHex: Long? = null
): androidx.compose.material3.ColorScheme {
    val rawPrimary = if (customColorHex != null) {
        Color(customColorHex)
    } else if (isDark && palette.darkPrimaryHex != null) {
        Color(palette.darkPrimaryHex)
    } else {
        Color(palette.primaryHex)
    }

    val rawSecondary = if (customColorHex != null) {
        Color(customColorHex).copy(alpha = 0.85f)
    } else if (isDark && palette.darkSecondaryHex != null) {
        Color(palette.darkSecondaryHex)
    } else {
        Color(palette.secondaryHex)
    }

    val basePrimary = if (isInvertedColors) invertColorHue(rawPrimary) else rawPrimary
    val baseSecondary = if (isInvertedColors) invertColorHue(rawSecondary) else rawSecondary

    val h = extractColorHue(basePrimary)
    val isMonochrome = palette == AppColorScheme.MONOCHROME || extractColorSaturation(basePrimary) < 0.08f

    val primaryLum = basePrimary.red * 0.299f + basePrimary.green * 0.587f + basePrimary.blue * 0.114f
    val darkOnPrimary = if (primaryLum > 0.45f) colorFromHsv(h, 0.70f, 0.16f) else Color.White
    val lightOnPrimary = if (primaryLum > 0.65f) Color(0xFF1E1E1E) else Color.White

    val base = if (isDark) {
        if (isOledBlack) {
            DarkColorScheme.copy(
                primary = basePrimary,
                onPrimary = darkOnPrimary,
                primaryContainer = basePrimary.copy(alpha = 0.35f),
                onPrimaryContainer = Color.White,
                secondary = baseSecondary,
                surfaceTint = basePrimary,
                background = Color(0xFF000000),
                surface = Color(0xFF000000),
                surfaceDim = Color(0xFF000000),
                surfaceContainerLowest = Color(0xFF000000),
                surfaceContainerLow = if (isMonochrome) Color(0xFF0D0D0D) else colorFromHsv(h, 0.35f, 0.08f),
                surfaceContainer = if (isMonochrome) Color(0xFF141414) else colorFromHsv(h, 0.38f, 0.12f),
                surfaceContainerHigh = if (isMonochrome) Color(0xFF1C1C1C) else colorFromHsv(h, 0.34f, 0.16f),
                surfaceContainerHighest = if (isMonochrome) Color(0xFF242424) else colorFromHsv(h, 0.30f, 0.20f),
                surfaceVariant = if (isMonochrome) Color(0xFF181818) else colorFromHsv(h, 0.30f, 0.15f),
                onBackground = Color(0xFFFFFFFF),
                onSurface = Color(0xFFFFFFFF),
                onSurfaceVariant = if (isMonochrome) Color(0xFFAAAAAA) else colorFromHsv(h, 0.12f, 0.78f)
            )
        } else {
            if (isMonochrome) {
                DarkColorScheme.copy(
                    primary = basePrimary,
                    onPrimary = darkOnPrimary,
                    primaryContainer = basePrimary.copy(alpha = 0.35f),
                    onPrimaryContainer = Color.White,
                    secondary = baseSecondary,
                    surfaceTint = basePrimary,
                    background = Color(0xFF121212),
                    surface = Color(0xFF141414),
                    surfaceDim = Color(0xFF101010),
                    surfaceBright = Color(0xFF383838),
                    surfaceContainerLowest = Color(0xFF0D0D0D),
                    surfaceContainerLow = Color(0xFF181818),
                    surfaceContainer = Color(0xFF1E1E1E),
                    surfaceContainerHigh = Color(0xFF262626),
                    surfaceContainerHighest = Color(0xFF303030),
                    surfaceVariant = Color(0xFF282828),
                    onBackground = Color(0xFFE6E6E6),
                    onSurface = Color(0xFFE6E6E6),
                    onSurfaceVariant = Color(0xFFAAAAAA)
                )
            } else {
                DarkColorScheme.copy(
                    primary = basePrimary,
                    onPrimary = darkOnPrimary,
                    primaryContainer = basePrimary.copy(alpha = 0.35f),
                    onPrimaryContainer = Color.White,
                    secondary = baseSecondary,
                    surfaceTint = basePrimary,
                    background = colorFromHsv(h, 0.48f, 0.11f),
                    surface = colorFromHsv(h, 0.45f, 0.12f),
                    surfaceDim = colorFromHsv(h, 0.48f, 0.10f),
                    surfaceBright = colorFromHsv(h, 0.35f, 0.22f),
                    surfaceContainerLowest = colorFromHsv(h, 0.50f, 0.08f),
                    surfaceContainerLow = colorFromHsv(h, 0.42f, 0.135f),
                    surfaceContainer = colorFromHsv(h, 0.38f, 0.165f),
                    surfaceContainerHigh = colorFromHsv(h, 0.34f, 0.195f),
                    surfaceContainerHighest = colorFromHsv(h, 0.30f, 0.235f),
                    surfaceVariant = colorFromHsv(h, 0.32f, 0.20f),
                    onBackground = colorFromHsv(h, 0.06f, 0.94f),
                    onSurface = colorFromHsv(h, 0.06f, 0.94f),
                    onSurfaceVariant = colorFromHsv(h, 0.12f, 0.78f)
                )
            }
        }
    } else {
        if (isMonochrome) {
            LightColorScheme.copy(
                primary = basePrimary,
                onPrimary = lightOnPrimary,
                primaryContainer = basePrimary.copy(alpha = 0.18f),
                onPrimaryContainer = basePrimary,
                secondary = baseSecondary,
                surfaceTint = basePrimary,
                background = Color(0xFFF8FAFC),
                surface = Color(0xFFFFFFFF),
                surfaceContainerLow = Color(0xFFF1F5F9),
                surfaceContainer = Color(0xFFE2E8F0),
                surfaceContainerHigh = Color(0xFFCBD5E1),
                surfaceContainerHighest = Color(0xFF94A3B8),
                surfaceVariant = Color(0xFFE2E8F0),
                onBackground = Color(0xFF0F172A),
                onSurface = Color(0xFF0F172A),
                onSurfaceVariant = Color(0xFF475569)
            )
        } else {
            LightColorScheme.copy(
                primary = basePrimary,
                onPrimary = lightOnPrimary,
                primaryContainer = colorFromHsv(h, 0.12f, 0.96f),
                onPrimaryContainer = colorFromHsv(h, 0.85f, 0.35f),
                secondary = baseSecondary,
                surfaceTint = basePrimary,
                background = colorFromHsv(h, 0.08f, 0.985f),
                surface = Color(0xFFFFFFFF),
                surfaceDim = colorFromHsv(h, 0.10f, 0.91f),
                surfaceBright = Color(0xFFFFFFFF),
                surfaceContainerLowest = Color(0xFFFFFFFF),
                surfaceContainerLow = colorFromHsv(h, 0.08f, 0.965f),
                surfaceContainer = colorFromHsv(h, 0.12f, 0.935f),
                surfaceContainerHigh = colorFromHsv(h, 0.16f, 0.905f),
                surfaceContainerHighest = colorFromHsv(h, 0.20f, 0.865f),
                surfaceVariant = colorFromHsv(h, 0.15f, 0.90f),
                onBackground = colorFromHsv(h, 0.25f, 0.14f),
                onSurface = colorFromHsv(h, 0.25f, 0.14f),
                onSurfaceVariant = colorFromHsv(h, 0.20f, 0.38f)
            )
        }
    }

    var scheme = base

    val effectiveContrast = maxOf(contrastLevel, if (isHighContrast) 0.8f else 0.0f)
    if (effectiveContrast > 0.05f) {
        val outlineColor = if (isDark) {
            Color(0xFFE2E8F0).copy(alpha = 0.4f + 0.6f * effectiveContrast)
        } else {
            Color(0xFF0F172A).copy(alpha = 0.4f + 0.6f * effectiveContrast)
        }
        scheme = scheme.copy(
            outline = outlineColor,
            outlineVariant = outlineColor.copy(alpha = 0.6f)
        )
    }

    return scheme
}

/**
 * Smoothly animates all ColorScheme tokens during theme mode (Light <-> Dark <-> OLED)
 * and accent palette transitions adhering strictly to Material 3 Expressive spring physics.
 */
@Composable
fun animateColorScheme(
    targetColorScheme: ColorScheme,
    animationSpec: AnimationSpec<Color> = spring(stiffness = Spring.StiffnessMediumLow)
): ColorScheme {
    val primary by animateColorAsState(targetColorScheme.primary, animationSpec, label = "primary")
    val onPrimary by animateColorAsState(targetColorScheme.onPrimary, animationSpec, label = "onPrimary")
    val primaryContainer by animateColorAsState(targetColorScheme.primaryContainer, animationSpec, label = "primaryContainer")
    val onPrimaryContainer by animateColorAsState(targetColorScheme.onPrimaryContainer, animationSpec, label = "onPrimaryContainer")
    val inversePrimary by animateColorAsState(targetColorScheme.inversePrimary, animationSpec, label = "inversePrimary")
    val secondary by animateColorAsState(targetColorScheme.secondary, animationSpec, label = "secondary")
    val onSecondary by animateColorAsState(targetColorScheme.onSecondary, animationSpec, label = "onSecondary")
    val secondaryContainer by animateColorAsState(targetColorScheme.secondaryContainer, animationSpec, label = "secondaryContainer")
    val onSecondaryContainer by animateColorAsState(targetColorScheme.onSecondaryContainer, animationSpec, label = "onSecondaryContainer")
    val tertiary by animateColorAsState(targetColorScheme.tertiary, animationSpec, label = "tertiary")
    val onTertiary by animateColorAsState(targetColorScheme.onTertiary, animationSpec, label = "onTertiary")
    val tertiaryContainer by animateColorAsState(targetColorScheme.tertiaryContainer, animationSpec, label = "tertiaryContainer")
    val onTertiaryContainer by animateColorAsState(targetColorScheme.onTertiaryContainer, animationSpec, label = "onTertiaryContainer")
    val background by animateColorAsState(targetColorScheme.background, animationSpec, label = "background")
    val onBackground by animateColorAsState(targetColorScheme.onBackground, animationSpec, label = "onBackground")
    val surface by animateColorAsState(targetColorScheme.surface, animationSpec, label = "surface")
    val onSurface by animateColorAsState(targetColorScheme.onSurface, animationSpec, label = "onSurface")
    val surfaceVariant by animateColorAsState(targetColorScheme.surfaceVariant, animationSpec, label = "surfaceVariant")
    val onSurfaceVariant by animateColorAsState(targetColorScheme.onSurfaceVariant, animationSpec, label = "onSurfaceVariant")
    val surfaceTint by animateColorAsState(targetColorScheme.surfaceTint, animationSpec, label = "surfaceTint")
    val inverseSurface by animateColorAsState(targetColorScheme.inverseSurface, animationSpec, label = "inverseSurface")
    val inverseOnSurface by animateColorAsState(targetColorScheme.inverseOnSurface, animationSpec, label = "inverseOnSurface")
    val error by animateColorAsState(targetColorScheme.error, animationSpec, label = "error")
    val onError by animateColorAsState(targetColorScheme.onError, animationSpec, label = "onError")
    val errorContainer by animateColorAsState(targetColorScheme.errorContainer, animationSpec, label = "errorContainer")
    val onErrorContainer by animateColorAsState(targetColorScheme.onErrorContainer, animationSpec, label = "onErrorContainer")
    val outline by animateColorAsState(targetColorScheme.outline, animationSpec, label = "outline")
    val outlineVariant by animateColorAsState(targetColorScheme.outlineVariant, animationSpec, label = "outlineVariant")
    val scrim by animateColorAsState(targetColorScheme.scrim, animationSpec, label = "scrim")
    val surfaceBright by animateColorAsState(targetColorScheme.surfaceBright, animationSpec, label = "surfaceBright")
    val surfaceDim by animateColorAsState(targetColorScheme.surfaceDim, animationSpec, label = "surfaceDim")
    val surfaceContainer by animateColorAsState(targetColorScheme.surfaceContainer, animationSpec, label = "surfaceContainer")
    val surfaceContainerHigh by animateColorAsState(targetColorScheme.surfaceContainerHigh, animationSpec, label = "surfaceContainerHigh")
    val surfaceContainerHighest by animateColorAsState(targetColorScheme.surfaceContainerHighest, animationSpec, label = "surfaceContainerHighest")
    val surfaceContainerLow by animateColorAsState(targetColorScheme.surfaceContainerLow, animationSpec, label = "surfaceContainerLow")
    val surfaceContainerLowest by animateColorAsState(targetColorScheme.surfaceContainerLowest, animationSpec, label = "surfaceContainerLowest")

    return targetColorScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inversePrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim,
        surfaceBright = surfaceBright,
        surfaceDim = surfaceDim,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainerLowest = surfaceContainerLowest
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LingUBibleTheme(
    darkTheme: Boolean = true,
    oledBlack: Boolean = true,
    appColorScheme: AppColorScheme = AppColorScheme.LINGNAN_RED,
    paletteStyle: PaletteStyle = PaletteStyle.TONAL_SPOT,
    isInvertedColors: Boolean = false,
    contrastLevel: Float = 0.0f,
    customColorHex: Long? = null,
    dynamicColor: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val rawColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val dynamic = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            if (darkTheme && oledBlack) {
                dynamic.copy(
                    background = Color(0xFF000000),
                    surface = Color(0xFF000000),
                    surfaceDim = Color(0xFF000000),
                    surfaceContainerLowest = Color(0xFF000000),
                    surfaceContainerLow = Color(0xFF0A0A0A),
                    surfaceContainer = Color(0xFF121212),
                    surfaceContainerHigh = Color(0xFF1A1A1A),
                    surfaceContainerHighest = Color(0xFF222222)
                )
            } else dynamic
        }
        else -> colorSchemeFor(
            palette = appColorScheme,
            isDark = darkTheme,
            isOledBlack = oledBlack,
            isHighContrast = highContrast,
            paletteStyle = paletteStyle,
            isInvertedColors = isInvertedColors,
            contrastLevel = contrastLevel,
            customColorHex = customColorHex
        )
    }

    // Material 3 Expressive fluid spring animated color scheme transition
    val animatedColorScheme = animateColorScheme(rawColorScheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalOledBlack provides oledBlack,
        LocalAppColorScheme provides appColorScheme,
        LocalPaletteStyle provides paletteStyle,
        LocalInvertedColors provides isInvertedColors,
        LocalContrastLevel provides contrastLevel,
        LocalCustomColorHex provides customColorHex,
        LocalHighContrast provides (highContrast || contrastLevel > 0.4f)
    ) {
        MaterialExpressiveTheme(
            colorScheme = animatedColorScheme,
            motionScheme = MotionScheme.expressive(),
            typography = Typography,
            content = content
        )
    }
}
