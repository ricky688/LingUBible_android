package com.lingubible.app.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.core.settings.ThemeMode
import com.lingubible.app.core.theme.*
import com.lingubible.app.ui.common.mouseScrollbar
import org.koin.compose.koinInject

/**
 * Material 3 Expressive Navigation Drawer Content.
 *
 * Provides organized, spacious navigation for all app modules:
 * - Brand header with Lingnan red gradient and emblem
 * - Categorized items (Core Explore, Academic Tools, Account)
 * - Bilingual labels (Traditional Chinese + English)
 * - Pinned bottom Theme switcher (Light / Dark / System)
 * - Pinned bottom Language switcher (繁中 / EN)
 * - Edge-to-edge system bars (status bar + navigation bar padding)
 * - Mouse cursor hand indicator and desktop scrollbar support
 * - Native Material 3 NavigationDrawerItem selection semantics
 */
@Composable
fun LingUBibleDrawerSheet(
    navController: NavHostController,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    settingsManager: AppSettingsManager = koinInject()
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val sheetBg = MaterialTheme.colorScheme.surfaceContainerLow
    val headerBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f)
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight(),
        drawerContainerColor = sheetBg,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Brand Header Banner extending edge-to-edge into status bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 18.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "LingUBible",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "嶺南大學課程資訊平台",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "嶺大同學必備指南",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Course Hub",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }

            // 2. Middle Scrollable Destination Items
            Column(
                modifier = Modifier
                    .weight(1f)
                    .mouseScrollbar(scrollState)
                    .verticalScroll(scrollState)
                    .padding(vertical = 6.dp)
            ) {
                val categories = remember { DrawerCategory.entries }
                val allDestinations = remember { DrawerNavDestination.entries }

                categories.forEachIndexed { catIndex, category ->
                    val itemsInCategory = allDestinations.filter { it.category == category }
                    if (itemsInCategory.isNotEmpty()) {
                        // Category Header
                        Text(
                            text = "${category.titleZh} ${category.titleEn}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                            letterSpacing = 1.sp
                        )

                        itemsInCategory.forEach { destination ->
                            val selected = currentDestination?.hasRoute(destination.screen::class) == true

                            NavigationDrawerItem(
                                selected = selected,
                                onClick = {
                                    onCloseDrawer()
                                    if (!selected) {
                                        if (destination == DrawerNavDestination.HOME) {
                                            navController.navigate(Screen.Home) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    inclusive = false
                                                    saveState = false
                                                }
                                                launchSingleTop = true
                                                restoreState = false
                                            }
                                        } else {
                                            navController.navigate(destination.screen) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = "${destination.titleZh} ${destination.titleEn}"
                                    )
                                },
                                label = {
                                    Text(
                                        text = "${destination.titleZh} ${destination.titleEn}",
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                },
                                badge = destination.badgeText?.let { badge ->
                                    {
                                        Surface(
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = badge,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.5f else 0.35f),
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    unselectedTextColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155)
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 2.dp)
                                    .pointerHoverIcon(PointerIcon.Hand)
                            )
                        }

                        if (catIndex < categories.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                color = headerBorderColor
                            )
                        }
                    }
                }
            }

            // 3. Pinned Bottom Section (Theme Switcher, Language Switcher, and Version Footer)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = headerBorderColor
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val themeMode by settingsManager.themeMode.collectAsState()
                val appLanguage by settingsManager.appLanguage.collectAsState()

                // Row 1: Theme Mode Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "主題 Theme",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }

                    // 3-Segment connected pill for Theme
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, headerBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                val isSelected = themeMode == mode
                                val (icon, label) = when (mode) {
                                    ThemeMode.LIGHT -> Icons.Filled.LightMode to "淺色"
                                    ThemeMode.DARK -> Icons.Filled.DarkMode to "深色"
                                    ThemeMode.SYSTEM -> Icons.Filled.BrightnessAuto to "系統"
                                }
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { settingsManager.setThemeMode(mode) }
                                        .pointerHoverIcon(PointerIcon.Hand)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = mode.titleEn,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Row 2: Language Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "語言 Language",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }

                    // 2-Segment connected pill for Language
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, headerBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            AppLanguage.entries.forEach { lang ->
                                val isSelected = appLanguage == lang
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { settingsManager.setLanguage(lang) }
                                        .pointerHoverIcon(PointerIcon.Hand)
                                ) {
                                    Text(
                                        text = lang.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Row 3: Footer App Name & Version
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LingUBible Android",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "v1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
