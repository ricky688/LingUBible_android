package com.lingubible.app

import androidx.compose.ui.graphics.Color
import com.lingubible.app.core.theme.DarkColorScheme
import com.lingubible.app.core.theme.LightColorScheme
import com.lingubible.app.core.theme.LingnanRed
import com.lingubible.app.core.theme.LingnanRedDark
import com.lingubible.app.core.theme.LingnanRedLight
import com.lingubible.app.core.theme.Typography
import com.lingubible.app.core.theme.md_theme_light_primary
import com.lingubible.app.core.theme.md_theme_light_secondary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTest {

    @Test
    fun `verify Lingnan Red brand color definitions`() {
        assertEquals(Color(0xFFE31F26), LingnanRed)
        assertEquals(Color(0xFFB5191E), LingnanRedDark)
        assertEquals(Color(0xFFFEE2E2), LingnanRedLight)
        assertEquals(LingnanRed, md_theme_light_primary)
        assertEquals(Color(0xFF775653), md_theme_light_secondary)
    }

    @Test
    fun `verify light and dark color schemes primary tokens`() {
        assertEquals(LingnanRed, LightColorScheme.primary)
        assertEquals(Color(0xFFFF5449), DarkColorScheme.primary)
        assertEquals(Color(0xFFFFFFFF), LightColorScheme.onPrimary)
        assertEquals(Color(0xFF141213), DarkColorScheme.surface)
    }

    @Test
    fun `verify typography scales and font sizes`() {
        assertTrue(Typography.displayLarge.fontSize.value > 0f)
        assertTrue(Typography.headlineLarge.fontSize.value > 0f)
        assertTrue(Typography.titleLarge.fontSize.value > 0f)
        assertTrue(Typography.bodyLarge.fontSize.value > 0f)
        assertTrue(Typography.bodyMedium.fontSize.value > 0f)
        assertTrue(Typography.labelSmall.fontSize.value > 0f)
    }

    @Test
    fun `verify OLED black mode overrides dark surfaces to pure black`() {
        val oledDarkScheme = com.lingubible.app.core.theme.colorSchemeFor(
            palette = com.lingubible.app.core.settings.AppColorScheme.LINGNAN_RED,
            isDark = true,
            isOledBlack = true
        )
        assertEquals(Color(0xFF000000), oledDarkScheme.background)
        assertEquals(Color(0xFF000000), oledDarkScheme.surface)
        assertEquals(Color(0xFF000000), oledDarkScheme.surfaceContainerLowest)
    }

    @Test
    fun `verify accent color palettes available and generate distinct schemes`() {
        val palettes = com.lingubible.app.core.settings.AppColorScheme.entries
        assertTrue(palettes.size >= 12)

        val oceanBlue = com.lingubible.app.core.theme.colorSchemeFor(
            palette = com.lingubible.app.core.settings.AppColorScheme.OCEAN_BLUE,
            isDark = false
        )
        assertEquals(Color(0xFF1D4ED8), oceanBlue.primary)

        val emeraldGreen = com.lingubible.app.core.theme.colorSchemeFor(
            palette = com.lingubible.app.core.settings.AppColorScheme.EMERALD_GREEN,
            isDark = false
        )
        assertEquals(Color(0xFF059669), emeraldGreen.primary)
    }

    @Test
    fun `verify Image Toolbox inverted colors and custom hex`() {
        val normalRed = com.lingubible.app.core.theme.colorSchemeFor(
            palette = com.lingubible.app.core.settings.AppColorScheme.RED,
            isDark = false,
            isInvertedColors = false
        )
        val invertedRed = com.lingubible.app.core.theme.colorSchemeFor(
            palette = com.lingubible.app.core.settings.AppColorScheme.RED,
            isDark = false,
            isInvertedColors = true
        )
        assertNotEquals(normalRed.primary, invertedRed.primary)

        val customScheme = com.lingubible.app.core.theme.colorSchemeFor(
            palette = com.lingubible.app.core.settings.AppColorScheme.LINGNAN_RED,
            isDark = false,
            customColorHex = 0xFF9C27B0L
        )
        assertEquals(Color(0xFF9C27B0), customScheme.primary)
    }

    @Test
    fun `verify background color dynamically changes with selected color scheme`() {
        val blueDark = com.lingubible.app.core.theme.colorSchemeFor(
            palette = com.lingubible.app.core.settings.AppColorScheme.OCEAN_BLUE,
            isDark = true,
            isOledBlack = false
        )
        val orangeDark = com.lingubible.app.core.theme.colorSchemeFor(
            palette = com.lingubible.app.core.settings.AppColorScheme.SUNSET_ORANGE,
            isDark = true,
            isOledBlack = false
        )
        val greenDark = com.lingubible.app.core.theme.colorSchemeFor(
            palette = com.lingubible.app.core.settings.AppColorScheme.EMERALD_GREEN,
            isDark = true,
            isOledBlack = false
        )

        // Backgrounds must be distinct and tinted with their palette hue
        assertNotEquals(blueDark.background, orangeDark.background)
        assertNotEquals(blueDark.background, greenDark.background)
        assertNotEquals(orangeDark.background, greenDark.background)

        // Surfaces must also follow the palette hue
        assertNotEquals(blueDark.surfaceContainer, orangeDark.surfaceContainer)

        val blueLight = com.lingubible.app.core.theme.colorSchemeFor(
            palette = com.lingubible.app.core.settings.AppColorScheme.OCEAN_BLUE,
            isDark = false
        )
        val orangeLight = com.lingubible.app.core.theme.colorSchemeFor(
            palette = com.lingubible.app.core.settings.AppColorScheme.SUNSET_ORANGE,
            isDark = false
        )

        // Light mode backgrounds must also reflect pastel tinted hue
        assertNotEquals(blueLight.background, orangeLight.background)
    }

    @Test
    fun `verify Google recommended and Android blue default schemes`() {
        val androidBlue = com.lingubible.app.core.settings.AppColorScheme.ANDROID_BLUE
        assertEquals("android_blue", androidBlue.code)
        assertEquals(0xFF0061A4L, androidBlue.primaryHex)
        assertEquals(0xFF9ECAFFL, androidBlue.darkPrimaryHex)

        val googleBlue = com.lingubible.app.core.settings.AppColorScheme.GOOGLE_BLUE
        assertEquals("google_blue", googleBlue.code)
        assertEquals(0xFF1A73E8L, googleBlue.primaryHex)
        assertEquals(0xFF8AB4F8L, googleBlue.darkPrimaryHex)

        val googlePurple = com.lingubible.app.core.settings.AppColorScheme.GOOGLE_PURPLE
        assertEquals("google_purple", googlePurple.code)
        assertEquals(0xFF6750A4L, googlePurple.primaryHex)
        assertEquals(0xFFD0BCFFL, googlePurple.darkPrimaryHex)

        // Verify Android Blue light and dark token generation
        val abLight = com.lingubible.app.core.theme.colorSchemeFor(androidBlue, isDark = false)
        val abDark = com.lingubible.app.core.theme.colorSchemeFor(androidBlue, isDark = true)
        assertEquals(Color(0xFF0061A4), abLight.primary)
        assertEquals(Color(0xFF9ECAFF), abDark.primary)

        // Verify Google Purple light and dark token generation
        val gpLight = com.lingubible.app.core.theme.colorSchemeFor(googlePurple, isDark = false)
        val gpDark = com.lingubible.app.core.theme.colorSchemeFor(googlePurple, isDark = true)
        assertEquals(Color(0xFF6750A4), gpLight.primary)
        assertEquals(Color(0xFFD0BCFF), gpDark.primary)

        // Verify Google Blue light and dark token generation
        val gbLight = com.lingubible.app.core.theme.colorSchemeFor(googleBlue, isDark = false)
        val gbDark = com.lingubible.app.core.theme.colorSchemeFor(googleBlue, isDark = true)
        assertEquals(Color(0xFF1A73E8), gbLight.primary)
        assertEquals(Color(0xFF8AB4F8), gbDark.primary)
    }

    @Test
    fun `verify default app color scheme is Red with OLED pitch black mode`() {
        val redScheme = com.lingubible.app.core.settings.AppColorScheme.LINGNAN_RED
        assertEquals("lingnan_red", redScheme.code)
        assertEquals("紅色", redScheme.titleZh)
        assertEquals("Red", redScheme.titleEn)
        assertEquals(0xFFE31F26L, redScheme.primaryHex)
        assertEquals(0xFFFF8A80L, redScheme.secondaryHex)
        assertEquals(0xFFFFCDD2L, redScheme.tertiaryHex)

        val defaultOledDarkScheme = com.lingubible.app.core.theme.colorSchemeFor(
            palette = redScheme,
            isDark = true,
            isOledBlack = true
        )
        assertEquals(Color(0xFF000000), defaultOledDarkScheme.background)
        assertEquals(Color(0xFF000000), defaultOledDarkScheme.surface)
        assertEquals(Color(0xFF000000), defaultOledDarkScheme.surfaceContainerLowest)
        assertEquals(Color(0xFFFF5449), defaultOledDarkScheme.primary)

        val defaultLightScheme = com.lingubible.app.core.theme.colorSchemeFor(
            palette = redScheme,
            isDark = false
        )
        assertEquals(Color(0xFFE31F26), defaultLightScheme.primary)
    }
}

