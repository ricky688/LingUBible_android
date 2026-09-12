package com.lingubible.app.ui.screens

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.settings.AppColorScheme
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.core.settings.PaletteStyle
import com.lingubible.app.core.settings.ThemeMode
import com.lingubible.app.core.theme.*
import com.lingubible.app.ui.components.FloatingCircles
import com.lingubible.app.ui.components.ImageToolboxColorSchemeSheet
import com.lingubible.app.ui.components.M3ButtonGroup
import com.lingubible.app.ui.components.M3ButtonGroupItem
import com.lingubible.app.ui.components.ScallopedFlowerBadge
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    settingsManager: AppSettingsManager = koinInject()
) {
    val isDark = isAppDarkTheme()
    val isOled = isOledBlackActive()
    val themeMode by settingsManager.themeMode.collectAsState()
    val appLanguage by settingsManager.appLanguage.collectAsState()
    val isOledBlack by settingsManager.isOledBlack.collectAsState()
    val activeColorScheme by settingsManager.colorScheme.collectAsState()
    val paletteStyle by settingsManager.paletteStyle.collectAsState()
    val isInvertedColors by settingsManager.isInvertedColors.collectAsState()
    val contrastLevel by settingsManager.contrastLevel.collectAsState()
    val customColorHex by settingsManager.customColorHex.collectAsState()
    val isDynamicColor by settingsManager.isDynamicColor.collectAsState()
    val isHighContrast by settingsManager.isHighContrast.collectAsState()
    val isHapticsEnabled by settingsManager.isHapticsEnabled.collectAsState()

    var showColorSchemeSheet by remember { mutableStateOf(false) }

    val isZh = appLanguage == AppLanguage.ZH_TW
    val haptics = LocalHapticFeedback.current

    fun triggerHaptic() {
        if (isHapticsEnabled) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val targetBorderColor = if (isHighContrast) {
        if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f)
    }
    val cardBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "settingsCardBorder"
    )

    val targetCardBg = if (isDark) {
        if (isOled) Color(0xFF000000) else MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val cardBg by animateColorAsState(
        targetValue = targetCardBg,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "settingsCardBg"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingCircles(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Theme Mode Card (Light, Dark, System)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isZh) "主題外觀模式" else "Appearance Theme Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isZh) "選擇日間淺色、夜間深色或跟隨系統" else "Choose light, dark, or system default mode",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val themeItems = listOf(
                        M3ButtonGroupItem(if (isZh) "淺色" else "Light", Icons.Filled.LightMode) {
                            triggerHaptic()
                            settingsManager.setThemeMode(ThemeMode.LIGHT)
                        },
                        M3ButtonGroupItem(if (isZh) "深色" else "Dark", Icons.Filled.DarkMode) {
                            triggerHaptic()
                            settingsManager.setThemeMode(ThemeMode.DARK)
                        },
                        M3ButtonGroupItem(if (isZh) "系統" else "System", Icons.Filled.BrightnessAuto) {
                            triggerHaptic()
                            settingsManager.setThemeMode(ThemeMode.SYSTEM)
                        }
                    )
                    val selectedThemeIndex = when (themeMode) {
                        ThemeMode.LIGHT -> 0
                        ThemeMode.DARK -> 1
                        ThemeMode.SYSTEM -> 2
                    }

                    M3ButtonGroup(
                        selectedIndex = selectedThemeIndex,
                        items = themeItems,
                        height = 42.dp,
                        spacing = 4.dp,
                        activeCornerRadius = 21.dp,
                        inactiveCornerRadius = 10.dp,
                        showCheckmarkOnSelected = true
                    )
                }
            }

            // 2. OLED Pitch Black Mode Card (#000000)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF1F5F9),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.NightsStay,
                                        contentDescription = null,
                                        tint = if (isDark && isOledBlack) Color(0xFF60A5FA) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isZh) "OLED 純黑模式" else "OLED Pure Black",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isDark) {
                                        if (isZh) "深色模式下使用純黑背景，有助於 OLED 螢幕省電並提升對比度"
                                        else "Use pure black background in dark mode to save battery on OLED screens"
                                    } else {
                                        if (isZh) "深色模式開啟時生效" else "Applies when Dark mode is enabled"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = isOledBlack,
                            onCheckedChange = {
                                triggerHaptic()
                                settingsManager.setOledBlack(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // 3. Accent Color Scheme Palettes (Image Toolbox Style)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isZh) "主題色彩方案" else "Accent Color Scheme",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = buildString {
                                        append(if (customColorHex != null) "自訂顏色" else activeColorScheme.label(isZh))
                                        append(" · ")
                                        append(paletteStyle.label(isZh))
                                        if (isInvertedColors) append(" · 反轉")
                                        if (contrastLevel > 0f) append(" · 對比 ${String.format("%.1f", contrastLevel)}")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                triggerHaptic()
                                android.util.Log.d("ImageToolbox", "Pencil button clicked, showColorSchemeSheet = true")
                                showColorSchemeSheet = true
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.22f else 0.12f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Color Scheme",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Android 12+ Dynamic Color Toggle
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFF1F5F9))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isZh) "Material You 動態桌布配色" else "Material You Dynamic Colors",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (isZh) "自動從系統桌面壁紙提取主色系" else "Extract color palette from device wallpaper",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isDynamicColor,
                                onCheckedChange = {
                                    triggerHaptic()
                                    settingsManager.setDynamicColor(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Image Toolbox Style "簡單變體" (Simple Variants) Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isZh) "簡單變體 (點擊快速切換)：" else "Simple Variants:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = if (isZh) "查看全部" else "View all",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                triggerHaptic()
                                showColorSchemeSheet = true
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal scrolling row of 12-Lobed Scalloped Flower Badges + (+) button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppColorScheme.entries.forEach { scheme ->
                            val isSelected = !isDynamicColor && customColorHex == null && activeColorScheme == scheme
                            val primary = Color(scheme.primaryHex)
                            val secondary = Color(scheme.secondaryHex)
                            val tertiary = Color(scheme.tertiaryHex)
                            ScallopedFlowerBadge(
                                primaryColor = primary,
                                secondaryColor = secondary,
                                tertiaryColor = tertiary,
                                isSelected = isSelected,
                                onClick = {
                                    triggerHaptic()
                                    settingsManager.setDynamicColor(false)
                                    settingsManager.setColorScheme(scheme)
                                }
                            )
                        }

                        // (+) Custom Color Button
                        ScallopedFlowerBadge(
                            primaryColor = if (customColorHex != null) Color(customColorHex!!) else MaterialTheme.colorScheme.primary,
                            secondaryColor = MaterialTheme.colorScheme.secondary,
                            tertiaryColor = MaterialTheme.colorScheme.tertiary,
                            isSelected = customColorHex != null,
                            isAddButton = true,
                            onClick = {
                                triggerHaptic()
                                showColorSchemeSheet = true
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dedicated Button to open complete Image Toolbox Color Scheme Sheet
                    OutlinedButton(
                        onClick = {
                            triggerHaptic()
                            showColorSchemeSheet = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isZh) "自訂色彩方案 (開啟完整調色盤)" else "Customize Color Scheme (Image Toolbox Style)",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Interactive Preview Sandbox
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.background,
                        border = BorderStroke(1.dp, cardBorderColor.copy(alpha = 0.7f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isZh) "🎨 即時效果預覽" else "🎨 Live Preview Sandbox",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isOled) "OLED #000000" else if (isDark) "Dark Theme" else "Light Theme",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { triggerHaptic() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isZh) "主要按鈕" else "Primary", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { triggerHaptic() },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        if (isZh) "外框按鈕" else "Outlined",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = if (isZh) "已選 3 學分" else "3 Credits",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "2026–27 Term 1",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = "★ 4.8 / 5.0",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Display & Tactile Preferences
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isZh) "介面與觸覺偏好" else "Display & Tactile Preferences",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isZh) "輔助視覺對比度與操作觸感" else "Accessibility contrast and haptic response",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = cardBorderColor.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // High Contrast Borders Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isZh) "強化高對比輪廓線" else "Enhanced Contrast Borders",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isZh) "使卡片與容器外框更加清晰明確" else "Sharpens borders around cards and dialogs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isHighContrast,
                            onCheckedChange = {
                                triggerHaptic()
                                settingsManager.setHighContrast(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    HorizontalDivider(color = cardBorderColor.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Tactile Haptics Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isZh) "觸覺回饋振動" else "Tactile Haptic Feedback",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isZh) "按鈕點擊與切換時伴隨細緻觸感回饋" else "Subtle vibration when tapping controls",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isHapticsEnabled,
                            onCheckedChange = {
                                if (it) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                settingsManager.setHapticsEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // 5. Language Switcher Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Translate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isZh) "顯示語言" else "Display Language",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isZh) "繁體中文 / English" else "Traditional Chinese / English",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val langItems = listOf(
                            M3ButtonGroupItem(AppLanguage.ZH_TW.label) {
                                triggerHaptic()
                                settingsManager.setLanguage(AppLanguage.ZH_TW)
                            },
                            M3ButtonGroupItem(AppLanguage.EN.label) {
                                triggerHaptic()
                                settingsManager.setLanguage(AppLanguage.EN)
                            }
                        )
                        val selectedLangIndex = when (appLanguage) {
                            AppLanguage.ZH_TW -> 0
                            AppLanguage.EN -> 1
                        }

                        M3ButtonGroup(
                            selectedIndex = selectedLangIndex,
                            items = langItems,
                            height = 36.dp,
                            spacing = 3.dp,
                            activeCornerRadius = 18.dp,
                            inactiveCornerRadius = 8.dp,
                            showCheckmarkOnSelected = false
                        )
                    }
                }
            }

            // 6. Reset to Defaults & App Version Info
            OutlinedButton(
                onClick = {
                    triggerHaptic()
                    settingsManager.setThemeMode(ThemeMode.DARK)
                    settingsManager.setOledBlack(true)
                    settingsManager.resetColorSchemeDefaults()
                    settingsManager.setHighContrast(false)
                    settingsManager.setHapticsEnabled(true)
                },
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, cardBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isZh) "恢復預設外觀設定" else "Reset to Default Appearance",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "LingUBible Android • Material 3 Expressive • v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Image Toolbox Modal Bottom Sheet
        if (showColorSchemeSheet) {
            ImageToolboxColorSchemeSheet(
                activeColorScheme = activeColorScheme,
                paletteStyle = paletteStyle,
                isInvertedColors = isInvertedColors,
                isDynamicColor = isDynamicColor,
                contrastLevel = contrastLevel,
                customColorHex = customColorHex,
                onSelectColorScheme = { scheme ->
                    triggerHaptic()
                    settingsManager.setDynamicColor(false)
                    settingsManager.setColorScheme(scheme)
                },
                onSelectPaletteStyle = { style ->
                    triggerHaptic()
                    settingsManager.setPaletteStyle(style)
                },
                onToggleInvertedColors = { inverted ->
                    triggerHaptic()
                    settingsManager.setInvertedColors(inverted)
                },
                onToggleDynamicColor = { dynamic ->
                    triggerHaptic()
                    settingsManager.setDynamicColor(dynamic)
                },
                onContrastLevelChange = { level ->
                    settingsManager.setContrastLevel(level)
                },
                onCustomColorSelected = { color ->
                    triggerHaptic()
                    val colorLong = ((color.red * 255).toLong() shl 16) or
                            ((color.green * 255).toLong() shl 8) or
                            (color.blue * 255).toLong() or
                            0xFF000000L
                    settingsManager.setCustomColorHex(colorLong)
                },
                onResetDefaults = {
                    triggerHaptic()
                    settingsManager.resetColorSchemeDefaults()
                },
                onDismiss = { showColorSchemeSheet = false }
            )
        }
    }
}
