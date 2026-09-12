package com.lingubible.app.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Lingnan University Brand Colors
val LingnanRed = Color(0xFFE31F26) // Lingnan Red (RGB 227, 31, 38)
val LingnanRed500 = Color(0xFFE31F26)
val LingnanRed600 = Color(0xFFDC2626) // Tailwind red-600
val LingnanRedDark = Color(0xFFB5191E)
val LingnanRedLight = Color(0xFFFEE2E2)
val LingnanRed50 = Color(0xFFFEF2F2)
val LingnanGray = Color(0xFF5D5F60)
val LingnanGrayLight = Color(0xFFBCBEC0)

// Card & Surface Colors matching web
val CardBackgroundLight = Color(0xFFF9FAFB) // #f9fafb (.course-card)
val CardBorderLight = Color(0xFFE5E7EB) // border-gray-200
val CardBackgroundDark = Color(0xFF1D1A1C) // M3 SurfaceContainerLow (harmonized with Lingnan Red base)
val CardBorderDark = Color(0xFF534341) // M3 OutlineVariant

// Golden Star Color
val StarGold = Color(0xFFFACC15) // yellow-400

// Light Theme Color Tokens
val md_theme_light_primary = LingnanRed
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFFFDAD6)
val md_theme_light_onPrimaryContainer = Color(0xFF410002)
val md_theme_light_secondary = Color(0xFF775653)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFFFDAD6)
val md_theme_light_onSecondaryContainer = Color(0xFF2C1513)
val md_theme_light_tertiary = Color(0xFF715B2E)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFEDFA6)
val md_theme_light_onTertiaryContainer = Color(0xFF261900)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFFFBFF)
val md_theme_light_onBackground = Color(0xFF201A19)
val md_theme_light_surface = Color(0xFFFFFBFF)
val md_theme_light_onSurface = Color(0xFF201A19)
val md_theme_light_surfaceVariant = Color(0xFFF5DDDA)
val md_theme_light_onSurfaceVariant = Color(0xFF534341)
val md_theme_light_outline = Color(0xFF857371)
val md_theme_light_outlineVariant = Color(0xFFD8C2BF)

// Dark Theme Color Tokens - Material 3 Expressive with Lingnan Red Brand Fidelity
val md_theme_dark_primary = Color(0xFFFF5449) // Luminous Lingnan Red calibrated for Dark Mode
val md_theme_dark_onPrimary = Color(0xFF690005) // High-contrast deep dark crimson
val md_theme_dark_primaryContainer = Color(0xFF93000A) // Deep ruby red container
val md_theme_dark_onPrimaryContainer = Color(0xFFFFDAD6) // Soft rose highlight text
val md_theme_dark_inversePrimary = LingnanRed // Lingnan Red (RGB 227, 31, 38)

val md_theme_dark_secondary = Color(0xFFFFB3AD) // Warm coral-rose secondary
val md_theme_dark_onSecondary = Color(0xFF561D1A)
val md_theme_dark_secondaryContainer = Color(0xFF73332E)
val md_theme_dark_onSecondaryContainer = Color(0xFFFFDAD6)

val md_theme_dark_tertiary = Color(0xFFFFB951) // Warm Amber Gold (StarGold / Academic Honours)
val md_theme_dark_onTertiary = Color(0xFF452B00)
val md_theme_dark_tertiaryContainer = Color(0xFF633F00)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFDDB3)

val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

// Material 3 Dark Surface System (Expressive Tonal Containers)
val md_theme_dark_background = Color(0xFF141213)
val md_theme_dark_onBackground = Color(0xFFEDE0DE)
val md_theme_dark_surface = Color(0xFF141213)
val md_theme_dark_onSurface = Color(0xFFEDE0DE)
val md_theme_dark_surfaceVariant = Color(0xFF262224)
val md_theme_dark_onSurfaceVariant = Color(0xFFD8C2BF)
val md_theme_dark_surfaceTint = Color(0xFFFF5449)
val md_theme_dark_inverseSurface = Color(0xFFEDE0DE)
val md_theme_dark_inverseOnSurface = Color(0xFF322F30)
val md_theme_dark_outline = Color(0xFFA08C8A)
val md_theme_dark_outlineVariant = Color(0xFF534341)
val md_theme_dark_scrim = Color(0xFF000000)

val md_theme_dark_surfaceDim = Color(0xFF141213)
val md_theme_dark_surfaceBright = Color(0xFF3B3739)
val md_theme_dark_surfaceContainerLowest = Color(0xFF0F0D0E)
val md_theme_dark_surfaceContainerLow = Color(0xFF1D1A1C)
val md_theme_dark_surfaceContainer = Color(0xFF211E20)
val md_theme_dark_surfaceContainerHigh = Color(0xFF2C282A)
val md_theme_dark_surfaceContainerHighest = Color(0xFF373335)

// Gradient Brushes
val LingnanRedGradient = Brush.linearGradient(
    colors = listOf(LingnanRed, LingnanRedDark)
)

val HeroTitleNeonGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFDC2626), Color(0xFF881317))
)

val GpaTextGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFDC2626), Color(0xFFEF4444), Color(0xFFF87171))
)

// Dynamic rating HSL color function matching web app
// hue = (rating / 5) * 120 (0° = red, 120° = green), sat = 95%, lightness = 30%
fun getRatingGradientColor(rating: Double): Color {
    if (rating <= 0.0) return Color(0xFF4B5563) // Slate Gray when no data
    val clamped = rating.coerceIn(0.0, 5.0)
    val hue = (clamped / 5.0 * 120.0).toFloat()
    return Color.hsl(hue = hue, saturation = 0.95f, lightness = 0.30f)
}

// Grade Badge Gradients matching web GradeBadge.tsx
object GradeColors {
    val GradeA = listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669))
    val GradeB = listOf(Color(0xFF60A5FA), Color(0xFF3B82F6), Color(0xFF2563EB))
    val GradeC = listOf(Color(0xFFFACC15), Color(0xFFEAB308), Color(0xFFD97706))
    val GradeD = listOf(Color(0xFFFB923C), Color(0xFFF97316), Color(0xFFEA580C))
    val GradeF = listOf(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFFB91C1C))
    val GradeNA = listOf(Color(0xFF9CA3AF), Color(0xFF6B7280), Color(0xFF4B5563))

    fun getGradientForGrade(grade: String): List<Color> {
        val upper = grade.trim().uppercase()
        return when {
            upper.startsWith("A") -> GradeA
            upper.startsWith("B") -> GradeB
            upper.startsWith("C") -> GradeC
            upper.startsWith("D") -> GradeD
            upper.startsWith("F") -> GradeF
            else -> GradeNA
        }
    }
}

// Category Badge Color Schemes matching web
object BadgeTheme {
    // Faculty (Blue)
    val FacultyBgLight = Color(0xFFEFF6FF)
    val FacultyTextLight = Color(0xFF1D4ED8)
    val FacultyBorderLight = Color(0xFFBFDBFE)
    val FacultyBgDark = Color(0xFF1E293B)
    val FacultyTextDark = Color(0xFF93C5FD)
    val FacultyBorderDark = Color(0xFF1D4ED8)

    // Department (Gray)
    val DepartmentBgLight = Color(0xFFF3F4F6)
    val DepartmentTextLight = Color(0xFF374151)
    val DepartmentBorderLight = Color(0xFFE5E7EB)
    val DepartmentBgDark = Color(0xFF27272A)
    val DepartmentTextDark = Color(0xFFE4E4E7)
    val DepartmentBorderDark = Color(0xFF3F3F46)

    // Teaching Language (Orange)
    val LanguageBgLight = Color(0xFFFFF7ED)
    val LanguageTextLight = Color(0xFFC2410C)
    val LanguageBorderLight = Color(0xFFFED7AA)
    val LanguageBgDark = Color(0xFF431407)
    val LanguageTextDark = Color(0xFFFDBA74)
    val LanguageBorderDark = Color(0xFF9A3412)

    // Service Learning (Purple)
    val ServiceBgLight = Color(0xFFFAF5FF)
    val ServiceTextLight = Color(0xFF7E22CE)
    val ServiceBorderLight = Color(0xFFE9D5FF)
    val ServiceBgDark = Color(0xFF3B0764)
    val ServiceTextDark = Color(0xFFD8B4FE)
    val ServiceBorderDark = Color(0xFF7E22CE)

    // Study Materials (Emerald)
    val MaterialsBgLight = Color(0xFFECFDF5)
    val MaterialsTextLight = Color(0xFF047857)
    val MaterialsBorderLight = Color(0xFFA7F3D0)
    val MaterialsBgDark = Color(0xFF064E3B)
    val MaterialsTextDark = Color(0xFF6EE7B7)
    val MaterialsBorderDark = Color(0xFF047857)

    // Term Offered (Green)
    val OfferedBgLight = Color(0xFFDCFCE7)
    val OfferedTextLight = Color(0xFF166534)
    val OfferedBorderLight = Color(0xFFBBF7D0)
    val OfferedBgDark = Color(0xFF14532D)
    val OfferedTextDark = Color(0xFF86EFAC)
    val OfferedBorderDark = Color(0xFF166534)
}
