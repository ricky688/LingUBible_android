package com.lingubible.app.core.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class ThemeMode(val code: String, val titleZh: String, val titleEn: String) {
    SYSTEM("system", "系統", "System"),
    LIGHT("light", "淺色", "Light"),
    DARK("dark", "深色", "Dark")
}

enum class AppLanguage(val code: String, val locale: Locale, val label: String) {
    ZH_TW("zh-TW", Locale.TRADITIONAL_CHINESE, "繁中"),
    EN("en", Locale.ENGLISH, "EN")
}

enum class PaletteStyle(val code: String, val titleZh: String, val titleEn: String) {
    TONAL_SPOT("tonal_spot", "色調點綴", "Tonal Spot"),
    VIBRANT("vibrant", "生動活力", "Vibrant"),
    EXPRESSIVE("expressive", "表現力強", "Expressive"),
    NEUTRAL("neutral", "中性溫潤", "Neutral"),
    FRUIT_SALAD("fruit_salad", "水果沙拉", "Fruit Salad"),
    RAINBOW("rainbow", "彩虹七色", "Rainbow");

    fun label(isZh: Boolean): String = if (isZh) titleZh else titleEn
}

enum class AppColorScheme(
    val code: String,
    val titleZh: String,
    val titleEn: String,
    val primaryHex: Long,
    val secondaryHex: Long = primaryHex,
    val tertiaryHex: Long = primaryHex,
    val darkPrimaryHex: Long? = null,
    val darkSecondaryHex: Long? = null,
    val darkTertiaryHex: Long? = null
) {
    ANDROID_BLUE(
        "android_blue", "Android 藍色", "Android Blue",
        0xFF0061A4, 0xFF4193D8, 0xFF9ECAFF,
        0xFF9ECAFF, 0xFFBAC8DB, 0xFFD7BEE4
    ),
    GOOGLE_BLUE(
        "google_blue", "Google 藍色", "Google Blue",
        0xFF1A73E8, 0xFF4285F4, 0xFF8AB4F8,
        0xFF8AB4F8, 0xFFAECBFA, 0xFFC2E7FF
    ),
    GOOGLE_PURPLE(
        "google_purple", "Google 紫色", "Google Purple",
        0xFF6750A4, 0xFF8B77B8, 0xFFD0BCFF,
        0xFFD0BCFF, 0xFFCCC2DC, 0xFFEFB8C8
    ),
    RED("red", "鮮紅", "Bright Red", 0xFFE53935, 0xFFFF8A80, 0xFFFFCDD2),
    LINGNAN_RED(
        "lingnan_red", "紅色", "Red",
        0xFFE31F26, 0xFFFF8A80, 0xFFFFCDD2,
        0xFFFF5449, 0xFFFFB3AD, 0xFFFFB951
    ),
    BROWN("brown", "棕色", "Brown", 0xFF795548, 0xFFA1887F, 0xFFD7CCC8),
    SUNSET_ORANGE("sunset_orange", "橙色", "Orange", 0xFFFF9800, 0xFFFFB74D, 0xFFFFE0B2),
    YELLOW("yellow", "黃色", "Yellow", 0xFFFDD835, 0xFFFFF176, 0xFFFFF9C4),
    LIME_GREEN("lime_green", "淺綠", "Lime Green", 0xFF8BC34A, 0xFFAED581, 0xFFDCEDC8),
    EMERALD_GREEN("emerald_green", "綠色", "Green", 0xFF059669, 0xFF6EE7B7, 0xFFA7F3D0),
    CYAN_TEAL("cyan_teal", "青色", "Teal", 0xFF00ACC1, 0xFF4DD0E1, 0xFFB2EBF2),
    OCEAN_BLUE("ocean_blue", "藍色", "Blue", 0xFF1D4ED8, 0xFF60A5FA, 0xFFBFDBFE),
    LAVENDER_PURPLE("lavender_purple", "紫色", "Purple", 0xFF7C3AED, 0xFFA78BFA, 0xFFDDD6FE),
    SAKURA_PINK("sakura_pink", "粉紅", "Pink", 0xFFDB2777, 0xFFF472B6, 0xFFFCE7F3),
    MONOCHROME("monochrome", "灰色", "Grey", 0xFF424242, 0xFF757575, 0xFFE0E0E0);

    fun label(isZh: Boolean): String = if (isZh) titleZh else titleEn
}

class AppSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        ThemeMode.entries.find { it.code == prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.code) } ?: ThemeMode.DARK
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(
        AppLanguage.entries.find { it.code == prefs.getString(KEY_APP_LANGUAGE, AppLanguage.ZH_TW.code) } ?: AppLanguage.ZH_TW
    )
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _isOledBlack = MutableStateFlow(
        prefs.getBoolean(KEY_OLED_BLACK, true)
    )
    val isOledBlack: StateFlow<Boolean> = _isOledBlack.asStateFlow()

    private val _colorScheme = MutableStateFlow(
        AppColorScheme.entries.find { it.code == prefs.getString(KEY_COLOR_SCHEME, AppColorScheme.LINGNAN_RED.code) } ?: AppColorScheme.LINGNAN_RED
    )
    val colorScheme: StateFlow<AppColorScheme> = _colorScheme.asStateFlow()

    private val _paletteStyle = MutableStateFlow(
        PaletteStyle.entries.find { it.code == prefs.getString(KEY_PALETTE_STYLE, PaletteStyle.TONAL_SPOT.code) } ?: PaletteStyle.TONAL_SPOT
    )
    val paletteStyle: StateFlow<PaletteStyle> = _paletteStyle.asStateFlow()

    private val _isInvertedColors = MutableStateFlow(
        prefs.getBoolean(KEY_INVERTED_COLORS, false)
    )
    val isInvertedColors: StateFlow<Boolean> = _isInvertedColors.asStateFlow()

    private val _contrastLevel = MutableStateFlow(
        prefs.getFloat(KEY_CONTRAST_LEVEL, 0.0f)
    )
    val contrastLevel: StateFlow<Float> = _contrastLevel.asStateFlow()

    private val _customColorHex = MutableStateFlow<Long?>(
        if (prefs.contains(KEY_CUSTOM_COLOR_HEX)) prefs.getLong(KEY_CUSTOM_COLOR_HEX, 0L) else null
    )
    val customColorHex: StateFlow<Long?> = _customColorHex.asStateFlow()

    private val _isDynamicColor = MutableStateFlow(
        prefs.getBoolean(KEY_DYNAMIC_COLOR, false)
    )
    val isDynamicColor: StateFlow<Boolean> = _isDynamicColor.asStateFlow()

    private val _isHighContrast = MutableStateFlow(
        prefs.getBoolean(KEY_HIGH_CONTRAST, false)
    )
    val isHighContrast: StateFlow<Boolean> = _isHighContrast.asStateFlow()

    private val _isHapticsEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
    )
    val isHapticsEnabled: StateFlow<Boolean> = _isHapticsEnabled.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME_MODE, mode.code).apply()
        val nightMode = when (mode) {
            ThemeMode.LIGHT -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    fun setLanguage(lang: AppLanguage) {
        _appLanguage.value = lang
        prefs.edit().putString(KEY_APP_LANGUAGE, lang.code).apply()
    }

    fun setOledBlack(enabled: Boolean) {
        _isOledBlack.value = enabled
        prefs.edit().putBoolean(KEY_OLED_BLACK, enabled).apply()
    }

    fun setColorScheme(scheme: AppColorScheme) {
        _colorScheme.value = scheme
        _customColorHex.value = null
        prefs.edit()
            .putString(KEY_COLOR_SCHEME, scheme.code)
            .remove(KEY_CUSTOM_COLOR_HEX)
            .apply()
    }

    fun setPaletteStyle(style: PaletteStyle) {
        _paletteStyle.value = style
        prefs.edit().putString(KEY_PALETTE_STYLE, style.code).apply()
    }

    fun setInvertedColors(inverted: Boolean) {
        _isInvertedColors.value = inverted
        prefs.edit().putBoolean(KEY_INVERTED_COLORS, inverted).apply()
    }

    fun setContrastLevel(level: Float) {
        _contrastLevel.value = level
        prefs.edit().putFloat(KEY_CONTRAST_LEVEL, level).apply()
    }

    fun setCustomColorHex(hex: Long?) {
        _customColorHex.value = hex
        if (hex != null) {
            prefs.edit().putLong(KEY_CUSTOM_COLOR_HEX, hex).apply()
        } else {
            prefs.edit().remove(KEY_CUSTOM_COLOR_HEX).apply()
        }
    }

    fun resetColorSchemeDefaults() {
        _colorScheme.value = AppColorScheme.LINGNAN_RED
        _paletteStyle.value = PaletteStyle.TONAL_SPOT
        _isInvertedColors.value = false
        _contrastLevel.value = 0.0f
        _customColorHex.value = null
        _isDynamicColor.value = false
        prefs.edit()
            .putString(KEY_COLOR_SCHEME, AppColorScheme.LINGNAN_RED.code)
            .putString(KEY_PALETTE_STYLE, PaletteStyle.TONAL_SPOT.code)
            .putBoolean(KEY_INVERTED_COLORS, false)
            .putFloat(KEY_CONTRAST_LEVEL, 0.0f)
            .remove(KEY_CUSTOM_COLOR_HEX)
            .putBoolean(KEY_DYNAMIC_COLOR, false)
            .apply()
    }

    fun setDynamicColor(enabled: Boolean) {
        _isDynamicColor.value = enabled
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
    }

    fun setHighContrast(enabled: Boolean) {
        _isHighContrast.value = enabled
        prefs.edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _isHapticsEnabled.value = enabled
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_OLED_BLACK = "oled_black"
        private const val KEY_COLOR_SCHEME = "color_scheme"
        private const val KEY_PALETTE_STYLE = "palette_style"
        private const val KEY_INVERTED_COLORS = "inverted_colors"
        private const val KEY_CONTRAST_LEVEL = "contrast_level"
        private const val KEY_CUSTOM_COLOR_HEX = "custom_color_hex"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_HIGH_CONTRAST = "high_contrast"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
    }
}
